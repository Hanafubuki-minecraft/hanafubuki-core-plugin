# Hanafubuki Core Plugin

A Paper Minecraft plugin providing essential teleportation commands for survival servers.

[Русский](./README.ru.md)

## Commands

| Command | Description |
|---|---|
| `/spawn` | Teleport to world spawn |
| `/home` | Teleport to your set home |
| `/tpa <player>` | Send a teleport request to a player |
| `/tpaaccept` | Accept an incoming teleport request |
| `/tpadeny` | Deny an incoming teleport request |

## Requirements

- Java 21
- Paper 1.21+

## Build

```bash
mvn package
```

The compiled `.jar` will be in `target/`. Drop it into your server's `plugins/` folder and restart.

## Tech

- Java 21
- Paper API 1.21
- Maven + Shade plugin
