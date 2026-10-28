# Ejecución de planes asignados — guía CMP móvil

Esta guía es para la aplicación de miembros en Kotlin Multiplatform y Compose Multiplatform. Describe cómo permitir que un usuario inicie, retome y registre el progreso de una rutina asignada desde Android o iOS.

La app registra la intención declarada por el usuario; no intenta comprobar que realizó físicamente los ejercicios. El registro tampoco es asistencia: no iniciar una rutina no equivale a una falta.

## 1. Reglas de negocio que debe respetar la app

- Una sesión corresponde a una combinación de **plan asignado** (`userPlanId`) y día local del gimnasio.
- El mismo plan asignado puede iniciarse solo una vez por día. Si el móvil reintenta el inicio, el backend devuelve la sesión existente en lugar de crear un duplicado.
- Un usuario puede tener dos planes asignados y ejecutar una sesión de cada uno en el mismo día.
- Solo se permite iniciar o modificar hasta las `23:50` de la zona horaria configurada para el gimnasio.
- Al completar todos los ítems, la sesión pasa a `COMPLETED` (100 %). Una sesión que no llegue al 100 % antes del cierre pasa a `PARTIAL`.
- La estructura de la rutina se copia al iniciar la sesión. Nunca reconstruir una sesión histórica consultando la planificación actual: esta puede haber cambiado.

## 2. Datos previos: planes activos y el identificador correcto

Primero cargar las rutinas activas del gimnasio elegido:

```http
GET /plan/{gymId}/me
Authorization: Bearer <accessToken>
```

Cada elemento de `plans` incluye dos IDs diferentes:

```json
{
  "userPlanId": 12,
  "id": 7,
  "name": "Fuerza inicial",
  "weeks": [
    {
      "id": 31,
      "days": [{ "id": 44, "number": 1 }]
    }
  ]
}
```

| Campo | Uso |
| --- | --- |
| `userPlanId` | Identifica la asignación de ese plan a la persona. Es obligatorio para iniciar la sesión. |
| `id` | Identifica el plan base. Usarlo para UI o navegación, pero **no** como `userPlanId`. |
| `weeks[].days[].id` | Es `planDayId`, el día que el usuario selecciona para ejecutar. |

Si `plans` es `[]`, mostrar “No tenés rutinas activas” y no mostrar el botón de inicio.

## 3. Flujo de pantalla recomendado

```text
Elegir gimnasio activo
        ↓
GET /plan/{gymId}/me
        ↓
Elegir plan y día
        ↓
POST /me/plan-executions
        ↓
Pantalla de sesión con los ítems snapshot
        ↓
PATCH por cada ejercicio marcado/desmarcado
        ↓
Mostrar porcentaje y estado actualizado por backend
```

Al volver a abrir la app, consultar el historial del día para encontrar una sesión ya iniciada antes de mostrar “Comenzar rutina”. La app no debe crear una sesión local falsa.

## 4. Iniciar o retomar una sesión

```http
POST /gyms/{gymId}/me/plan-executions
Content-Type: application/json
Authorization: Bearer <accessToken>

{
  "userPlanId": 12,
  "planDayId": 44
}
```

- `201 Created`: se creó una nueva sesión.
- `200 OK`: ya existía una sesión para ese plan asignado hoy; abrirla como continuación.

Ambas respuestas contienen el mismo DTO de sesión. No tratar `200` como un error ni crear una segunda pantalla de sesión.

```kotlin
@Serializable
data class StartPlanExecutionRequest(
    val userPlanId: Long,
    val planDayId: Long,
)

@Serializable
data class PlanExecution(
    val id: Long,
    val userPlanId: Long,
    val planId: Long,
    val planDayId: Long,
    val executionDate: String,
    val status: String,
    val startedAt: String,
    val lastProgressAt: String,
    val completedAt: String? = null,
    val totalItems: Int,
    val completedItems: Int,
    val progressPercent: Int,
    val items: List<PlanExecutionItem>,
)

@Serializable
data class PlanExecutionItem(
    val id: Long,
    val sourceBlockExerciseId: Long? = null,
    val blockPosition: Int,
    val blockName: String? = null,
    val blockLaps: Int,
    val exercisePosition: Int,
    val exerciseId: Long? = null,
    val exerciseName: String,
    val quantityTypeId: Long? = null,
    val repetitions: Int? = null,
    val quantity: Double? = null,
    val isCompleted: Boolean,
    val completedAt: String? = null,
)
```

## 5. Actualizar progreso por ejercicio

El backend calcula progreso y estado. Al tocar un checkbox, botón “Hecho” o “Deshacer”, enviar el valor final deseado, no un comando de toggle:

```http
PATCH /gyms/{gymId}/me/plan-executions/{executionId}/items/{itemId}
Content-Type: application/json

{ "completed": true }
```

La respuesta devuelve la sesión completa. Reemplazar el estado local con esa respuesta, especialmente `completedItems`, `progressPercent`, `status` e `items`.

```kotlin
@Serializable
data class UpdatePlanExecutionItemRequest(val completed: Boolean)

suspend fun updateItem(
    gymId: Long,
    executionId: Long,
    itemId: Long,
    completed: Boolean,
): PlanExecution = client.patch("$baseUrl/gyms/$gymId/me/plan-executions/$executionId/items/$itemId") {
    contentType(ContentType.Application.Json)
    setBody(UpdatePlanExecutionItemRequest(completed))
}.body()
```

No incrementar ni decrementar el porcentaje localmente como fuente de verdad. Las respuestas pueden llegar fuera de orden si se presionan ítems rápido: serializar las actualizaciones de una misma sesión, o ignorar una respuesta cuya versión de UI ya no sea actual.

## 6. Retomar e historial

```http
GET /gyms/{gymId}/me/plan-executions?from=2026-10-27&to=2026-10-27
GET /gyms/{gymId}/me/plan-executions?from=2026-10-01&to=2026-10-31
```

- Ambos parámetros son obligatorios, inclusivos y usan `YYYY-MM-DD`.
- El rango máximo es 366 días.
- El resultado es un array. Puede contener más de una sesión en una misma fecha si el usuario ejecutó planes asignados distintos.
- Para reanudar, buscar la sesión cuyo `userPlanId` coincida con el plan elegido y cuyo estado sea `IN_PROGRESS`.

La ruta de detalle también existe para el usuario autenticado:

```http
GET /gyms/{gymId}/me/plan-executions/{executionId}
```

## 7. Presentación Compose

Estado sugerido en `commonMain`:

```kotlin
sealed interface RoutineSessionState {
    data object Idle : RoutineSessionState
    data object Starting : RoutineSessionState
    data class Active(val execution: PlanExecution) : RoutineSessionState
    data class Error(val message: String, val canRetry: Boolean) : RoutineSessionState
}
```

La pantalla puede mostrar:

- barra `progressPercent / 100f`;
- texto `"${completedItems} de ${totalItems} ejercicios"`;
- bloques agrupados por `blockPosition` y ejercicios ordenados por `exercisePosition`;
- badge de estado: `IN_PROGRESS`, `PARTIAL` o `COMPLETED`;
- `blockName ?: "Bloque ${blockPosition + 1}"`, vueltas, repeticiones y cantidad del snapshot.

Deshabilitar la edición si el estado no es `IN_PROGRESS`. Si el backend devuelve `400` al intentar editar, recargar el detalle: probablemente la ventana diaria cerró y la sesión pasó a `PARTIAL`.

## 8. Offline, reintentos y errores

No hay sincronización offline en este contrato inicial. Para evitar que el estado visual mienta:

- no confirmar definitivamente un ítem hasta recibir la respuesta del `PATCH`;
- durante una mutación, deshabilitar temporalmente ese ítem o mostrar un indicador de envío;
- si falla por red, restaurar el valor confirmado anterior y ofrecer reintentar;
- un reintento de `POST` es seguro: devuelve la sesión existente (`200`) si la primera solicitud sí llegó;
- no reintentar automáticamente un `PATCH` después de un fallo ambiguo; primero recargar el detalle para conocer el estado confirmado.

| HTTP | Tratamiento |
| --- | --- |
| `200` / `201` | Actualizar estado con el body recibido. |
| `400` | Request inválida, día ajeno al plan o ventana diaria cerrada. Mostrar mensaje y recargar. |
| `401` | Ejecutar una única renovación de token. Si falla, limpiar sesión e ir a login. |
| `403` | El usuario no tiene membresía vigente o intenta acceder a un plan ajeno. No hacer refresh. |
| `404` | La sesión o ítem ya no existe. Volver al historial y recargar. |
| Error de red | Mantener el último estado confirmado y ofrecer reintento explícito. |

## Checklist de entrega

- [ ] Se usa `userPlanId` de `GET /plan/{gymId}/me`, no el `id` del plan.
- [ ] La app envía siempre `planDayId` de la estructura recibida.
- [ ] `200` al iniciar se trata como una sesión existente válida.
- [ ] El porcentaje se toma de la respuesta del backend.
- [ ] La pantalla agrupa los ítems snapshot sin consultar la planificación actual para el historial.
- [ ] No se informa una falta si no existe una ejecución.
- [ ] Los `PATCH` de una sesión no compiten entre sí ni dejan cambios optimistas sin confirmar.
- [ ] Se manejan `400`, `401`, `403`, `404` y fallos de red.
