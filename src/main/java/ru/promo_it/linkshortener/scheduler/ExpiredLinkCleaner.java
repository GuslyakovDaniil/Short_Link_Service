package ru.promo_it.linkshortener.scheduler;

import ru.promo_it.linkshortener.model.ShortLink;
import ru.promo_it.linkshortener.repository.LinkRepository;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiredLinkCleaner {
    private final LinkRepository repository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ExpiredLinkCleaner(LinkRepository repository) {
        this.repository = repository;
    }

    public void start() {
        Runnable task = () -> {
            System.out.println("[Scheduler] Running cleanup task...");
            for (ShortLink link : repository.findAll()) {
                if (link.isExpired()) {
                    System.out.println("[Scheduler] Deleting expired link: " + link.getShortCode());
                    repository.deleteByShortCode(link.getShortCode());
                }
            }
        };
        scheduler.scheduleAtFixedRate(task, 1, 1, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }
}