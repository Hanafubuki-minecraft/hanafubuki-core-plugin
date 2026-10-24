# Hanafubuki Core Plugin

A Paper Minecraft plugin providing essential teleportation commands for survival servers.

[Русский](./README.ru.md)

## Download

Grab the latest `.jar` from [Releases](https://github.com/Hanafubuki-minecraft/hanafubuki-core-plugin/releases/latest).

## Commands

| Command | Description |
|---|---|
| `/spawn` | Teleport to world spawn |
| `/home` | Teleport to your set home |
| `/tpa <player>` | Send a teleport request to a player |
| `/tpaaccept` | Accept an incoming teleport request |
| `/tpadeny` | Deny an incoming teleport request |
| `/resources` | Teleport to a random safe spot in the resource world (needs [hanafubuki-resource-world](https://github.com/Hanafubuki-minecraft/hanafubuki-resource-world)) |

## Requirements

- Java 21
- Paper 1.21+

## Installation

1. Download the `.jar` from [Releases](https://github.com/Hanafubuki-minecraft/hanafubuki-core-plugin/releases/latest)
2. Drop it into your server's `plugins/` folder
3. Restart the server

## Build from source

```bash
mvn package
```

The compiled `.jar` will be in `target/`.

## Tech

- Java 21
- Paper API 1.21
- Maven + Shade plugin
