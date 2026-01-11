# URL Shortener CLI (Gradle + Java 17)

Консольный сервис для сокращения ссылок.

## Возможности
- Создание короткой ссылки из длинного URL
- Уникальные ссылки для разных пользователей (через UUID)
- Лимит переходов (после достижения ссылка блокируется)
- Время жизни ссылки (TTL) и автоудаление “протухших”
- Уведомления пользователю (лимит/TTL)
- Хранение ссылок в JSON (после перезапуска данные не теряются)

---

## Технологии
- Java 17
- Gradle
- Jackson (JSON)
- JUnit 5 (тесты)

---

## Структура проекта
src/main/java/shortener
config/ - конфигурация приложения
model/ - модели (Link, User, Notification)
repo/ - хранилище (LinkRepository, JsonFileLinkRepository)
service/ - бизнес-логика (LinkService, UserService, CleanupService, NotificationService)
App.java - точка входа
src/main/resources
app.properties
src/test/java
тесты

## Настройка конфигурации
Файл: `src/main/resources/app.properties`

Пример:
app.baseUrl=short.ly
app.ttlHours=24
app.defaultMaxClicks=10
app.cleanupIntervalSeconds=30
app.currentUserFile=.shortener-user
app.storageFile=./data/links.json

- `storageFile` — где хранить JSON со ссылками (папка `data/` не коммитится)
- `currentUserFile` — где хранится текущий UUID пользователя (не коммитится)

## Запуск
### В IntelliJ IDEA
1. Открыть проект
2. Открыть `App.java`
3. Нажать ▶ Run


### Через терминал (в корне проекта)
PowerShell / CMD:
```bash
.\gradlew run

```

## Команды в консоли
### После запуска доступны команды:

help — список команд
me — показать текущий UUID
use <uuid> — переключить пользователя
create <url> [limit] — создать короткую ссылку (limit необязателен)
open <short> — открыть короткую ссылку
list — показать мои ссылки
limit <short> <newLimit> — изменить лимит (только владелец)
delete <short> — удалить ссылку (только владелец)
notifications — показать уведомления
exit — выход

Пример:
create https://google.com 2
list
open short.ly/Ab12Cd3
notifications


