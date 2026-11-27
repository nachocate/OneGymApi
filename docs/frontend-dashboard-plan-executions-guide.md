# Seguimiento de cumplimiento de rutinas — Dashboard

Esta guía permite al dashboard administrativo consultar cómo los miembros ejecutan las planificaciones que tienen asignadas. Es una funcionalidad de **solo lectura** para el dashboard: los avances son declarados por el miembro desde la app móvil y el staff no debe marcarlos, editarlos ni eliminarlos.

## 1. Qué mide y qué no mide

Una ejecución existe solamente cuando el miembro inició una rutina. Por eso:

- una ejecución `COMPLETED` llegó al 100 %;
- una ejecución `PARTIAL` quedó por debajo del 100 % al cerrar el día;
- una ejecución `IN_PROGRESS` sigue abierta dentro del día local del gimnasio;
- la ausencia de una fila **no es una falta** ni una rutina incumplida.

Esto es intencional: hay gimnasios con pases libres, frecuencias variables y miembros que eligen qué día entrenar. No mostrar métricas como “faltas”, “días perdidos” o “porcentaje de asistencia” con estos datos.

Cada miembro puede ejecutar una vez por día cada planificación asignada. El día se calcula con la zona horaria del gimnasio, no con la zona horaria del navegador.

## 2. Permisos y navegación

Todas las rutas requieren:

```http
Authorization: Bearer <accessToken>
```

Pueden usar esta sección `COACH`, `ADMIN`, `OWNER` y `SUPER_ADMIN`. Un `MEMBER` no debe ver el acceso en el dashboard.

Flujo sugerido:

```text
Gimnasio seleccionado
       ↓
Resumen de cumplimiento del período
       ↓
Seleccionar miembro del ranking o listado
       ↓
Historial de ejecuciones del miembro
       ↓
Detalle de rutina, bloques e ítems completados
```

Antes de cargar los datos, obtener miembros del gimnasio:

```http
GET /gyms/{gymId}/members?limit=100&offset=0
```

No usar el ranking como fuente única de miembros: solo aparecen personas que iniciaron al menos una ejecución dentro del período solicitado.

## 3. Resumen y ranking del período

```http
GET /gyms/{gymId}/plan-execution-adherence?from=2026-10-01&to=2026-10-31
```

`from` y `to` son obligatorios, inclusivos y usan `YYYY-MM-DD`. El rango máximo es de 366 días. Para una pantalla mensual, usar el primer y último día **válido** del mes seleccionado; no usar `new Date()` sin normalizar, porque el navegador puede cambiar de día antes o después que el gimnasio.

`2026-11-00` no es una fecha ISO válida y el backend la rechaza con `400 Bad Request`. No construir el último día del mes restando texto ni generando el día `00`. En JavaScript/TypeScript, calcularlo como una fecha real y formatearla con zona UTC para no desplazar el día por la zona del navegador:

```ts
function monthlyExecutionRange(year: number, month: number) {
  // month usa el formato humano: enero = 1, diciembre = 12.
  const from = new Date(Date.UTC(year, month - 1, 1))
  const to = new Date(Date.UTC(year, month, 0))
  const format = (date: Date) => date.toISOString().slice(0, 10)

  return { from: format(from), to: format(to) }
}

monthlyExecutionRange(2026, 10)
// { from: "2026-10-01", to: "2026-10-31" }

monthlyExecutionRange(2026, 11)
// { from: "2026-11-01", to: "2026-11-30" }
```

Respuesta `200 OK`:

```json
[
  {
    "userId": 42,
    "completedExecutions": 12,
    "partialExecutions": 3,
    "inProgressExecutions": 0,
    "averageProgressPercent": 88
  },
  {
    "userId": 8,
    "completedExecutions": 9,
    "partialExecutions": 1,
    "inProgressExecutions": 1,
    "averageProgressPercent": 94
  }
]
```

La API ya devuelve el ranking ordenado por:

1. `completedExecutions`, descendente.
2. `averageProgressPercent`, descendente.
3. `userId`, ascendente, para desempatar de forma estable.

No reordenar la lista si la pantalla presenta “más rutinas completadas”. Si se habilita otro criterio visual —por ejemplo, mayor progreso medio— declarar ese cambio claramente en el encabezado de la tabla.

### Campos para la interfaz

| Campo | Mostrar como | Observación |
| --- | --- | --- |
| `completedExecutions` | “Completadas” | Es el principal indicador de consistencia real. |
| `partialExecutions` | “Parciales” | No equivale a una falta. |
| `inProgressExecutions` | “En curso” | Puede ocurrir solamente en la fecha local actual. |
| `averageProgressPercent` | Barra o porcentaje | Promedio de las ejecuciones iniciadas, redondeado por el backend. |

El endpoint no devuelve nombre ni avatar. Resolver esos datos desde el listado de miembros y mantener un mapa local `userId → member`. Si un `userId` ya no se encuentra en la página cargada, mostrar “Miembro #42” y ofrecer recargar miembros; no inventar datos personales.

### Estado vacío

Un arreglo `[]` significa que nadie inició una rutina en ese rango. El texto recomendado es: “Todavía no hay ejecuciones registradas en este período”. No usar “No hubo asistencia”.

## 4. Historial de un miembro

Al seleccionar un miembro del ranking o del listado:

```http
GET /gyms/{gymId}/members/{userId}/plan-executions?from=2026-10-01&to=2026-10-31
```

Respuesta `200 OK`: arreglo de `PlanExecutionResponse`.

```json
[
  {
    "id": 73,
    "userPlanId": 12,
    "planId": 7,
    "planDayId": 31,
    "executionDate": "2026-10-18",
    "status": "PARTIAL",
    "startedAt": "2026-10-18T18:10:00-03:00",
    "lastProgressAt": "2026-10-18T19:05:00-03:00",
    "completedAt": null,
    "totalItems": 8,
    "completedItems": 4,
    "progressPercent": 50,
    "items": []
  }
]
```

La API no garantiza un orden de presentación para este arreglo. Antes de renderizar, ordenar una copia por:

1. `executionDate`, descendente.
2. `startedAt`, descendente, si hay varias planificaciones en el mismo día.

No agrupar ni deduplicar por fecha: un miembro puede ejecutar dos planificaciones distintas en una misma fecha, pero solo una vez cada una.

## 5. Detalle de una ejecución

Cada elemento del historial ya contiene `items`, por lo que normalmente no hace falta una segunda petición. Para navegación directa o una recarga puntual existe:

```http
GET /gyms/{gymId}/me/plan-executions/{executionId}
```

Esta última ruta es personal (`/me`) y no sirve para el dashboard de staff. Por eso, para el dashboard conservar el objeto obtenido del historial del miembro y abrir el detalle localmente. Si el equipo necesita una ruta directa para staff por ID, debe solicitarla al backend: no intentar reutilizar `/me` con el token de un coach.

Cada `item` guarda una foto histórica de lo programado:

```json
{
  "id": 501,
  "sourceBlockExerciseId": 99,
  "blockPosition": 0,
  "blockName": "Fuerza",
  "blockLaps": 3,
  "exercisePosition": 0,
  "exerciseId": 6,
  "exerciseName": "Press de banca",
  "quantityTypeId": null,
  "repetitions": 10,
  "quantity": null,
  "isCompleted": true,
  "completedAt": "2026-10-18T18:37:00-03:00"
}
```

Agrupar visualmente los ítems por `blockPosition` y ordenar cada grupo por `exercisePosition`. Mostrar `blockName` si existe; si es `null`, usar “Bloque {blockPosition + 1}”. Mostrar `blockLaps`, repeticiones y cantidad tal como vienen en el snapshot.

No volver a cargar la estructura actual del plan para reconstruir el detalle histórico: un coach puede haber editado la planificación después de la ejecución y sus valores actuales pueden ser distintos.

## 6. Estados y badges

| Estado | Badge sugerido | Interpretación |
| --- | --- | --- |
| `COMPLETED` | Verde: “Completada” | Todos los ítems fueron marcados. `progressPercent` debe ser 100. |
| `PARTIAL` | Ámbar: “Parcial” | El día cerró sin completar todos los ítems. |
| `IN_PROGRESS` | Azul: “En curso” | Sigue dentro de la ventana del día del gimnasio. |

Usar `progressPercent` como fuente de verdad para la barra de avance. `completedItems / totalItems` está disponible para el texto, por ejemplo: `4 de 8 ejercicios`.

## 7. Estados de pantalla y errores

| Resultado | Comportamiento del dashboard |
| --- | --- |
| `200` con datos | Mostrar ranking o historial. |
| `200` con `[]` | Mostrar estado vacío sin inferir faltas. |
| `400` | Validar formato de fechas, presencia de ambas y rango máximo de un año. |
| `401` | Renovar token una sola vez; si falla, limpiar sesión y navegar a login. |
| `403` | Ocultar la sección y mostrar que el rol actual no puede consultar cumplimiento. No hacer refresh. |
| Error de red / `5xx` | Mantener período y miembro seleccionados; ofrecer “Reintentar”. |

## 8. DTOs de referencia

```ts
type PlanExecutionStatus = "IN_PROGRESS" | "PARTIAL" | "COMPLETED"

type PlanExecutionItem = {
  id: number
  sourceBlockExerciseId: number | null
  blockPosition: number
  blockName: string | null
  blockLaps: number
  exercisePosition: number
  exerciseId: number | null
  exerciseName: string
  quantityTypeId: number | null
  repetitions: number | null
  quantity: number | null
  isCompleted: boolean
  completedAt: string | null
}

type PlanExecution = {
  id: number
  userPlanId: number
  planId: number
  planDayId: number
  executionDate: string
  status: PlanExecutionStatus
  startedAt: string
  lastProgressAt: string
  completedAt: string | null
  totalItems: number
  completedItems: number
  progressPercent: number
  items: PlanExecutionItem[]
}

type PlanExecutionSummary = {
  userId: number
  completedExecutions: number
  partialExecutions: number
  inProgressExecutions: number
  averageProgressPercent: number
}
```

## Checklist de entrega

- [ ] La sección solo aparece para `COACH`, `ADMIN`, `OWNER` o `SUPER_ADMIN`.
- [ ] Todas las consultas incluyen `from` y `to` en formato `YYYY-MM-DD`.
- [ ] El fin de mes se calcula como una fecha real; nunca se envía un día `00`.
- [ ] El estado vacío no se presenta como asistencia o falta.
- [ ] El ranking se une con los datos del miembro por `userId`.
- [ ] El historial permite varias ejecuciones de planes distintos en la misma fecha.
- [ ] La vista histórica usa `items` snapshot y no la planificación actual.
- [ ] No hay botones para editar, borrar o completar ejecuciones desde el dashboard.
- [ ] La UI maneja `401`, `403` y errores de red.
