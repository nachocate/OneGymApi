# Actividades, grupos y reservas

Todas las rutas requieren JWT. `ADMIN`/`OWNER` administran actividades; un `COACH` asignado a un horario de la actividad puede editarla y administrar sus grupos. Los miembros activos pueden consultar actividades visibles, unirse a grupos abiertos, solicitar acceso a grupos cerrados y reservar clases puntuales permitidas.

## Actividades

```http
GET  /gyms/{gymId}/activities
POST /gyms/{gymId}/activities
PUT  /gyms/{gymId}/activities/{activityId}
```

Body de alta/edición:

```json
{
  "title": "Boxeo funcional",
  "description": "Clases regulares con cupos.",
  "bannerUrl": "/media/images/gym-1/boxeo.jpg",
  "participationMode": "HYBRID",
  "status": "ACTIVE",
  "isVisible": true,
  "defaultMaxCapacity": 15
}
```

Modalidades: `OPEN`, `GROUP_ONLY`, `HYBRID`. Estados: `DRAFT`, `ACTIVE`, `SUSPENDED`, `ARCHIVED`.

Respuesta de `GET /gyms/{gymId}/activities` (`200 OK`):

```json
[
  {
    "id": 1,
    "gymId": 1,
    "title": "Boxeo funcional",
    "description": "Clases regulares con cupos.",
    "bannerUrl": "/media/images/gym-1/boxeo.jpg",
    "participationMode": "HYBRID",
    "status": "ACTIVE",
    "isVisible": true,
    "defaultMaxCapacity": 15
  }
]
```

## Horarios y profesores

```http
GET  /gyms/{gymId}/activities/{activityId}/schedules
POST /gyms/{gymId}/activities/{activityId}/schedules
PUT  /gyms/{gymId}/activities/{activityId}/schedules/{scheduleId}
DELETE /gyms/{gymId}/activities/{activityId}/schedules/{scheduleId}
```

```json
{
  "dayOfWeek": 2,
  "startsAt": "17:00",
  "endsAt": "18:00",
  "maxCapacity": 15,
  "activeFrom": "2026-10-27",
  "activeUntil": null,
  "instructorGymCoachIds": [3, 8]
}
```

`dayOfWeek` usa `1` lunes a `7` domingo. Cada horario requiere uno o más `gym_coach` del mismo gimnasio.

`GET` devuelve un array de `ActivityScheduleResponse`; puede ser `[]`. `PUT` recibe el mismo body completo que `POST` y devuelve `200 OK` con el horario actualizado. `DELETE` devuelve `204 No Content` únicamente si el horario no está asociado a un grupo ni tiene ocurrencias generadas. Si tiene alguna de esas dependencias, devuelve `409 Conflict` para preservar membresías, reservas e historial.

## Grupos

```http
GET  /gyms/{gymId}/activities/{activityId}/groups
POST /gyms/{gymId}/activities/{activityId}/groups
PUT  /gyms/{gymId}/activities/{activityId}/groups/{groupId}
DELETE /gyms/{gymId}/activities/{activityId}/groups/{groupId}
```

```json
{
  "name": "Boxeo Mar-Jue 17 hs",
  "joinPolicy": "OPEN",
  "status": "ACTIVE",
  "maxMembers": 10,
  "scheduleIds": [12, 13]
}
```

`joinPolicy` admite `OPEN` o `REQUEST_ONLY`. Los grupos solo existen en actividades `GROUP_ONLY` o `HYBRID`.

Respuesta de `GET /gyms/{gymId}/activities/{activityId}/groups` (`200 OK`):

```json
[
  {
    "id": 8,
    "activityId": 1,
    "name": "Boxeo Mar-Jue 17 hs",
    "joinPolicy": "OPEN",
    "status": "ACTIVE",
    "maxMembers": 10,
    "scheduleIds": [12, 13],
    "activeMemberCount": 4
  }
]
```

`PUT` recibe el mismo body completo que `POST` y devuelve el grupo actualizado. No permite cambiar `scheduleIds` si el grupo ya tiene historial de membresías, porque modificaría cómo se interpreta su historial. Tampoco permite reducir `maxMembers` por debajo de los miembros activos.

`DELETE` es una baja lógica: responde `204 No Content`, cambia el grupo a `ARCHIVED`, finaliza membresías activas al cierre de la fecha local del gimnasio y cancela solicitudes pendientes. No borra el grupo ni su historial.

## Inscripción a grupo

Grupo abierto:

```http
POST /gyms/{gymId}/activity-groups/{groupId}/memberships/me
```

Respuesta `201 Created`:

```json
{ "id": 31, "groupId": 8, "userId": 42, "startDate": "2026-10-28", "endDate": null }
```

Grupo cerrado:

```http
POST /gyms/{gymId}/activity-groups/{groupId}/join-requests
```

Respuesta `201 Created`:

```json
{ "id": 14, "groupId": 8, "userId": 42, "status": "PENDING", "requestedAt": "2026-10-28T14:30:00-03:00" }
```

## Mis actividades y salida de grupo

```http
GET    /gyms/{gymId}/me/activities
DELETE /gyms/{gymId}/activity-group-memberships/{membershipId}/me
```

`GET /me/activities` devuelve únicamente actividades donde el usuario autenticado tiene una membresía activa de grupo. Una reserva puntual de una clase `OPEN` o `HYBRID` no crea una afiliación permanente y no aparece aquí.

```json
[
  {
    "activity": {
      "id": 1,
      "gymId": 1,
      "title": "Boxeo funcional",
      "description": "Clases regulares con cupos.",
      "bannerUrl": null,
      "participationMode": "HYBRID",
      "status": "ACTIVE",
      "isVisible": true,
      "defaultMaxCapacity": 15
    },
    "group": {
      "id": 8,
      "activityId": 1,
      "name": "Boxeo Mar-Jue 17 hs",
      "joinPolicy": "OPEN",
      "status": "ACTIVE",
      "maxMembers": 10,
      "scheduleIds": [12, 13],
      "activeMemberCount": 4
    },
    "membership": {
      "id": 31,
      "groupId": 8,
      "userId": 42,
      "startDate": "2026-10-01",
      "endDate": null
    }
  }
]
```

`DELETE .../me` permite al titular de una membresía activa salir de su propio grupo. Devuelve `204 No Content` y libera el cupo desde la fecha actual; no elimina el historial.

Un coach asignado a la actividad, `ADMIN`, `OWNER` o `SUPER_ADMIN` puede aprobar una solicitud:

```http
POST /gyms/{gymId}/activity-group-join-requests/{requestId}/approve
```

La capacidad se valida nuevamente al aprobar.

## Integrantes y traslado de grupo

```http
GET  /gyms/{gymId}/activity-groups/{groupId}/memberships
POST /gyms/{gymId}/activity-group-memberships/{membershipId}/transfer
```

Body de traslado:

```json
{
  "targetGroupId": 8,
  "effectiveFrom": "2026-11-03"
}
```

El traslado conserva historial: cierra la membresía anterior el día previo y crea una nueva, dentro de una sola transacción. El grupo destino debe pertenecer a la misma actividad y tener cupo.

No se permite unirse mientras una clase del grupo está en curso. Una membresía conserva lugar en todos los horarios futuros del grupo. Si el grupo está lleno, la API responde `409`.

## Calendario y reserva puntual

```http
GET /gyms/{gymId}/activity-occurrences?from=2026-11-01&to=2026-11-30
POST /gyms/{gymId}/activity-occurrences/{occurrenceId}/reservations
DELETE /gyms/{gymId}/activity-occurrences/{occurrenceId}/reservations/me
```

La reserva puntual solo funciona para `OPEN` y `HYBRID`. En `HYBRID`, los lugares de miembros de grupo se descuentan antes de calcular los disponibles para pruebas o reservas puntuales. El rango de calendario máximo es 90 días.

Errores habituales: `400` datos inválidos, `403` falta de membresía/permisos, `404` recurso inexistente, `409` sin cupo o inscripción duplicada, `422` integridad de base de datos.
