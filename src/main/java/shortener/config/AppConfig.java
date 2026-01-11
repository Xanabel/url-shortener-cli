package shortener.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final String baseUrl;
    private final int ttlHours;
    private final int defaultMaxClicks;
    private final int cleanupIntervalSeconds;
    private final String currentUserFile;
    private final String storageFile;

    public AppConfig(String baseUrl, int ttlHours, int defaultMaxClicks, int cleanupIntervalSeconds,
                     String currentUserFile, String storageFile) {
        this.baseUrl = baseUrl;
        this.ttlHours = ttlHours;
        this.defaultMaxClicks = defaultMaxClicks;
        this.cleanupIntervalSeconds = cleanupIntervalSeconds;
        this.currentUserFile = currentUserFile;
        this.storageFile = storageFile;
    }
    public static AppConfig load() {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in == null) throw new IllegalStateException("Не найден app.properties в resources.");

            Properties p = new Properties();
            p.load(in);

            return new AppConfig(
                    p.getProperty("app.baseUrl", "short.ly"),
                    Integer.parseInt(p.getProperty("app.ttlHours", "24")),
                    Integer.parseInt(p.getProperty("app.defaultMaxClicks", "10")),
                    Integer.parseInt(p.getProperty("app.cleanupIntervalSeconds", "30")),
                    p.getProperty("app.currentUserFile", ".shortener-user"),
                    p.getProperty("app.storageFile", "./data/links.json")
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки конфигурации: " + e.getMessage(), e);
        }
    }
    public String baseUrl() { return baseUrl; }
    public int ttlHours() { return ttlHours; }
    public int defaultMaxClicks() { return defaultMaxClicks; }
    public int cleanupIntervalSeconds() { return cleanupIntervalSeconds; }
    public String currentUserFile() { return currentUserFile; }
    public String storageFile() { return storageFile; }
}
