package com.capstone.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {
    private int workerPoolSize = 4;
    private int maxRetries = 3;
    private long retryBaseDelaySeconds = 2;
    private double emailFailureRate = 0.3;
    private long emailWorkMs = 800;
    private long invoiceWorkMs = 1200;
    private long lockTtlMs = 30000;
    private long pollMs = 500;
    private int consumerBufferLimit = 1000;
    private String consumerGroup = "task-scheduler-workers";

    public int getWorkerPoolSize() { return workerPoolSize; }
    public void setWorkerPoolSize(int v) { this.workerPoolSize = v; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int v) { this.maxRetries = v; }
    public long getRetryBaseDelaySeconds() { return retryBaseDelaySeconds; }
    public void setRetryBaseDelaySeconds(long v) { this.retryBaseDelaySeconds = v; }
    public double getEmailFailureRate() { return emailFailureRate; }
    public void setEmailFailureRate(double v) { this.emailFailureRate = v; }
    public long getEmailWorkMs() { return emailWorkMs; }
    public void setEmailWorkMs(long v) { this.emailWorkMs = v; }
    public long getInvoiceWorkMs() { return invoiceWorkMs; }
    public void setInvoiceWorkMs(long v) { this.invoiceWorkMs = v; }
    public long getLockTtlMs() { return lockTtlMs; }
    public void setLockTtlMs(long v) { this.lockTtlMs = v; }
    public long getPollMs() { return pollMs; }
    public void setPollMs(long v) { this.pollMs = v; }
    public int getConsumerBufferLimit() { return consumerBufferLimit; }
    public void setConsumerBufferLimit(int v) { this.consumerBufferLimit = v; }
    public String getConsumerGroup() { return consumerGroup; }
    public void setConsumerGroup(String v) { this.consumerGroup = v; }
}
