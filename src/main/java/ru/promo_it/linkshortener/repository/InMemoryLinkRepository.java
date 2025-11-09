package ru.promo_it.linkshortener.repository;

import ru.promo_it.linkshortener.model.ShortLink;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryLinkRepository implements LinkRepository {
    private final Map<String, ShortLink> storage = new ConcurrentHashMap<>();

    @Override
    public void save(ShortLink link) {
        storage.put(link.getShortCode(), link);
    }

    @Override
    public Optional<ShortLink> findByShortCode(String shortCode) {
        return Optional.ofNullable(storage.get(shortCode));
    }

    @Override
    public void deleteByShortCode(String shortCode) {
        storage.remove(shortCode);
    }

    @Override
    public List<ShortLink> findByOwner(UUID ownerUuid) {
        return storage.values().stream()
                .filter(link -> link.getOwnerUuid().equals(ownerUuid))
                .collect(Collectors.toList());
    }

    @Override
    public List<ShortLink> findAll() {
        return List.copyOf(storage.values());
    }
}