(function () {
  'use strict';
  var $ = function (id) { return document.getElementById(id); };
  var REFRESH_MS = 3000;
  var COLORS = { green: '#22c55e', red: '#ef4444', blue: '#3b82f6', amber: '#f59e0b', slate: '#64748b', grid: '#22304f', text: '#8fa1c2' };

  function esc(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]; }); }
  function fmt(t) { return t ? new Date(t).toLocaleTimeString() : '-'; }
  function fmtFull(t) { return t ? new Date(t).toLocaleString() : '-'; }
  function badge(cls, v) { return '<span class="badge ' + cls + '-' + esc(v) + '">' + esc(v) + '</span>'; }

  function api(path, opts) {
    return fetch(path, opts).then(function (r) {
      return r.text().then(function (t) {
        var body = t ? JSON.parse(t) : null;
        if (!r.ok) throw new Error((body && body.message) || ('HTTP ' + r.status));
        return body;
      });
    });
  }

  // ---------- tiny canvas charts (no external libraries) ----------
  function setup(canvas) {
    var dpr = window.devicePixelRatio || 1, w = canvas.clientWidth, h = canvas.getAttribute('height') * 1;
    canvas.width = w * dpr; canvas.height = h * dpr;
    var c = canvas.getContext('2d'); c.scale(dpr, dpr); c.clearRect(0, 0, w, h);
    c.font = '11px Segoe UI, sans-serif'; return { c: c, w: w, h: h };
  }
  function axes(g, max, labels) {
    var c = g.c, left = 34, bottom = 22, top = 8, ph = g.h - bottom - top;
    c.strokeStyle = COLORS.grid; c.fillStyle = COLORS.text; c.lineWidth = 1;
    for (var i = 0; i <= 4; i++) {
      var y = top + ph * i / 4; c.beginPath(); c.moveTo(left, y); c.lineTo(g.w - 6, y); c.stroke();
      c.textAlign = 'right'; c.fillText(Math.round(max * (4 - i) / 4), left - 5, y + 4);
    }
    c.textAlign = 'center';
    var n = labels.length, pw = g.w - left - 6, step = Math.max(1, Math.ceil(n / 8));
    for (var j = 0; j < n; j += step) c.fillText(labels[j], left + pw * (j + 0.5) / n, g.h - 6);
    return { left: left, top: top, pw: pw, ph: ph, n: n };
  }
  function niceMax(v) { v = Math.max(v, 4); var p = Math.pow(10, Math.floor(Math.log10(v))); return Math.ceil(v / p) * p; }
  function lineChart(id, labels, values) {
    var g = setup($(id)), max = niceMax(Math.max.apply(null, values.concat([0]))), a = axes(g, max, labels), c = g.c;
    c.strokeStyle = COLORS.blue; c.lineWidth = 2; c.beginPath();
    values.forEach(function (v, i) { var x = a.left + a.pw * (i + 0.5) / a.n, y = a.top + a.ph * (1 - v / max); i ? c.lineTo(x, y) : c.moveTo(x, y); });
    c.stroke(); c.fillStyle = COLORS.blue;
    values.forEach(function (v, i) { var x = a.left + a.pw * (i + 0.5) / a.n, y = a.top + a.ph * (1 - v / max); c.beginPath(); c.arc(x, y, 2.5, 0, 7); c.fill(); });
  }
  function barChart(id, labels, success, failure) {
    var g = setup($(id)), tot = success.map(function (s, i) { return s + failure[i]; });
    var max = niceMax(Math.max.apply(null, tot.concat([0]))), a = axes(g, max, labels), c = g.c, bw = Math.max(4, a.pw / a.n * 0.6);
    success.forEach(function (s, i) {
      var x = a.left + a.pw * (i + 0.5) / a.n - bw / 2, hs = a.ph * s / max, hf = a.ph * failure[i] / max;
      c.fillStyle = COLORS.green; c.fillRect(x, a.top + a.ph - hs, bw, hs);
      c.fillStyle = COLORS.red; c.fillRect(x, a.top + a.ph - hs - hf, bw, hf);
    });
  }
  function donut(id, items) {
    var g = setup($(id)), c = g.c, total = items.reduce(function (s, i) { return s + i.v; }, 0);
    var cx = g.w * 0.32, cy = g.h / 2, r = Math.min(g.h / 2 - 8, g.w * 0.28);
    if (!total) { c.fillStyle = COLORS.text; c.textAlign = 'center'; c.fillText('No data yet', g.w / 2, g.h / 2); return; }
    var ang = -Math.PI / 2;
    items.forEach(function (it) {
      var d = 2 * Math.PI * it.v / total; c.beginPath(); c.moveTo(cx, cy); c.arc(cx, cy, r, ang, ang + d); c.closePath(); c.fillStyle = it.color; c.fill(); ang += d;
    });
    c.globalCompositeOperation = 'destination-out'; c.beginPath(); c.arc(cx, cy, r * 0.58, 0, 7); c.fill(); c.globalCompositeOperation = 'source-over';
    c.fillStyle = '#e5ecf8'; c.textAlign = 'center'; c.font = 'bold 18px Segoe UI'; c.fillText(total, cx, cy + 6); c.font = '12px Segoe UI';
    items.forEach(function (it, i) {
      var y = 40 + i * 24, x = g.w * 0.64; c.fillStyle = it.color; c.fillRect(x, y - 10, 12, 12);
      c.fillStyle = '#e5ecf8'; c.textAlign = 'left'; c.fillText(it.label + ': ' + it.v + ' (' + Math.round(100 * it.v / total) + '%)', x + 18, y);
    });
  }

  // ---------- rendering ----------
  var lastStats = null;
  function renderStats(s) {
    lastStats = s;
    ['total', 'pending', 'running', 'success', 'retrying', 'failed'].forEach(function (k) { $('c-' + k).textContent = s[k]; });
    $('i-workers').textContent = s.activeWorkers + '/' + s.workerPoolSize;
    $('i-buffer').textContent = s.bufferedMessages;
    $('i-delayed').textContent = s.delayedQueueSize;
    $('i-cur').textContent = s.currentMinuteProcessed;
    $('i-last').textContent = s.lastFullMinuteProcessed;
    $('i-retry').textContent = s.retrySuccessRatePercent == null ? 'n/a' : s.retrySuccessRatePercent + '% (' + s.jobsRecoveredByRetry + '/' + s.jobsThatFailedAtLeastOnce + ')';
    drawCharts();
  }
  function drawCharts() {
    if (!lastStats) return; var s = lastStats, pm = s.perMinute;
    var labels = pm.map(function (m) { var d = new Date(m.minuteStartMs); return d.getHours() + ':' + ('0' + d.getMinutes()).slice(-2); });
    var run = 0, cum = pm.map(function (m) { run += m.processed; return run; });
    lineChart('ch-time', labels, cum);
    barChart('ch-rate', labels, pm.map(function (m) { return m.success; }), pm.map(function (m) { return m.failure; }));
    var finishedOk = s.success, terminalFail = s.failed;
    donut('ch-sf', [{ label: 'Success', v: finishedOk, color: COLORS.green }, { label: 'Failed (DLQ)', v: terminalFail, color: COLORS.red }, { label: 'Retrying', v: s.retrying, color: COLORS.amber }]);
    donut('ch-prio', [{ label: 'HIGH', v: s.byPriority.HIGH, color: COLORS.red }, { label: 'MEDIUM', v: s.byPriority.MEDIUM, color: COLORS.amber }, { label: 'LOW', v: s.byPriority.LOW, color: COLORS.blue }]);
  }
  function renderJobs(list) {
    var rows = list.map(function (j) {
      return '<tr><td class="mono">' + esc(j.id) + '</td><td>' + esc(j.type) + '</td><td>' + badge('p', j.priority) + '</td><td>' + badge('s', j.status) +
        '</td><td>' + j.attempts + (j.retryCount ? ' <span class="muted">(' + j.retryCount + ' retries)</span>' : '') + '</td><td>' + fmt(j.createdAt) + '</td><td>' + fmt(j.updatedAt) +
        '</td><td><button class="btn small" data-detail="' + esc(j.id) + '">Details</button></td></tr>';
    });
    $('jobs-body').innerHTML = rows.join('') || '<tr><td colspan="8" class="muted">No jobs match the filters</td></tr>';
  }
  function renderDlq(list) {
    $('dlq-count').textContent = list.length + ' job(s)';
    var rows = list.map(function (d) {
      return '<tr><td class="mono">' + esc(d.jobId) + '</td><td>' + esc(d.type) + '</td><td>' + badge('p', d.priority) + '</td><td>' + d.attempts +
        '</td><td class="wrap">' + esc(d.reason) + '</td><td>' + fmt(d.failedAt) + '</td><td><button class="btn small" data-detail="' + esc(d.jobId) +
        '">History</button> <button class="btn small" data-retry="' + esc(d.jobId) + '">Retry</button></td></tr>';
    });
    $('dlq-body').innerHTML = rows.join('') || '<tr><td colspan="7" class="muted">No dead-lettered jobs</td></tr>';
  }

  function jobsUrl() {
    var p = [], st = $('f-status').value, pr = $('f-prio').value, q = $('f-q').value.trim();
    if (st) p.push('status=' + st); if (pr) p.push('priority=' + pr); if (q) p.push('q=' + encodeURIComponent(q));
    p.push('size=100'); return '/api/jobs?' + p.join('&');
  }
  function refresh() {
    Promise.all([api('/api/stats'), api(jobsUrl()), api('/api/dlq')]).then(function (r) {
      renderStats(r[0]); renderJobs(r[1]); renderDlq(r[2]); $('live').className = 'live'; $('live').textContent = '\u25CF live';
    }).catch(function () { $('live').className = 'live off'; $('live').textContent = '\u25CF backend unreachable'; });
  }

  // ---------- details modal ----------
  function openDetail(id) {
    api('/api/jobs/' + encodeURIComponent(id)).then(function (d) {
      var j = d.job, prevEnd = null;
      var h = '<div class="kv"><div>Job ID</div><div class="mono">' + esc(j.id) + '</div><div>Type</div><div>' + esc(j.type) + '</div><div>Priority</div><div>' + badge('p', j.priority) +
        '</div><div>Status</div><div>' + badge('s', j.status) + '</div><div>Attempts</div><div>' + j.attempts + ' (retries used: ' + j.retryCount + ')</div><div>Scheduled for</div><div>' + fmtFull(j.runAt) +
        '</div><div>Cron</div><div>' + esc(j.cron || '-') + '</div><div>Created</div><div>' + fmtFull(j.createdAt) + '</div><div>Payload</div><div class="mono">' + esc(j.payload) +
        '</div><div>Last error</div><div>' + esc(j.lastError || '-') + '</div></div><h3>Execution history</h3><div class="table-wrap"><table><thead><tr><th>#</th><th>Started</th><th>Finished</th><th>Gap since previous attempt</th><th>Result</th><th>Worker</th><th>Error / output</th></tr></thead><tbody>';
      d.executions.forEach(function (e) {
        var gap = prevEnd ? ((new Date(e.startedAt) - prevEnd) / 1000).toFixed(1) + ' s' : '-';
        h += '<tr><td>' + e.attemptNo + '</td><td>' + fmt(e.startedAt) + '</td><td>' + fmt(e.finishedAt) + '</td><td>' + gap + '</td><td>' + esc(e.result) + '</td><td>' + esc(e.worker) + '</td><td class="wrap">' + esc(e.error || '') + '</td></tr>';
        prevEnd = e.finishedAt ? new Date(e.finishedAt) : null;
      });
      $('d-body').innerHTML = h + '</tbody></table></div>'; $('m-detail').classList.add('open');
    }).catch(function (e) { alert(e.message); });
  }

  // ---------- submit modal ----------
  function openModal(id) { $(id).classList.add('open'); }
  function closeModals() { document.querySelectorAll('.modal').forEach(function (m) { m.classList.remove('open'); }); }
  function submitJob() {
    var msg = $('s-msg'), mode = $('s-mode').value, payload;
    try { payload = $('s-payload').value.trim() ? JSON.parse($('s-payload').value) : null; } catch (e) { msg.className = 'msg err'; msg.textContent = 'Payload must be valid JSON'; return; }
    var body = { type: $('s-type').value, priority: $('s-prio').value, payload: payload, permanentFailure: $('s-perm').checked };
    if (mode === 'delay') body.delaySeconds = parseInt($('s-delay').value, 10);
    if (mode === 'cron') body.cron = $('s-cron').value.trim();
    api('/api/jobs', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }).then(function (j) {
      msg.className = 'msg ok'; msg.textContent = 'Created job ' + j.id; refresh();
    }).catch(function (e) { msg.className = 'msg err'; msg.textContent = e.message; });
  }

  // ---------- wiring ----------
  $('btnNew').onclick = function () { $('s-msg').textContent = ''; openModal('m-submit'); };
  $('btnBatch').onclick = function () { api('/api/demo/batch?count=100', { method: 'POST' }).then(refresh).catch(function (e) { alert(e.message); }); };
  $('btnPerm').onclick = function () { api('/api/demo/permanent-failure', { method: 'POST' }).then(refresh).catch(function (e) { alert(e.message); }); };
  $('s-go').onclick = submitJob;
  $('s-mode').onchange = function () { $('l-delay').classList.toggle('hide', this.value !== 'delay'); $('l-cron').classList.toggle('hide', this.value !== 'cron'); };
  document.querySelectorAll('[data-close]').forEach(function (b) { b.onclick = closeModals; });
  document.addEventListener('click', function (ev) {
    var t = ev.target;
    if (t.dataset && t.dataset.detail) openDetail(t.dataset.detail);
    if (t.dataset && t.dataset.retry) api('/api/jobs/' + encodeURIComponent(t.dataset.retry) + '/retry', { method: 'POST' }).then(refresh).catch(function (e) { alert(e.message); });
    if (t.classList && t.classList.contains('modal')) closeModals();
  });
  ['f-status', 'f-prio'].forEach(function (id) { $(id).onchange = refresh; });
  var timer; $('f-q').oninput = function () { clearTimeout(timer); timer = setTimeout(refresh, 300); };
  window.addEventListener('resize', drawCharts);
  refresh(); setInterval(refresh, REFRESH_MS);
})();
