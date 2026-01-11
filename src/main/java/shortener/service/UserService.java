package shortener.service;

import shortener.model.User;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class UserService {
    private final Path currentUserFile;

    public UserService(String currentUserFile) {
        this.currentUserFile = Path.of(currentUserFile);
    }

    public User getOrCreateCurrentUser() {
        try {
            if (Files.exists(currentUserFile)) {
                String s = Files.readString(currentUserFile).trim();
                return new User(UUID.fromString(s));
            }
        } catch (Exception ignored) {}

        User u = User.newUser();
        saveCurrentUser(u);
        return u;
    }

    public User useUser(String uuidInput) {
        UUID id = UUID.fromString(uuidInput.trim());
        User u = new User(id);
        saveCurrentUser(u);
        return u;
    }

    private void saveCurrentUser(User user) {
        try {
            Files.writeString(currentUserFile, user.getId().toString());
        } catch (Exception e) {
            System.out.println("Не удалось сохранить текущего пользователя: " + e.getMessage());
        }
    }
}
