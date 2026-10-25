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

## Player /help

`/help` and `/?` show a short list of commands players can actually use (configurable in `messages.yml` → `help`), instead of every command on the server.

## Hidden commands

Non-op players don't see `/plugins`, `/version` and namespaced commands (`minecraft:give`…) in tab completion, and typing them gives the vanilla "unknown command" message. Ops see everything. The list is in `config.yml` → `hide-from-non-ops`; `plugins` hides every command of the listed plugins (WorldEdit's `//wand`, `//set`, CoreProtect, Chunky…).

## Messages

All texts live in `plugins/Hanafubuki/messages.yml` (MiniMessage) and follow the [Hanafubuki text style](https://github.com/Hanafubuki-minecraft/hanafubuki-brand/blob/main/STYLE.md). `/msg` (`/tell`, `/w`, `/m`) and `/r` replace the vanilla whisper with a styled one; join and quit messages are styled too (`config.yml` → `chat.join-quit`).

## Configuration: commands and style

- `config.yml` → `commands`: rename any command, change aliases or disable it (restart to apply).
- `config.yml` → `style`: the message palette and prefix symbol. Texts in `messages.yml` use semantic tags — `<accent>`, `<text>`, `<muted>`, `<dim>`, `<success>`, `<error>`, `<gold>`, `<symbol>` — so recoloring everything is one edit.
- `/help` lists only the commands the player can actually run (ops see everything), grouped and paged; groups, usages and descriptions are in `messages.yml` → `help`.

## Sakura creepers

With a configurable chance (`config.yml` → `sakura-creepers.chance`, default 25%) a creeper bursts into a cloud of cherry petals instead of exploding — no damage, no broken blocks.

## Admin

`/hanafubuki reload` (`/hf reload`, ops only — `hanafubuki.admin`) reloads `config.yml` and `messages.yml` without a restart: texts, style, `/help`, hidden command lists, sakura creepers. Command names and aliases still need a restart.
