package shortener.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import shortener.model.Link;

import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonFileLinkRepository implements LinkRepository {
    private final Path file;
    private final ObjectMapper mapper;

    private final Map<String, Link> storage = new ConcurrentHashMap<>();

    public JsonFileLinkRepository(String storageFile) {
        this.file = Path.of(storageFile);
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        loadFromDisk();
    }

    private void loadFromDisk() {
        try {
            if (!Files.exists(file)) return;

            String json = Files.readString(file);
            if (json.isBlank()) return;

            List<LinkDto> list = mapper.readValue(json, new TypeReference<>() {});
            for (LinkDto dto : list) {
                Link link = new Link(
                        dto.originalUrl,
                        dto.shortUrl,
                        dto.ownerId,
                        dto.maxClicks,
                        dto.createdAt,
                        dto.expiresAt
                );
                for (int i = 0; i < dto.currentClicks; i++) link.incrementClicks();
                storage.put(link.getShortUrl(), link);
            }
        } catch (Exception e) {
            System.out.println("Не удалось загрузить links.json: " + e.getMessage());
        }
    }
    private synchronized void flushToDisk() {
        try {
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);

            List<LinkDto> list = new ArrayList<>();
            for (Link link : storage.values()) {
                LinkDto dto = new LinkDto();
                dto.originalUrl = link.getOriginalUrl();
                dto.shortUrl = link.getShortUrl();
                dto.ownerId = link.getOwnerId();
                dto.maxClicks = link.getMaxClicks();
                dto.currentClicks = link.getCurrentClicks();
                dto.createdAt = link.getCreatedAt();
                dto.expiresAt = link.getExpiresAt();
                list.add(dto);
            }

            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);

            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения хранилища: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Link link) {
        storage.put(link.getShortUrl(), link);
        flushToDisk();
    }

    @Override
    public Optional<Link> findByShortUrl(String shortUrl) {
        return Optional.ofNullable(storage.get(shortUrl));
    }

    @Override
    public List<Link> findByOwner(UUID ownerId) {
        List<Link> res = new ArrayList<>();
        for (Link l : storage.values()) if (l.getOwnerId().equals(ownerId)) res.add(l);
        res.sort(Comparator.comparing(Link::getShortUrl));
        return res;
    }

    @Override
    public void delete(String shortUrl) {
        storage.remove(shortUrl);
        flushToDisk();
    }

    @Override
    public boolean exists(String shortUrl) {
        return storage.containsKey(shortUrl);
    }

    @Override
    public List<Link> findExpired(Instant now) {
        List<Link> res = new ArrayList<>();
        for (Link l : storage.values()) {
            if (l.isExpired(now)) res.add(l);
        }
        return res;
    }
}
