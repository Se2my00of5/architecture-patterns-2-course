# WebSocket гайд для клиента (core_service)

Этот документ описывает только realtime-работу через WebSocket/STOMP.

## 1) Что нужно знать

- WS endpoint: `/ws`
- STOMP topic для операций счёта: `/topic/accounts/{accountId}/operations`
- На `CONNECT` обязательно передавать JWT:

```text
Authorization: Bearer <access_token>
```

- Сервер пушит в topic **готовый объект `OperationResponse`** (без обёртки).

---

## 2) Права доступа на подписку

- `ROLE_EMPLOYEE` — может подписываться на любые счета.
- `ROLE_CLIENT` — только на свои счета.

Если клиент подпишется на чужой счёт, сервер отклонит подписку.

---

## 3) Минимальный пример на JS (`@stomp/stompjs`)

```js
import { Client } from '@stomp/stompjs';

const accountId = '11111111-1111-1111-1111-111111111111';
const token = '<access_token>';

const stompClient = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  reconnectDelay: 3000,
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  onConnect: () => {
    stompClient.subscribe(`/topic/accounts/${accountId}/operations`, (frame) => {
      const operation = JSON.parse(frame.body);
      console.log('operation:', operation);

      // Здесь обновляйте UI сразу из operation
      // например: добавить в начало списка операций
    });
  },
  onStompError: (frame) => {
    console.error('STOMP error:', frame.headers['message'], frame.body);
  },
  onWebSocketError: (event) => {
    console.error('WS error:', event);
  }
});

stompClient.activate();

// Позже при выходе пользователя:
// stompClient.deactivate();
```

---

## 4) Рекомендуемый клиентский поток

1. Получить JWT через ваш auth flow.
2. Подключиться к `/ws` с `Authorization: Bearer <token>`.
3. Подписаться на `/topic/accounts/{accountId}/operations`.
4. На каждое событие обновлять экран сразу из payload.
5. При реконнекте сделать REST-синхронизацию истории операций, чтобы закрыть возможные пропуски.