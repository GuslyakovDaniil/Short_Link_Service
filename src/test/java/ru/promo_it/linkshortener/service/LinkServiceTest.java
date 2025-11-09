package ru.promo_it.linkshortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.promo_it.linkshortener.config.AppConfig;
import ru.promo_it.linkshortener.exception.*;
import ru.promo_it.linkshortener.model.ShortLink;
import ru.promo_it.linkshortener.repository.InMemoryLinkRepository;
import ru.promo_it.linkshortener.repository.LinkRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {
    private LinkService linkService;
    private LinkRepository repository;
    private AppConfig config;
    private final UUID user1 = UUID.randomUUID();
    private final UUID user2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = new InMemoryLinkRepository();
        config = new AppConfig();
        linkService = new LinkService(repository, config);
    }

    @Test
    @DisplayName("Создание ссылки должно возвращать объект с 7-значным коротким кодом")
    void create_shouldReturnLinkWithShortCode() {
        ShortLink link = linkService.create("https://google.com", user1, Optional.empty());
        assertNotNull(link);
        assertNotNull(link.getShortCode());
        assertEquals(7, link.getShortCode().length());
    }

    @Test
    @DisplayName("Создание ссылок для одного URL разными пользователями должно генерировать разные коды")
    void create_shouldGenerateUniqueCodesForSameUrlAndDifferentUsers() {
        ShortLink link1 = linkService.create("https://google.com", user1, Optional.empty());
        ShortLink link2 = linkService.create("https://google.com", user2, Optional.empty());
        assertNotEquals(link1.getShortCode(), link2.getShortCode());
    }

    @Test
    @DisplayName("Создание ссылки без указания лимита должно применять лимит по умолчанию из конфига")
    void create_shouldApplyDefaultLimit_whenLimitIsNotProvided() {
        ShortLink link = linkService.create("https://google.com", user1, Optional.empty());
        assertEquals(config.getDefaultClickLimit(), link.getClickLimit());
    }

    @Test
    @DisplayName("Переход по ссылке должен возвращать URL и увеличивать счетчик кликов")
    void getOriginalUrlAndRegisterClick_shouldReturnUrlAndIncrementCount() {
        ShortLink link = linkService.create("https://google.com", user1, Optional.of(5));
        String url = linkService.getOriginalUrlAndRegisterClick(link.getShortCode());

        assertEquals("https://google.com", url);
        ShortLink updatedLink = repository.findByShortCode(link.getShortCode()).get();
        assertEquals(1, updatedLink.getClickCount());
    }

    @Test
    @DisplayName("Переход по ссылке должен выбрасывать исключение, если лимит исчерпан")
    void getOriginalUrlAndRegisterClick_shouldThrowException_whenLimitExceeded() {
        ShortLink link = linkService.create("https://google.com", user1, Optional.of(1));
        linkService.getOriginalUrlAndRegisterClick(link.getShortCode());

        assertThrows(LimitExceededException.class, () -> {
            linkService.getOriginalUrlAndRegisterClick(link.getShortCode());
        });
    }

    @Test
    @DisplayName("Переход по ссылке должен выбрасывать исключение, если ссылка не найдена")
    void getOriginalUrlAndRegisterClick_shouldThrowException_whenLinkNotFound() {
        assertThrows(LinkNotFoundException.class, () -> {
            linkService.getOriginalUrlAndRegisterClick("unknown");
        });
    }

    @Test
    @DisplayName("Переход по ссылке должен выбрасывать исключение, если срок жизни истек")
    void getOriginalUrlAndRegisterClick_shouldThrowException_whenTtlExpired() {
        // Создаем ссылку, которая "была создана" 2 дня назад, при TTL в 1 день
        Instant pastDate = Instant.now().minus(2, ChronoUnit.DAYS);
        ShortLink expiredLink = new ShortLink("expired", "https://expired.com", user1, pastDate, config.getLinkTtlSeconds(), 10);
        repository.save(expiredLink);

        assertThrows(LinkExpiredException.class, () -> {
            linkService.getOriginalUrlAndRegisterClick("expired");
        });
    }

    @Test
    @DisplayName("Удаление ссылки владельцем должно проходить успешно")
    void delete_shouldRemoveLink_whenUserIsOwner() {
        ShortLink link = linkService.create("https://google.com", user1, Optional.empty());
        String shortCode = link.getShortCode();

        assertDoesNotThrow(() -> {
            linkService.delete(shortCode, user1);
        });

        assertTrue(repository.findByShortCode(shortCode).isEmpty());
    }

    @Test
    @DisplayName("Удаление ссылки не-владельцем должно выбрасывать исключение")
    void delete_shouldThrowException_whenUserIsNotOwner() {
        ShortLink link = linkService.create("https://google.com", user1, Optional.empty());
        String shortCode = link.getShortCode();

        assertThrows(SecurityException.class, () -> {
            linkService.delete(shortCode, user2);
        });

        assertTrue(repository.findByShortCode(shortCode).isPresent());
    }

    @Test
    @DisplayName("Удаление несуществующей ссылки должно выбрасывать исключение")
    void delete_shouldThrowException_whenLinkNotFound() {
        assertThrows(LinkNotFoundException.class, () -> {
            linkService.delete("unknown", user1);
        });
    }
}