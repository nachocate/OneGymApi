# Gestión atómica de gimnasio y miembros

Estas rutas sustituyen la composición de CRUDs genéricos desde el frontend. Todas requieren JWT.

## Crear gimnasio — `POST /gyms`

Solo `SUPER_ADMIN`. Crea en una única transacción el gimnasio, su suscripción y su primer `OWNER`; si un paso falla, no persiste ningún dato parcial.

```json
{
  "name": "Nuevo Gym",
  "localityId": 1,
  "subscription": { "startDate": "2026-10-11" },
  "owner": {
    "firstName": "Ana",
    "lastName": "Dueña",
    "email": "ana@example.test",
    "password": "una-contraseña-segura"
  }
}
```

Como alternativa a `owner`, enviar `ownerUserId` para asignar un usuario global existente. Se debe enviar exactamente una de las dos opciones. Responde `201 Created` con el gimnasio, `404` si la localidad/usuario no existe, `409` ante email duplicado y `400` ante datos inválidos.

## Miembros de un gimnasio

| Método | Path | Uso |
| --- | --- | --- |
| `POST` | `/gyms/{gymId}/members` | Asigna un usuario global existente. |
| `POST` | `/gyms/{gymId}/members/registration` | Crea usuario global y membresía en una transacción. |
| `DELETE` | `/gyms/{gymId}/members/{userId}` | Elimina únicamente la membresía, no el usuario global. |

El body para asignar un usuario existente es:

```json
{ "userId": 12, "roleName": "MEMBER" }
```

Para registrar y asignar en un paso:

```json
{
  "user": {
    "firstName": "Lucía",
    "lastName": "Pérez",
    "email": "lucia@example.test",
    "password": "una-contraseña-segura"
  },
  "roleName": "MEMBER"
}
```

`SUPER_ADMIN` puede gestionar cualquier rol. Un `OWNER` puede administrar todos los roles de su gimnasio; `ADMIN` solo roles inferiores y `COACH` solamente `MEMBER`. La eliminación del último owner es rechazada por la base de datos.
