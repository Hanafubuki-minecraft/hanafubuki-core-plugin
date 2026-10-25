# Hanafubuki Core Plugin

Paper-плагин для Minecraft с базовыми командами телепортации для survival-серверов.

[English](./README.md)

## Скачать

Актуальный `.jar` в [Releases](https://github.com/Hanafubuki-minecraft/hanafubuki-core-plugin/releases/latest).

## Команды

| Команда | Описание |
|---|---|
| `/spawn` | Телепорт на спаун мира |
| `/home` | Телепорт домой |
| `/tpa <игрок>` | Отправить запрос на телепортацию |
| `/tpaaccept` | Принять запрос на телепортацию |
| `/tpadeny` | Отклонить запрос на телепортацию |
| `/resources` | Телепорт в случайную безопасную точку мира ресурсов (нужен [hanafubuki-resource-world](https://github.com/Hanafubuki-minecraft/hanafubuki-resource-world)) |

## Требования

- Java 21
- Paper 1.21+

## Установка

1. Скачай `.jar` из [Releases](https://github.com/Hanafubuki-minecraft/hanafubuki-core-plugin/releases/latest)
2. Положи в папку `plugins/` сервера
3. Перезапусти сервер

## Сборка из исходников

```bash
mvn package
```

Скомпилированный `.jar` появится в `target/`.

## Технологии

- Java 21
- Paper API 1.21
- Maven + Shade plugin

## /help для игроков

`/help` и `/?` показывают короткий список команд, которыми игроки реально могут пользоваться (настраивается в `messages.yml` → `help`), а не все команды сервера.

## Скрытые команды

Обычные игроки не видят в Tab `/plugins`, `/version` и команды с префиксом (`minecraft:give`…), а при вводе получают ванильное «неизвестная команда». Опы видят всё. Список — в `config.yml` → `hide-from-non-ops`.

## Тексты

Все тексты — в `plugins/Hanafubuki/messages.yml` (MiniMessage), по [стилю Hanafubuki](https://github.com/Hanafubuki-minecraft/hanafubuki-brand/blob/main/STYLE.md). `/msg` (`/tell`, `/w`, `/m`) и `/r` заменяют ванильный шёпот на оформленный; сообщения о входе и выходе тоже в стиле сервера (`config.yml` → `chat.join-quit`).

## Настройка: команды и стиль

- `config.yml` → `commands`: переименовать любую команду, поменять алиасы или выключить (после рестарта).
- `config.yml` → `style`: палитра сообщений и символ префикса. Тексты в `messages.yml` используют смысловые теги — `<accent>`, `<text>`, `<muted>`, `<dim>`, `<success>`, `<error>`, `<gold>`, `<symbol>` — поэтому перекрасить всё можно одной правкой.
- `/help` показывает только команды, которые игрок реально может выполнить (опы видят всё), по разделам и со страницами; разделы, аргументы и описания — в `messages.yml` → `help`.
