# Content Service
Сервис сообщений, подписок и ленты. Работает вместе с user-service (пользователи, логин, JWT).

> `PROJECT.md` — старое описание проекта, местами устарело. Актуальное поведение сервиса описано здесь.

## Запуск

### 1. База данных
У сервиса своя БД `content_db` (отдельный контейнер, порт 5434, чтобы не пересекаться с `user-postgres` на 5433):
```bash
docker run -d --name content-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=content_db -p 5434:5432 -v content_pgdata:/var/lib/postgresql/data --restart unless-stopped postgres:16
```
Таблицы создавать вручную не нужно: Flyway применит `db/migration/V1__init_schema.sql` при старте сервиса.

Подключение из DBeaver: `localhost:5434`, БД `content_db`, пользователь `postgres`, пароль `postgres`.

### 2. Сервис
Запустить `ContentserviceApplication` (или `./mvnw spring-boot:run`). Адрес: `http://localhost:9001`.

user-service должен быть запущен на `http://localhost:9000` (настраивается в `user-service.url`).

`jwt.secret` в `application.properties` должен совпадать с секретом в user-service, иначе токены будут отклоняться.

## Аутентификация
Все эндпоинты, кроме `/actuator/health`, требуют заголовок `Authorization: Bearer <token>`, токен выдаёт user-service (`POST /auth/login`).
Токен проверяется локально по общему секрету. Текущий пользователь всегда берётся из токена: его id не передаётся ни в теле, ни в пути (кроме просмотра чужих данных).

## Эндпоинты
Формат ответа везде `{ "code": 200, "data": ..., "message": "" }`. Списки возвращаются как `{ items, limit, offset, hasMore }`
(`limit` по умолчанию 20, максимум 50).

### Сообщения — `MessageController`
| Метод | Путь | Описание |
|---|---|---|
| POST | `/messages` | Создать сообщение `{ "text": "..." }` (1–280 символов), автор — текущий пользователь |
| GET | `/messages/{id}` | Получить сообщение |
| GET | `/messages?authorId=...&limit=&offset=` | Сообщения пользователя, новые сверху |
| DELETE | `/messages/{id}` | Удалить своё сообщение (чужое — 403) |

### Подписки — `SubscriptionController`
| Метод | Путь | Описание |
|---|---|---|
| POST | `/subscriptions` | Подписаться `{ "followingId": "<uuid>" }` |
| DELETE | `/subscriptions/{followingId}` | Отписаться |
| GET | `/subscriptions/{userId}/followers` | Подписчики пользователя |
| GET | `/subscriptions/{userId}/following` | На кого подписан пользователь |

Ошибки подписки: 400 — на себя, 404 — пользователь не найден (проверяется в user-service), 409 — уже подписан.

### Лента — `FeedController`
| Метод | Путь | Описание |
|---|---|---|
| GET | `/feed?limit=&offset=` | Лента текущего пользователя: его сообщения и сообщения тех, на кого он подписан, новые сверху |

## Взаимодействие с user-service
- Токен текущего пользователя пересылается в user-service (его `/users/**` требуют аутентификации).
- Подписка и создание сообщения проверяют пользователя через `GET /users/{id}`.
- Имена авторов в сообщениях и списках подписок подтягиваются одним батч-запросом `GET /users?ids=...`.
- Если user-service недоступен: чтения (лента, списки) работают, но вместо имени отдаётся только `id`; подписка и создание сообщения отвечают 503.

## Схема БД
```
messages(id BIGSERIAL PK, text VARCHAR(280), author_id UUID, created_at TIMESTAMP)
subscriptions(id BIGSERIAL PK, follower_id UUID, following_id UUID, created_at TIMESTAMP,
              UNIQUE(follower_id, following_id), CHECK(follower_id <> following_id))
```
`author_id`, `follower_id`, `following_id` — UUID пользователей из user-service, без внешних ключей (другая БД).

## Проверка вручную
```bash
# 1. Два пользователя (user-service)
curl -X POST localhost:9000/users -H "Content-Type: application/json" -d '{"firstname":"Anna","email":"anna@test.com","phone":"+79990000001","password":"123456"}'
curl -X POST localhost:9000/users -H "Content-Type: application/json" -d '{"firstname":"Boris","email":"boris@test.com","phone":"+79990000002","password":"123456"}'

# 2. Логин — в ответе userId и token (data.userId, data.token)
curl -X POST localhost:9000/auth/login -H "Content-Type: application/json" -d '{"email":"anna@test.com","password":"123456"}'
curl -X POST localhost:9000/auth/login -H "Content-Type: application/json" -d '{"email":"boris@test.com","password":"123456"}'

# 3. Борис пишет, Анна подписывается и читает ленту
curl -X POST localhost:9001/messages -H "Authorization: Bearer $BORIS_TOKEN" -H "Content-Type: application/json" -d '{"text":"Привет!"}'
curl -X POST localhost:9001/subscriptions -H "Authorization: Bearer $ANNA_TOKEN" -H "Content-Type: application/json" -d "{\"followingId\":\"$BORIS_ID\"}"
curl localhost:9001/feed -H "Authorization: Bearer $ANNA_TOKEN"
```
