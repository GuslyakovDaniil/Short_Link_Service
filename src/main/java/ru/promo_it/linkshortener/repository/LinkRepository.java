package ru.promo_it.linkshortener.repository;

import ru.promo_it.linkshortener.model.ShortLink;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository {
    void save(ShortLink link);
    Optional<ShortLink> findByShortCode(String shortCode);
    void deleteByShortCode(String shortCode);
    List<ShortLink> findByOwner(UUID ownerUuid);
    List<ShortLink> findAll();
}