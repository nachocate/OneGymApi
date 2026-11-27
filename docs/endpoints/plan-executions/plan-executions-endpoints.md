# Seguimiento de ejecución de rutinas

Todos estos endpoints requieren `Authorization: Bearer <accessToken>`. El seguimiento registra únicamente actividad real: si no existe una ejecución para una fecha, **no significa que el miembro faltó**. Esto permite pases libres, frecuencias variables y planes sin días preasignados.

La fecha se calcula en la zona horaria configurada para el gimnasio. Cada combinación `userPlanId` + fecha local puede iniciarse una sola vez. La estructura de ejercicios se copia al comienzo: editar la planificación más tarde no modifica el historial.

## Endpoints del miembro

### Iniciar o retomar la rutina del día

```http
POST /gyms/{gymId}/me/plan-executions
Content-Type: application/json

{ "userPlanId": 1, "planDayId": 1 }
```

El usuario autenticado debe tener una membresía activa y ser el titular de `userPlanId`; `planDayId` debe pertenecer a esa planificación. Se puede iniciar hasta las `23:50` locales del gimnasio.

- `201 Created`: creó la ejecución y sus ítems snapshot.
- `200 OK`: ya existía la ejecución de esa planificación para hoy; es seguro reintentar por red.
- `400 Bad Request`: día inválido, sin ejercicios o fuera de horario.
- `403 Forbidden`: no pertenece al gimnasio o intenta usar un plan ajeno.

Respuesta:

```json
{
  "id": 1,
  "userPlanId": 1,
  "planId": 1,
  "planDayId": 1,
  "executionDate": "2026-10-27",
  "status": "IN_PROGRESS",
  "startedAt": "2026-10-27T15:00:00-03:00",
  "lastProgressAt": "2026-10-27T15:00:00-03:00",
  "completedAt": null,
  "totalItems": 4,
  "completedItems": 0,
  "progressPercent": 0,
  "items": [
    {
      "id": 1,
      "sourceBlockExerciseId": 1,
      "blockPosition": 0,
      "blockName": "Fuerza",
      "blockLaps": 3,
      "exercisePosition": 0,
      "exerciseId": 1,
      "exerciseName": "Sentadilla",
      "quantityTypeId": null,
      "repetitions": 10,
      "quantity": null,
      "isCompleted": false,
      "completedAt": null
    }
  ]
}
```

### Consultar historial propio

```http
GET /gyms/{gymId}/me/plan-executions?from=2026-10-01&to=2026-10-31
GET /gyms/{gymId}/me/plan-executions/{executionId}
```

`from` y `to` son obligatorios, inclusivos y aceptan un rango máximo de 366 días. La respuesta del listado es un array de ejecuciones. La lectura individual y el listado incluyen los `items` snapshot.

### Marcar o desmarcar un ejercicio

```http
PATCH /gyms/{gymId}/me/plan-executions/{executionId}/items/{itemId}
Content-Type: application/json

{ "completed": true }
```

Solo el miembro titular puede modificar sus ítems, hasta las `23:50` de la fecha local de la ejecución. Cada cambio devuelve la ejecución completa con `completedItems` y `progressPercent` recalculados. Al completar todos los ítems, el estado pasa a `COMPLETED` y se completa `completedAt`.

Una ejecución que permanezca en progreso después de las `23:50` pasa a `PARTIAL` con el porcentaje que tenga. El cierre se realiza de forma segura al consultar u operar sobre ejecuciones; no depende de que el móvil permanezca abierto.

## Lecturas de seguimiento para staff

Requieren `COACH`, `ADMIN`, `OWNER` o `SUPER_ADMIN` en el gimnasio.

```http
GET /gyms/{gymId}/members/{userId}/plan-executions?from=2026-10-01&to=2026-10-31
GET /gyms/{gymId}/plan-execution-adherence?from=2026-10-01&to=2026-10-31
```

El primer endpoint devuelve el detalle del historial de un miembro. El segundo devuelve un ranking agregado, ordenado por ejecuciones completadas y luego por progreso medio:

```json
[
  {
    "userId": 42,
    "completedExecutions": 12,
    "partialExecutions": 3,
    "inProgressExecutions": 0,
    "averageProgressPercent": 88
  }
]
```

No se reportan ausencias ni cumplimiento esperado: estos valores se calculan exclusivamente sobre ejecuciones que el usuario inició.
