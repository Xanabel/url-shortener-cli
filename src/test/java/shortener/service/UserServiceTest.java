package shortener.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shortener.model.User;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void getOrCreateCreatesAndPersistsUser(@TempDir Path dir) {
        Path file = dir.resolve(".shortener-user");
        UserService svc = new UserService(file.toString());

        User u1 = svc.getOrCreateCurrentUser();
        assertNotNull(u1.getId());

        // второй вызов должен прочитать тот же UUID из файла
        User u2 = svc.getOrCreateCurrentUser();
        assertEquals(u1.getId(), u2.getId());
    }

    @Test
    void useUserSwitchesAndPersists(@TempDir Path dir) {
        Path file = dir.resolve(".shortener-user");
        UserService svc = new UserService(file.toString());

        UUID id = UUID.randomUUID();
        User u = svc.useUser(id.toString());

        assertEquals(id, u.getId());

        // после переключения тот же UUID должен читаться как "текущий"
        User current = svc.getOrCreateCurrentUser();
        assertEquals(id, current.getId());
    }

    @Test
    void getOrCreateIfFileBrokenCreatesNew(@TempDir Path dir) throws Exception {
        Path file = dir.resolve(".shortener-user");
        java.nio.file.Files.writeString(file, "not-a-uuid");

        UserService svc = new UserService(file.toString());
        User u = svc.getOrCreateCurrentUser();

        assertNotNull(u.getId());
        assertDoesNotThrow(() -> UUID.fromString(java.nio.file.Files.readString(file).trim()));
    }
}
