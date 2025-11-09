package ru.promo_it.linkshortener.service;

import ru.promo_it.linkshortener.config.AppConfig;
import ru.promo_it.linkshortener.exception.*;
import ru.promo_it.linkshortener.model.ShortLink;
import ru.promo_it.linkshortener.repository.LinkRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class LinkService {
    private final LinkRepository repository;
    private final AppConfig config;

    public LinkService(LinkRepository repository, AppConfig config) {
        this.repository = repository;
        this.config = config;
    }

    public ShortLink create(String originalUrl, UUID ownerUuid, Optional<Integer> limit) {
        String shortCode;
        do {
            shortCode = generateShortCode(originalUrl, ownerUuid);
        } while (repository.findByShortCode(shortCode).isPresent());

        ShortLink link = new ShortLink(
                shortCode,
                originalUrl,
                ownerUuid,
                Instant.now(),
                config.getLinkTtlSeconds(),
                limit.orElse(config.getDefaultClickLimit())
        );
        repository.save(link);
        return link;
    }

    public String getOriginalUrlAndRegisterClick(String shortCode) {
        ShortLink link = repository.findByShortCode(shortCode).orElseThrow(LinkNotFoundException::new);

        if (link.isExpired()) {
            throw new LinkExpiredException();
        }
        if (link.isLimitExceeded()) {
            throw new LimitExceededException();
        }

        link.incrementClickCount();
        repository.save(link);

        return link.getOriginalUrl();
    }

    public void delete(String shortCode, UUID ownerUuid) {
        ShortLink link = repository.findByShortCode(shortCode).orElseThrow(LinkNotFoundException::new);
        if (!link.getOwnerUuid().equals(ownerUuid)) {
            throw new SecurityException("У вас нет прав на удаление этой ссылки.");
        }
        repository.deleteByShortCode(shortCode);
    }

    private String generateShortCode(String url, UUID owner) {
        try {
            String combined = url + owner.toString() + System.nanoTime();
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 7);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }
}