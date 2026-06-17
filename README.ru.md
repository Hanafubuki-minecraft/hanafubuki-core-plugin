# Hanafubuki Core Plugin

Paper-плагин для Minecraft с базовыми командами телепортации для survival-серверов.

[English](./README.md)

## Скачать

Актуальный `.jar` в [Releases](https://github.com/ByteMe6/hanafubuki-core-plugin/releases/latest).

## Команды

| Команда | Описание |
|---|---|
| `/spawn` | Телепорт на спаун мира |
| `/home` | Телепорт домой |
| `/tpa <игрок>` | Отправить запрос на телепортацию |
| `/tpaaccept` | Принять запрос на телепортацию |
| `/tpadeny` | Отклонить запрос на телепортацию |

## Требования

- Java 21
- Paper 1.21+

## Установка

1. Скачай `.jar` из [Releases](https://github.com/ByteMe6/hanafubuki-core-plugin/releases/latest)
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
