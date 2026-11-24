package com.capstone.scheduler.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/** All Redis usage: delayed_jobs sorted set, SET NX PX locks, per-minute rate counters. */
@Component
public class RedisJobStore {
    public static final String DELAYED_KEY = "delayed_jobs";
    public static final String LOCK_PREFIX = "locks:job:";
    private static final String STAT_PREFIX = "stats:";
    private static final Duration STAT_TTL = Duration.ofHours(2);

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redis;

    public RedisJobStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // ---- delayed_jobs sorted set ----
    public void scheduleDelayed(String member, long runAtEpochMs) {
        redis.opsForZSet().add(DELAYED_KEY, member, (double) runAtEpochMs);
    }

    /** Returns members that are due AND were successfully claimed by this caller (ZREM == 1). */
    public List<String> claimDue(long nowEpochMs, int limit) {
        List<String> claimed = new ArrayList<>();
        Set<String> due = redis.opsForZSet().rangeByScore(DELAYED_KEY, 0, (double) nowEpochMs, 0, limit);
        if (due == null) return claimed;
        for (String member : due) {
            Long removed = redis.opsForZSet().remove(DELAYED_KEY, member);
            if (removed != null && removed > 0) claimed.add(member);
        }
        return claimed;
    }

    public long delayedCount() {
        Long n = redis.opsForZSet().zCard(DELAYED_KEY);
        return n == null ? 0 : n;
    }

    // ---- distributed lock: SET key token NX PX ttl ----
    public boolean tryLock(String jobId, String token, long ttlMs) {
        Boolean ok = redis.opsForValue().setIfAbsent(LOCK_PREFIX + jobId, token, Duration.ofMillis(ttlMs));
        return Boolean.TRUE.equals(ok);
    }

    /** Compare-and-delete so a worker can never release a lock it no longer owns. */
    public boolean unlock(String jobId, String token) {
        Long r = redis.execute(UNLOCK_SCRIPT, List.of(LOCK_PREFIX + jobId), token);
        return r != null && r > 0;
    }

    // ---- rate counters (one key per minute, 2h TTL) ----
    public void recordProcessed(boolean success) {
        long minute = System.currentTimeMillis() / 60000L;
        bump(STAT_PREFIX + "processed:" + minute);
        bump(STAT_PREFIX + (success ? "success:" : "failure:") + minute);
    }

    private void bump(String key) {
        redis.opsForValue().increment(key);
        redis.expire(key, STAT_TTL);
    }

    /** Oldest-first list of per-minute counters ending at the current minute. */
    public List<Map<String, Object>> series(int minutes) {
        long current = System.currentTimeMillis() / 60000L;
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = minutes - 1; i >= 0; i--) {
            long m = current - i;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("minuteStartMs", m * 60000L);
            row.put("processed", read(STAT_PREFIX + "processed:" + m));
            row.put("success", read(STAT_PREFIX + "success:" + m));
            row.put("failure", read(STAT_PREFIX + "failure:" + m));
            out.add(row);
        }
        return out;
    }

    private long read(String key) {
        String v = redis.opsForValue().get(key);
        return v == null ? 0L : Long.parseLong(v);
    }
}
