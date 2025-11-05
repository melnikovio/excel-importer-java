# Сервис импорта данных из Excel

## Архитектура

![alt text](docs/scheme.png)

## Сборка и запуск

Запуск зависимостей:

```bash
docker compose up -d importer-postgres
```

После запуска можно открыть Swagger:

```bash
http://localhost:8080/swagger-ui/index.html
```
