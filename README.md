# DiscordChatBridge (English)

A two-way bridge between **Minecraft** (Spigot / Paper / Purpur) chat and **Discord**

---

## ✨ Features

|                  | Details                                                                       |
| ---------------- | ----------------------------------------------------------------------------- |
| **MC → Discord** | Messages are forwarded to Discord as an **Embed** with the player’s avatar    |
| **Discord → MC** | A Discord user’s reply appears in-game with a customizable prefix             |
| **Join / Quit**  | Player joins and quits are announced with colorful Embeds                     |
| **Account link** | You can link your Minecraft account to your Discord account                   |
| **HEX colors**   | `&RRGGBB` supported in config (1.16+)                                         |
| **1.16 → 1.20**  | Works on any Paper/Purpur core 1.16.5+                                        |
| **MySQL**        | MySQL database support included                                               |

---

## 📋 Version Matrix

|   Minecraft     | Latest release | Java version | Platforms | Support status        |
|:---------------:|:--------------:|:------------:|:---------:|:----------------------|
| 1.20 – 1.20.6   |    _latest_    |      21      | Paper     | ✅ **Active Release** |
| 1.19 – 1.19.4   |    _latest_    |      17      | Paper     | ✅ **Active Release** |

---

## 📥 Installation

1. **Download** the release from [Releases](https://github.com/freadc0de/DiscordBridge/releases)
2. Drop `DiscordChatBridge.jar` into your server’s `plugins/` folder.
3. Start the server, then open `plugins/DiscordChatBridge/config.yml`
4. `/dchat reload` — reload the plugin configuration.

> 💡 **Tip:** In Discord’s developer panel enable *MESSAGE CONTENT INTENT*, otherwise the bot can’t read message text.

---

## 🛠️ Building from source

# 1. Clone the repository
```bash
git clone https://github.com/your-repo/DiscordChatBridge.git
cd DiscordChatBridge
```

# 2. Build the fat-jar (Gradle 8, Java 17)
```bash
./gradlew shadowJar
```

# 3. The built file will be here:
```bash
build/libs/discord-chat-bridge-<version>.jar
```

| Key                    | Description                       |
| ---------------------- | --------------------------------- |
| `token`                | Discord bot token                 |
| `channelId`            | ID of the channel the plugin bridges |
| `to‑minecraft-prefix`  | Prefix for messages coming from Discord  |
| `discord-to-minecraft` | Format of the line that appears in Minecraft chat  |

Supported placeholders: `{player}`, `{author}`, `{message}`.

---

## 🤝 Contributing

PRs are welcome!
Commit message format: `type(scope): subject`.




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

## 📋 Список версий

|    Minecraft    | Последний релиз | Версия Java  | Платформы     | Статус поддержки               |
|:---------------:|:---------------:|:------------:|:--------------|:-------------------------------|
|  1.20 - 1.20.6  |    _latest_     |      21      | Paper         | ✅ **Active Release**          |
|  1.19 – 1.19.4  |    _latest_     |      17      | Paper         | ✅ **Active Release**          |

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
