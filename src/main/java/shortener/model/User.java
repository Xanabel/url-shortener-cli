package shortener.model;

import java.util.UUID;

public class User {
    private final UUID id;

    public User(UUID id) {
        this.id = id;
    }

    public static User newUser() {
        return new User(UUID.randomUUID());
    }

    public UUID getId() {
        return id;
    }
}
