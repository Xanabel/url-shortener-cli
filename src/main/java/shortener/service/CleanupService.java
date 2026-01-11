package shortener.service;

import shortener.model.Link;
import shortener.repo.LinkRepository;

import java.time.Instant;
import java.util.List;

public class CleanupService implements Runnable {
    private final LinkRepository repo;
    private final NotificationService notifier;

    public CleanupService(LinkRepository repo, NotificationService notifier) {
        this.repo = repo;
        this.notifier = notifier;
    }

    @Override
    public void run() {
        Instant now = Instant.now();
        List<Link> expired = repo.findExpired(now);

        for(Link link : expired) {
            repo.delete(link.getShortUrl());
            notifier.push(link.getOwnerId(), "TTL истёк, ссылка удалена: " + link.getShortUrl());
        }
    }
}
