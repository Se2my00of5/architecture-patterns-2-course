# Памятка по OAuth2 и межсервисной авторизации

## 1) Фронтенд: аутентификация пользователя (Authorization Code + PKCE)

1. Фронт редиректит пользователя на `user_service/oauth2/authorize` с параметрами:
   - `response_type=code`
   - `client_id`
   - `redirect_uri`
   - `scope`
   - `code_challenge` + `code_challenge_method=S256`
   - `state`
2. Пользователь проходит логин/пароль на стороне auth-сервера (`user_service`).
3. Сервер возвращает браузер на `redirect_uri` с `code` и `state`.
4. Фронт делает `POST /oauth2/token` с:
   - `grant_type=authorization_code`
   - `code`
   - `redirect_uri`
   - `client_id`
   - `client_secret`
   - `code_verifier`
5. Фронт получает `access_token` и `refresh_token`.
6. Все защищённые запросы идут с `Authorization: Bearer <access_token>`.

## 2) Фронтенд: обновление access_token (Refresh Token)

1. Когда access token истёк (обычно `401`), фронт вызывает `POST /oauth2/token`.
2. Тело запроса:
   - `grant_type=refresh_token`
   - `refresh_token`
   - `client_id`
   - `client_secret`
3. В ответе приходит новый `access_token` (и при ротации новый `refresh_token`).
4. Фронт обновляет локально сохранённые токены и продолжает запросы.

## 3) Межсервисная авторизация (core_service -> user_service)

Используется OAuth2 `client_credentials` (machine-to-machine), без пользователя.

1. `core_service` получает service-token через `POST user_service/oauth2/token`:
   - `grant_type=client_credentials`
   - `client_id=SERVICE_CLIENT_ID`
   - `client_secret=SERVICE_CLIENT_SECRET`
   - `scope=api`
2. `core_service` кэширует access token в памяти до истечения (`expires_in` с запасом).
3. `core_service` вызывает `user_service` с Bearer service-token, например:
   - `GET /api/users/{id}` для проверки существования пользователя.

## 4) Где это настроено

- Auth server и OAuth-клиенты:
  - `user_service/src/main/java/ru/hits/user_service/config/OAuth2AuthorizationServerConfig.java`
  - `user_service/src/main/resources/application.yml`
- Проверка пользователя из core:
  - `core_service/src/main/java/ru/hits/core_service/integration/UserServiceClient.java`
  - `core_service/src/main/java/ru/hits/core_service/handler/command/AccountCommandHandler.java`
  - `core_service/src/main/java/ru/hits/core_service/handler/query/AccountQueryHandler.java`
- Интеграционные настройки core:
  - `core_service/src/main/resources/application.yml`
- Общие секреты/ID клиентов:
  - `.env`

## 5) Рекомендуемые env-переменные

- `SERVICE_CLIENT_ID`
- `SERVICE_CLIENT_SECRET`
- `OAUTH_FRONTEND_CLIENT_ID`
- `OAUTH_FRONTEND_CLIENT_SECRET`
- `OAUTH_FRONTEND_REDIRECT_URI`
- `JWT_PUBLIC_KEY`
- `JWT_PRIVATE_KEY`
