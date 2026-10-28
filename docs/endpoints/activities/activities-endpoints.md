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

## Horarios y profesores

```http
POST /gyms/{gymId}/activities/{activityId}/schedules
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

## Grupos

```http
GET  /gyms/{gymId}/activities/{activityId}/groups
POST /gyms/{gymId}/activities/{activityId}/groups
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

## Inscripción a grupo

Grupo abierto:

```http
POST /gyms/{gymId}/activity-groups/{groupId}/memberships/me
```

Grupo cerrado:

```http
POST /gyms/{gymId}/activity-groups/{groupId}/join-requests
```

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
