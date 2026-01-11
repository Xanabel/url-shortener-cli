package shortener.model;

import java.time.Instant;

public class Notification {
    private final Instant at;
    private final String message;

    public Notification(Instant at, String message) {
        this.at = at;
        this.message = message;
    }

    public Instant getAt() { return at; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return at + " - " + message;
    }
}