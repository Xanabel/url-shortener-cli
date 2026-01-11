package shortener.service;

import shortener.model.Notification;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NotificationService {
    private final Map<UUID, Queue<Notification>> inbox = new ConcurrentHashMap<>();

    public void push(UUID userId, String message) {
        inbox.computeIfAbsent(userId, id -> new ConcurrentLinkedQueue<>())
                .add(new Notification(Instant.now(), message));
    }

    public List<Notification> drain(UUID userId) {
        Queue<Notification> q = inbox.getOrDefault(userId, new ConcurrentLinkedQueue<>());
        List<Notification> out = new ArrayList<>();
        Notification n;
        while ((n = q.poll()) != null) out.add(n);
        return out;
    }
}
