# API de perfil autenticado (`/profile/me`)

Documentación para consultar el perfil del usuario asociado al access token actual.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Este endpoint requiere `Authorization: Bearer <accessToken>`.
- El ID del usuario se obtiene exclusivamente del JWT; no se debe enviar un ID como parámetro ni body.
- La respuesta no expone la contraseña.

## Consultar mi perfil

```http
GET {BASE_URL}/profile/me
Authorization: Bearer <accessToken>
```

**Respuesta exitosa — `200 OK`**

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

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `id` | number | Identificador del usuario autenticado. |
| `firstName` | string | Nombre. |
| `lastName` | string | Apellido. |
| `email` | string | Correo electrónico. |
| `avatarUrl` | string \| null | URL del avatar. |
| `phone` | string \| null | Teléfono de contacto. |
| `address` | string \| null | Dirección. |

**Posibles respuestas**

- `401 Unauthorized`: el token falta, venció o no es válido.
- `404 Not Found`: el usuario asociado al token ya no existe.
