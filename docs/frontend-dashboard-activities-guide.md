# Actividades y grupos — Dashboard

Esta guía cubre clases libres, grupos recurrentes y la modalidad híbrida.

Para saber qué acciones de edición, desactivación o eliminación están disponibles hoy para horarios y grupos, consultar la [guía de ciclo de vida](frontend-dashboard-activity-lifecycle-guide.md). Horarios y grupos ya tienen rutas de gestión; la baja de grupo es lógica y conserva historial.

## Pantallas

1. **Actividades:** listado por gimnasio y editor administrativo.
2. **Grupos:** miembros, cupo, profesores y solicitudes pendientes.
3. **Calendario:** ocurrencias entre dos fechas y lugares disponibles.
4. **Reserva:** inscripción puntual para clases `OPEN` o `HYBRID`.

## Bootstrap de calendario

```http
GET /gyms/{gymId}/activities
GET /gyms/{gymId}/activity-occurrences?from=2026-11-01&to=2026-11-30
```

El rango máximo es 90 días. Una ocurrencia muestra `groupReservedSeats`, `confirmedReservations` y `availableSeats`. No calcular cupos solo en el cliente.

## Modalidades

| Modalidad | Acción del miembro |
| --- | --- |
| `OPEN` | Reserva una fecha concreta. |
| `GROUP_ONLY` | Se une a un grupo; no puede hacer reservas puntuales. |
| `HYBRID` | Puede estar en grupo o reservar una fecha si queda lugar. |

## Grupos

Los grupos `OPEN` permiten:

```http
POST /gyms/{gymId}/activity-groups/{groupId}/memberships/me
```

Los grupos `REQUEST_ONLY` requieren:

```http
POST /gyms/{gymId}/activity-groups/{groupId}/join-requests
```

No mostrar inscripción mientras el backend informe que una clase del grupo está en curso. Un `409` significa que no hay cupo o existe una inscripción activa.

## Contratos de respuesta

El catálogo `GET /gyms/{gymId}/activities` devuelve una lista de actividades con `id`, `gymId`, `title`, `description`, `bannerUrl`, `participationMode`, `status`, `isVisible` y `defaultMaxCapacity`.

Los grupos se obtienen con `GET /gyms/{gymId}/activities/{activityId}/groups`. Cada grupo contiene `id`, `activityId`, `name`, `joinPolicy`, `status`, `maxMembers`, `scheduleIds` y `activeMemberCount`. Los ejemplos JSON completos se encuentran en la [documentación de endpoints](endpoints/activities/activities-endpoints.md).

Las altas de inscripción y solicitud devuelven body con ID real:

- `POST .../memberships/me` → `{ id, groupId, userId, startDate, endDate }`.
- `POST .../join-requests` → `{ id, groupId, userId, status, requestedAt }`.

No asumir `201` sin body: guardar el ID devuelto si la interfaz necesita referenciar la membresía.

## Gestión por coaches

Un coach asignado a la actividad puede aprobar solicitudes. `ADMIN` y `OWNER` gestionan cualquier actividad del gimnasio.

Para trasladar un miembro:

```http
POST /gyms/{gymId}/activity-group-memberships/{membershipId}/transfer
```

```json
{ "targetGroupId": 8, "effectiveFrom": "2026-11-03" }
```

Usar una fecha futura o actual; el backend valida cupo y conserva el historial. No simular el traslado solo en la UI antes de recibir `200`.

## Reserva puntual

```http
POST /gyms/{gymId}/activity-occurrences/{occurrenceId}/reservations
DELETE /gyms/{gymId}/activity-occurrences/{occurrenceId}/reservations/me
```

Tras un `201` o `204`, volver a cargar la ocurrencia/calendario. Un `409` no se reintenta: el cupo puede haberse ocupado concurrentemente.

## Estados y errores

- `401`: refresh una vez; luego login.
- `403`: no tiene membresía o no administra la actividad.
- `404`: actividad, grupo u ocurrencia inexistente.
- `409`: cupo agotado, inscripción duplicada o grupo lleno.
- `422`: relación inválida o restricción de datos.
- Error de red: conservar formularios y ofrecer reintento.
