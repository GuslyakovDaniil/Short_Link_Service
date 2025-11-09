package ru.promo_it.linkshortener.model;

import java.time.Instant;
import java.util.UUID;

public class ShortLink {
    private final String shortCode;
    private final String originalUrl;
    private final UUID ownerUuid;
    private final Instant createdAt;
    private final long ttlSeconds;
    private int clickLimit;
    private int clickCount;

    public ShortLink(String shortCode, String originalUrl, UUID ownerUuid, Instant createdAt, long ttlSeconds, int clickLimit) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.ownerUuid = ownerUuid;
        this.createdAt = createdAt;
        this.ttlSeconds = ttlSeconds;
        this.clickLimit = clickLimit;
        this.clickCount = 0;
    }

    public boolean isExpired() {
        return createdAt.plusSeconds(ttlSeconds).isBefore(Instant.now());
    }

    public boolean isLimitExceeded() {
        return clickCount >= clickLimit;
    }

    public void incrementClickCount() {
        this.clickCount++;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public int getClickLimit() {
        return clickLimit;
    }

    public void setClickLimit(int clickLimit) {
        this.clickLimit = clickLimit;
    }

    public int getClickCount() {
        return clickCount;
    }
}