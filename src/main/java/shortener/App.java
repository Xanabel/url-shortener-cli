package shortener;

import shortener.config.AppConfig;
import shortener.model.User;
import shortener.repo.JsonFileLinkRepository;
import shortener.repo.LinkRepository;
import shortener.model.Notification;
import shortener.service.*;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        AppConfig config = AppConfig.load();

        NotificationService notifier = new NotificationService();
        LinkRepository repo = new JsonFileLinkRepository(config.storageFile());

        UserService userService = new UserService(config.currentUserFile());
        User currentUser = userService.getOrCreateCurrentUser();

        LinkService linkService = new LinkService(repo, notifier, config);

        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
        schedule.scheduleAtFixedRate(
                new CleanupService(repo, notifier),
                config.cleanupIntervalSeconds(),
                config.cleanupIntervalSeconds(),
                TimeUnit.SECONDS
        );

        Scanner scanner = new Scanner(System.in);

        System.out.println("ShortLinkService запущен.");
        System.out.println("Текущий пользователь UUID: " + currentUser.getId());
        System.out.println("Введите 'help' для списка команд.");

        while (true) {
            System.out.print("\n> ");
            String line = scanner.nextLine().trim();
            if (line.isBlank()) continue;

            try {
                String[] parts = line.split("\\s+");
                String cmd = parts[0];

                switch (cmd) {
                    case "help" -> System.out.println("""
                            Команды:
                            help                     - помощь
                            me                       - показать текущий UUID
                            use <uuid>               - переключить пользователя
                            create <url> [limit]     - создать короткую ссылку (limit опционален)
                            open <short.ly/xxxxxxx>  - открыть ссылку (попытка через браузер)
                            list                     - показать мои ссылки
                            limit <short> <newLimit> - изменить лимит (только владелец)
                            delete <short>           - удалить ссылку (только владелец)
                            notifications            - показать уведомления
                            exit                     -  выход
                            """);
                    case "me" -> System.out.println("Ваш UUID: " + currentUser.getId());

                    case "use" -> {
                        if (parts.length < 2) {
                            System.out.println("Использование: use <uuid>");
                            break;
                        }
                        currentUser = userService.useUser(parts[1]);
                        System.out.println("Текущий пользователь UUID: " + currentUser.getId());
                    }

                    case "create" -> {
                        if (parts.length < 2) {
                            System.out.println("Использование: create <url> [limit]");
                            break;
                        }
                        String url = parts[1];
                        Integer limit = (parts.length >= 3) ? Integer.parseInt(parts[2]) : null;

                        var link = linkService.createLink(currentUser.getId(), url, limit);
                        System.out.println("Короткая ссылка: " + link.getShortUrl());
                        System.out.println("Действительна до: " + link.getExpiresAt());
                    }

                    case "open" -> {
                        if (parts.length < 2) {
                            System.out.println("Использование: open <short>");
                            break;
                        }
                        linkService.openLink(parts[1]);
                    }

                    case "list" -> {
                        var links = repo.findByOwner(currentUser.getId());
                        if (links.isEmpty()) {
                            System.out.println("У вас нет ссылок.");
                        } else {
                            links.forEach(System.out::println);
                        }
                    }

                    case "limit" -> {
                        if (parts.length < 3) {
                            System.out.println("Использование: limit <short> <newLimit>");
                            break;
                        }
                        String shortUrl = parts[1];
                        int newLimit = Integer.parseInt(parts[2]);
                        linkService.updateLimit(currentUser.getId(), shortUrl, newLimit);
                        System.out.println("Лимит обновлён.");
                    }

                    case "delete" -> {
                        if (parts.length < 2) {
                            System.out.println("Использование: delete <short>");
                            break;
                        }
                        linkService.delete(currentUser.getId(), parts[1]);
                        System.out.println("Ссылка удалена.");
                    }

                    case "notifications" -> {
                        List<Notification> list = notifier.drain(currentUser.getId());
                        if (list.isEmpty()) {
                            System.out.println("Уведомлений нет.");
                        } else {
                            list.forEach(System.out::println);
                        }
                    }

                    case "exit" -> {
                        schedule.shutdownNow();
                        scanner.close();
                        System.out.println("Выход...");
                        return;
                    }

                    default -> System.out.println("Неизвестная команда. Введите 'help'.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }
}
