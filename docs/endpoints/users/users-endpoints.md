# API de usuarios (`/users`)

Documentación para la administración de usuarios.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente; por ejemplo, `http://localhost:8080`.
- Todos los endpoints de este documento requieren `Authorization: Bearer <accessToken>`.
- En las operaciones con body usar `Content-Type: application/json`.
- Los IDs son `Long` generados por la base de datos. No enviar `id` al crear un usuario.
- Las respuestas de usuario nunca incluyen la contraseña.

## Estructuras

**Body de creación o actualización**

```json
{
  "firstName": "Sofia",
  "lastName": "Martinez",
  "email": "sofia@onegym.test",
  "password": "una-contraseña-segura",
  "avatarUrl": "https://cdn.ejemplo.com/avatars/sofia.png",
  "phone": "+54 11 5555-0101",
  "address": "Av. Siempre Viva 123"
}
```

`avatarUrl`, `phone` y `address` pueden ser `null`. Todos los demás campos son obligatorios. En un `PUT`, la contraseña enviada reemplaza la anterior.

**Estructura de respuesta**

```json
{
  "id": 1,
  "firstName": "Sofia",
  "lastName": "Martinez",
  "email": "sofia@onegym.test",
  "avatarUrl": "https://cdn.ejemplo.com/avatars/sofia.png",
  "phone": "+54 11 5555-0101",
  "address": "Av. Siempre Viva 123"
}
```

## 1. Listar usuarios

```http
GET {BASE_URL}/users
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
[
  {
    "id": 1,
    "firstName": "Sofia",
    "lastName": "Martinez",
    "email": "sofia@onegym.test",
    "avatarUrl": null,
    "phone": "+54 11 5555-0101",
    "address": "Av. Siempre Viva 123"
  }
]
```

## 2. Obtener un usuario por ID

```http
GET {BASE_URL}/users/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un objeto con la estructura de respuesta indicada arriba.

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el usuario no existe.

## 3. Crear un usuario

```http
POST {BASE_URL}/users
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de creación indicado en [Estructuras](#estructuras).

**Respuesta — `201 Created`**

No devuelve body. El ID se genera en la base de datos; consultar luego `GET /users` o `GET /users/{id}`.

## 4. Actualizar un usuario

```http
PUT {BASE_URL}/users/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de creación completo. El `id` de la URL determina el usuario a modificar.

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el usuario no existe.

## 5. Cambiar solamente la contraseña

```http
PUT {BASE_URL}/users/1/password
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{
  "currentPassword": "contraseña-actual",
  "newPassword": "contraseña-nueva"
}
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `401 Unauthorized` si la contraseña actual no coincide o el usuario no existe.

## 6. Eliminar un usuario

```http
DELETE {BASE_URL}/users/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
