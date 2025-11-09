package ru.promo_it.linkshortener.cli;

import ru.promo_it.linkshortener.config.AppConfig;
import ru.promo_it.linkshortener.model.ShortLink;
import ru.promo_it.linkshortener.repository.InMemoryLinkRepository;
import ru.promo_it.linkshortener.repository.LinkRepository;
import ru.promo_it.linkshortener.scheduler.ExpiredLinkCleaner;
import ru.promo_it.linkshortener.service.LinkService;

import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        LinkRepository repository = new InMemoryLinkRepository();
        LinkService service = new LinkService(repository, config);
        ExpiredLinkCleaner cleaner = new ExpiredLinkCleaner(repository);
        cleaner.start();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Сервис сокращения ссылок запущен.");
        printHelp();

        UUID currentUser = null;

        while (true) {
            System.out.print("> ");
            String[] command = scanner.nextLine().trim().split("\\s+");
            String action = command[0].toLowerCase();

            try {
                switch (action) {
                    case "create":
                        if (currentUser == null) {
                            currentUser = UUID.randomUUID();
                            System.out.println("Вы новый пользователь. Ваш уникальный ID: " + currentUser);
                            System.out.println("Используйте его для управления вашими ссылками.");
                        }
                        Optional<Integer> limit = command.length > 2 ? Optional.of(Integer.parseInt(command[2])) : Optional.empty();
                        ShortLink link = service.create(command[1], currentUser, limit);
                        System.out.println("Создана короткая ссылка: " + link.getShortCode());
                        break;

                    case "open":
                        String originalUrl = service.getOriginalUrlAndRegisterClick(command[1]);
                        Desktop.getDesktop().browse(new URI(originalUrl));
                        System.out.println("Перенаправление на: " + originalUrl);
                        break;

                    case "delete":
                        if (currentUser == null) throw new IllegalStateException("Сначала создайте ссылку, чтобы получить UUID.");
                        service.delete(command[1], currentUser);
                        System.out.println("Ссылка " + command[1] + " удалена.");
                        break;

                    case "list":
                        if (currentUser == null) throw new IllegalStateException("Сначала создайте ссылку, чтобы получить UUID.");
                        repository.findByOwner(currentUser).forEach(l ->
                                System.out.printf(" - %s -> %s (лимит: %d/%d, активна: %b)\n",
                                        l.getShortCode(), l.getOriginalUrl(), l.getClickCount(), l.getClickLimit(), !l.isExpired()));
                        break;

                    case "help":
                        printHelp();
                        break;

                    case "exit":
                        cleaner.stop();
                        System.out.println("Завершение работы...");
                        return;

                    default:
                        System.out.println("Неизвестная команда. Введите 'help' для помощи.");
                }
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static void printHelp() {
        System.out.println("\nДоступные команды:");
        System.out.println("  create <url> [limit] - Создать короткую ссылку. Лимит опционален.");
        System.out.println("  open <short_code>   - Перейти по короткой ссылке.");
        System.out.println("  delete <short_code> - Удалить вашу ссылку.");
        System.out.println("  list                - Показать все ваши ссылки.");
        System.out.println("  help                - Показать это сообщение.");
        System.out.println("  exit                - Выйти из программы.\n");
    }
}