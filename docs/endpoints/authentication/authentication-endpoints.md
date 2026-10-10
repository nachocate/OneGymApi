# API de autenticación (`/login` y `/auth`)

Documentación para iniciar sesión, renovar la sesión y cerrarla.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Estos endpoints no requieren header `Authorization`.
- Enviar `Content-Type: application/json` en todos los requests.
- Tras un login o refresh exitoso, guardar el `accessToken` para consumir endpoints protegidos y el nuevo `refreshToken` para renovar la sesión.
- El access token se utiliza como `Authorization: Bearer <accessToken>`.

## 1. Iniciar sesión

### `POST /login`

**Body**

```json
{
  "email": "sofia@onegym.test",
  "password": "password",
  "deviceInfo": "Web Chrome"
}
```

`deviceInfo` es opcional y permite identificar la sesión del dispositivo.

**Respuesta exitosa — `200 OK`**

```json
{
  "user": {
    "id": 1,
    "firstName": "Sofia",
    "lastName": "Martinez",
    "email": "sofia@onegym.test",
    "avatarUrl": null,
    "phone": "+54 11 5555-0101",
    "address": "Av. Siempre Viva 123"
  },
  "accessToken": "eyJ...",
  "refreshToken": "token-de-refresh...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `user` | object | Datos públicos del usuario autenticado. No incluye la contraseña. |
| `accessToken` | string | JWT para los endpoints protegidos. |
| `refreshToken` | string | Token para renovar la sesión. |
| `tokenType` | string | Actualmente siempre `Bearer`. |
| `expiresIn` | number | Duración del access token en segundos. |

**Errores:** `401 Unauthorized` si el email no existe o la contraseña no coincide.

## 2. Renovar sesión

### `POST /auth/refresh`

Este endpoint rota el refresh token. Reemplazar los dos tokens almacenados por los que lleguen en la respuesta.

**Body**

```json
{
  "refreshToken": "token-de-refresh-vigente",
  "deviceInfo": "Web Chrome"
}
```

`deviceInfo` es opcional.

**Respuesta exitosa — `200 OK`**

```json
{
  "user": {
    "id": 1,
    "firstName": "Sofia",
    "lastName": "Martinez",
    "email": "sofia@onegym.test",
    "avatarUrl": null,
    "phone": "+54 11 5555-0101",
    "address": "Av. Siempre Viva 123"
  },
  "accessToken": "eyJ...nuevo...",
  "refreshToken": "token-de-refresh-nuevo...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Errores:** `401 Unauthorized` si el refresh token venció, fue revocado o no es válido.

## 3. Cerrar sesión

### `POST /auth/logout`

Revoca el refresh token proporcionado. El access token actual deja de ser utilizable al vencer.

**Body**

```json
{ "refreshToken": "token-de-refresh-vigente" }
```

**Respuesta — `204 No Content`**

No devuelve body. Es una operación idempotente: devuelve `204` aunque el token ya no exista o estuviera revocado.
