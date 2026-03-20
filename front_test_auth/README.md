# front_test_auth

Минимальный фронт для ручной проверки OAuth2 авторизации и refresh flow.

## Запуск

```bash
cd front_test_auth
npm start
```

Откройте: `http://localhost:3000`

## Что умеет

- Стартует OAuth2 Authorization Code + PKCE flow (`/oauth2/authorize`)
- Обменивает `code` на токены (`/oauth2/token`)
- Обновляет `access_token` через `refresh_token`
- Вызывает защищенный endpoint `GET /api/auth/me`
- Показывает payload JWT

## Базовые настройки по умолчанию

- Base URL: `http://localhost:1115`
- Client ID: `frontend-app`
- Client Secret: `frontend-app-secret`
- Redirect URI: `http://localhost:3000/login/oauth2/code/frontend-app`
- Scope: `api`

`frontend-app` настроен как confidential клиент в `user_service`; для него должен выдаваться `refresh_token`.
