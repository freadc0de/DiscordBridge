# DiscordChatBridge

Двусторонний мост между чатом **Minecraft** (Spigot/Paper/Purpur) и **Discord**

---

## ✨ Возможности

|                  | Подробности                                                                      |
| ---------------- | -------------------------------------------------------------------------------- |
| **MC → Discord** | Сообщения пересылаются в Discord **Embed**‑сообщением с аватаром игрока          |
| **Discord → MC** | Ответ Discord‑пользователя появляется в игровом чате с настраиваемым префиксом   |
| **Join / Quit**  | Вход и выход игроков объявляется цветными Embed‑ами                              |
| **Account link** | Вы можете связать свой аккаунт Minecraft с аккунтом Discord                      |
| **HEX‑цвета**    | В конфиге поддерживается `&RRGGBB` (1.16+)                                       |
| **1.16 → 1.20**  | Работает на любых ядрах Paper/Purpur 1.16.5+                                     |
| **MySQL**        | Присутствует поддержка базы данных MySQL                                         |

---

## 📥 Установка

1. **Скачайте** релиз с [Releases](https://github.com/freadc0de/DiscordBridge/releases)
2. Положите `DiscordChatBridge.jar` в папку `plugins/` вашего сервера.
3. Запустите сервер, затем откройте `plugins/DiscordChatBridge/config.yml`
4. `/dchat reload` — перезапуск конфигурации плагина.

> 💡 **Совет:** в панели разработчика Discord включите *MESSAGE CONTENT INTENT*, иначе бот не будет видеть текст сообщений.

---

## 🛠️ Сборка из исходников

# 1. Клонируем репозиторий
```bash
git clone https://github.com/your-repo/DiscordChatBridge.git
cd DiscordChatBridge
```

# 2. Собираем fat‑jar (Gradle 8, Java 17)
```bash
./gradlew shadowJar
```

# 3. Готовый файл будет здесь:
```bash
build/libs/discord-chat-bridge-<version>.jar
```

| Ключ                   | Описание                          |
| ---------------------- | --------------------------------- |
| `token`                | Токен Discord‑бота                |
| `channelId`            | ID канала, где мостит плагин      |
| `to‑minecraft-prefix`  | Префикс для сообщений из Discord  |
| `discord-to-minecraft` | Формат строки, приходящей в игру  |

Поддерживаемые плейсхолдеры: `{player}`, `{author}`, `{message}`.

---

## 🤝 Contributing

PR‑ы приветствуются!
Формат коммит‑сообщений: `type(scope): subject`.
