package shortener.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    @Test
    void drainOnEmptyReturnsEmptyList() {
        NotificationService svc = new NotificationService();
        assertTrue(svc.drain(UUID.randomUUID()).isEmpty());
    }

    @Test
    void pushThenDrainReturnsNotification() {
        NotificationService svc = new NotificationService();
        UUID userId = UUID.randomUUID();

        svc.push(userId, "Hello");
        var list = svc.drain(userId);

        assertEquals(1, list.size());
        assertTrue(list.get(0).getMessage().contains("Hello"));
    }

    @Test
    void drainClearsInbox() {
        NotificationService svc = new NotificationService();
        UUID userId = UUID.randomUUID();

        svc.push(userId, "One");
        assertEquals(1, svc.drain(userId).size());
        assertTrue(svc.drain(userId).isEmpty());
    }
}
