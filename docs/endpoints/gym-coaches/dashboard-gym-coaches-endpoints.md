# Endpoints de entrenadores para dashboard

Rutas orientadas al gimnasio activo. Sustituyen el uso de `/gym-coaches` en el dashboard; el CRUD legado se conserva por compatibilidad interna.

Todas requieren JWT. La lectura requiere membresía activa. Las escrituras requieren `ADMIN`, `OWNER` o `SUPER_ADMIN` y respetan la jerarquía de roles.

## Listar

```http
GET /gyms/{gymId}/coaches/details?limit=20&offset=0
```

Responde `200 OK` con objetos `GymCoachDetailsResponse`: `id`, `user`, `coachType`, `startDate` y `endDate`.

## Registrar persona nueva como coach

```http
POST /gyms/{gymId}/coaches
Content-Type: application/json
```

```json
{
  "user": {
    "firstName": "Lucía",
    "lastName": "Gómez",
    "email": "lucia@example.test",
    "password": "contraseña-inicial-segura",
    "avatarUrl": null,
    "phone": null,
    "address": null
  },
  "coachTypeId": 2,
  "startDate": "2026-10-26",
  "endDate": null
}
```

Responde `201 Created` con el coach enriquecido. Crea atómicamente usuario, membresía `COACH` y asignación.

## Asociar usuario existente

```http
POST /gyms/{gymId}/coaches/assignments
Content-Type: application/json
```

```json
{
  "userId": 42,
  "coachTypeId": 2,
  "startDate": "2026-10-26",
  "endDate": null
}
```

Responde `201 Created`. Crea o promueve la membresía a `COACH` cuando corresponde y crea la asignación dentro de una misma transacción.

## Actualizar asignación

```http
PUT /gyms/{gymId}/coaches/{coachId}
Content-Type: application/json
```

```json
{
  "coachTypeId": 1,
  "startDate": "2026-10-01",
  "endDate": null
}
```

Responde `200 OK` con el coach actualizado. No modifica el perfil global ni la contraseña del usuario.

## Eliminar asignación

```http
DELETE /gyms/{gymId}/coaches/{coachId}
```

Responde `204 No Content`. No elimina el usuario global. Si el usuario era solamente `COACH` y no conserva otra asignación activa, su rol de gimnasio vuelve a `MEMBER`.

## Errores

- `400 Bad Request`: body, IDs o fechas inválidas.
- `401 Unauthorized`: JWT ausente, inválido o vencido.
- `403 Forbidden`: rol insuficiente o intento de administrar un rol igual/superior.
- `404 Not Found`: gym, usuario, especialidad o asignación inexistente.
- `409 Conflict`: email duplicado o asignación repetida.
