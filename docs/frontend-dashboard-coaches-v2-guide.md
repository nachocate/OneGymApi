# Administración de entrenadores — Dashboard

Esta es la guía vigente para administrar coaches. El dashboard no debe usar el CRUD legado `/gym-coaches` ni encadenar altas manuales de usuario, membresía y asignación.

## Permisos

| Acción | Rol mínimo |
| --- | --- |
| Ver coaches | Cualquier miembro activo |
| Crear, editar o eliminar | `ADMIN`, `OWNER` o `SUPER_ADMIN` |

Se respeta la jerarquía: un `ADMIN` no administra la asignación de un `ADMIN` u `OWNER`. El backend confirma esta regla; la interfaz debe manejar `403`.

## Listar coaches

```http
GET /gyms/{gymId}/coaches/details?limit=20&offset=0
Authorization: Bearer <accessToken>
```

La respuesta incluye perfil y especialidad; no hay que unir IDs en el frontend:

```json
[
  {
    "id": 15,
    "user": {
      "id": 42,
      "firstName": "Lucía",
      "lastName": "Gómez",
      "email": "lucia@example.test",
      "avatarUrl": null,
      "phone": "+54 11 5555-5555",
      "address": null
    },
    "coachType": { "id": 2, "name": "Funcional" },
    "startDate": "2026-10-01",
    "endDate": null
  }
]
```

Usar `id` como `coachId` para editar o eliminar. Usar `user.id` solo al asociar una persona ya existente.

## Datos para el formulario

```http
GET /coach-types
GET /gyms/{gymId}/members?limit=100&offset=0
```

`/coach-types` llena especialidades; miembros sirve para buscar usuarios existentes.

## Crear un coach nuevo

```http
POST /gyms/{gymId}/coaches
Authorization: Bearer <accessToken>
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
    "phone": "+54 11 5555-5555",
    "address": null
  },
  "coachTypeId": 2,
  "startDate": "2026-10-26",
  "endDate": null
}
```

Esta única request crea atómicamente el usuario global, la membresía `COACH` y la asignación. Responde `201 Created` con el coach enriquecido. Nunca guardar o mostrar la contraseña tras enviarla.

## Asociar un usuario existente

```http
POST /gyms/{gymId}/coaches/assignments
Authorization: Bearer <accessToken>
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

Si no pertenece al gimnasio, se crea su membresía `COACH`; si es `MEMBER`, se promueve a `COACH`; los roles superiores se conservan. Respuesta: `201 Created` con el coach enriquecido.

## Editar una asignación

```http
PUT /gyms/{gymId}/coaches/{coachId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "coachTypeId": 1,
  "startDate": "2026-10-01",
  "endDate": null
}
```

Responde `200 OK` con el coach actualizado. Solo modifica especialidad y fechas; no altera el perfil global ni contraseña. Para conservar historial, finalizar con `endDate` en vez de borrar.

## Eliminar una asignación

```http
DELETE /gyms/{gymId}/coaches/{coachId}
Authorization: Bearer <accessToken>
```

Responde `204 No Content`. Nunca borra el usuario global. Si no conserva otra asignación activa y su rol era exactamente `COACH`, pasa a `MEMBER`; `ADMIN` y `OWNER` mantienen su rol.

## Errores y experiencia

| Código | Acción |
| --- | --- |
| `200` / `201` | Reemplazar o agregar el objeto devuelto. |
| `204` | Quitar el item; no parsear JSON. |
| `400` | Marcar campos y fechas inválidas. |
| `401` | Refresh una vez; si falla, cerrar sesión. |
| `403` | Informar falta de permisos; no hacer refresh. |
| `404` | Recargar lista o catálogos. |
| `409` | Email o asignación duplicada; conservar formulario. |
| Error de red | Mantener formulario y ofrecer reintento. |

Deshabilitar el botón de envío mientras una operación está en curso.

## Checklist

- [ ] La tabla usa `GET /gyms/{gymId}/coaches/details`.
- [ ] Altas nuevas usan `POST /gyms/{gymId}/coaches`.
- [ ] Usuarios existentes usan `POST /gyms/{gymId}/coaches/assignments`.
- [ ] Edición usa el `coachId` de la respuesta enriquecida.
- [ ] La baja se comunica como baja de asignación, no de usuario.
- [ ] La interfaz maneja `403`, `409` y fallos de red.
