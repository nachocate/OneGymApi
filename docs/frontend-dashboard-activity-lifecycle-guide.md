# Ciclo de vida de horarios y grupos — Dashboard

Esta guía define qué puede mostrar el dashboard para administrar horarios y grupos de actividades. Es importante distinguir el contrato disponible de las acciones que aún no existen en la API.

## Estado actual del contrato

| Recurso | Crear | Leer | Editar | Desactivar | Eliminar |
| --- | --- | --- | --- | --- | --- |
| Actividad | Sí | Sí | Sí | Mediante `status` | No hay endpoint específico |
| Horario de actividad | Sí | Sí | Sí | No | Sí, sin dependencias |
| Grupo de actividad | Sí | Sí | Sí, con límites de historial | Mediante `DELETE` lógico | No físico |

El dashboard puede administrar horarios y grupos con las rutas documentadas abajo. La eliminación de grupo es una baja lógica: nunca debe presentarse como un borrado definitivo.

## Funcionalidad que sí debe implementar ahora

### Crear horario

```http
POST /gyms/{gymId}/activities/{activityId}/schedules
Content-Type: application/json
```

```json
{
  "dayOfWeek": 2,
  "startsAt": "17:00",
  "endsAt": "18:00",
  "maxCapacity": 15,
  "activeFrom": "2026-10-28",
  "activeUntil": null,
  "instructorGymCoachIds": [3, 8]
}
```

- `dayOfWeek`: `1` lunes a `7` domingo.
- Se requiere por lo menos un instructor activo del mismo gimnasio.
- `endsAt` debe ser posterior a `startsAt`.
- `maxCapacity: null` utiliza, si existe, el cupo general de la actividad.

Después de `201 Created`, usar el horario devuelto para asociarlo a un grupo nuevo. No inventar IDs de horarios en el cliente.

### Listar, editar y eliminar horario

```http
GET    /gyms/{gymId}/activities/{activityId}/schedules
PUT    /gyms/{gymId}/activities/{activityId}/schedules/{scheduleId}
DELETE /gyms/{gymId}/activities/{activityId}/schedules/{scheduleId}
```

`GET` es la fuente de verdad después de una recarga. `PUT` recibe el mismo body completo que el alta y devuelve el horario actualizado. Tras un `POST`, `PUT` o `DELETE` exitoso, volver a cargar la lista.

El `DELETE` solo devuelve `204` si el horario no tiene grupos asociados ni ocurrencias generadas. Si devuelve `409`, no ocultar el horario de la UI: informar que tiene dependencias y que debe conservarse para no perder historial.

### Crear y mostrar grupos

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

Un grupo puede asociarse a uno o más horarios de la misma actividad. No permitir crear grupos para una actividad `OPEN`: solo son válidos en `GROUP_ONLY` o `HYBRID`.

## Diseño de interfaz recomendado

### Horarios

En la ficha de actividad cargar `GET /schedules` y mostrar una lista junto al formulario “Agregar horario”. Cada tarjeta puede mostrar:

- día de semana y franja horaria;
- vigencia (`activeFrom` / `activeUntil`);
- cupo específico o “Usa cupo general”;
- instructores asignados.

Para usuarios con permiso, mostrar “Editar” y “Eliminar”. La eliminación debe solicitar confirmación e informar que puede ser rechazada con `409` por grupos u ocurrencias existentes. No ofrecer una baja lógica: todavía no existe esa ruta.

### Grupos

En cada grupo mostrar:

- nombre, `OPEN` o `REQUEST_ONLY`, estado y cupo;
- horarios asociados;
- integrantes y solicitudes cuando correspondan;
- acciones ya disponibles: aprobar solicitudes y trasladar integrantes.

Mostrar “Editar” y “Archivar grupo”. No usar el texto “Eliminar definitivamente”. Si el grupo tiene historial de membresías, el formulario debe bloquear cambios de horarios (`scheduleIds`), pero permite editar nombre, política, estado y cupo válido.

```http
PUT    /gyms/{gymId}/activities/{activityId}/groups/{groupId}
DELETE /gyms/{gymId}/activities/{activityId}/groups/{groupId}
```

`PUT` recibe el mismo body completo de creación. `DELETE` devuelve `204`, cambia el grupo a `ARCHIVED`, cierra membresías activas para la fecha local actual y cancela solicitudes pendientes. Conservar la tarjeta en la lista recargada con el estado `ARCHIVED`; no quitarla como si sus datos hubieran desaparecido.

## Contrato futuro recomendado

Para una futura mejora, puede evaluarse un endpoint explícito de archivado que reemplace el `DELETE` lógico actual:

```http
POST   /gyms/{gymId}/activities/{activityId}/groups/{groupId}/archive
```

La recomendación es que el último endpoint sea una baja lógica (`ARCHIVED`), no un `DELETE` físico. Debe conservar membresías, solicitudes, reservas y ocurrencias históricas. Un borrado físico solo debería permitirse si el grupo/horario no tiene relaciones y el backend lo valida transaccionalmente.

Para un horario con ocurrencias futuras, la alternativa preferida es editar `activeUntil` o archivarlo; no borrar información ya publicada o reservada.

## Permisos y errores

- `ADMIN`, `OWNER` y `SUPER_ADMIN` gestionan cualquier actividad del gimnasio.
- Un `COACH` solo puede gestionar una actividad si está asignado como instructor de alguno de sus horarios.
- `401`: renovar sesión una vez.
- `403`: no mostrar acciones de administración; no intentar refresh.
- `409`: cupo o estado incompatible; recargar datos antes de volver a intentar.
- Error de red: conservar el formulario de alta y ofrecer reintento.

## Checklist

- [ ] El dashboard crea horarios solo con IDs de `gym_coaches` del gimnasio activo.
- [ ] Los grupos usan únicamente IDs de horarios devueltos por backend.
- [ ] La lista de horarios se obtiene mediante `GET /schedules` tras cada recarga.
- [ ] Un `409` al eliminar un horario conserva la tarjeta y explica sus dependencias.
- [ ] El botón de baja de grupo se nombra “Archivar grupo” y explica que preserva historial.
- [ ] Los cambios de `scheduleIds` se bloquean visualmente si el grupo tiene historial de membresías o se maneja el `409` del backend.
- [ ] Las acciones disponibles preservan el historial de miembros y reservas.
