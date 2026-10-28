# Actividades y grupos — guía CMP móvil

Esta guía es para la app de miembros hecha con Kotlin Multiplatform y Compose Multiplatform. Cubre únicamente las acciones personales: consultar el catálogo, ver los grupos, inscribirse, solicitar ingreso, consultar las afiliaciones propias y salir de un grupo. No incluye la administración de actividades, horarios, grupos ni profesores.

Todas las rutas requieren `Authorization: Bearer <accessToken>`. El `gymId` elegido debe ser uno de los gimnasios activos devueltos por `GET /status`.

## 1. Catálogo y grupos

Para mostrar el catálogo del gimnasio seleccionado:

```http
GET /gyms/{gymId}/activities
```

La respuesta es un array. La app debe mostrar sólo actividades con `status: "ACTIVE"` e `isVisible: true`; el backend puede devolver otros estados para consumidores administrativos, por lo que el filtrado visual evita mostrar actividades internas, suspendidas o archivadas.

```kotlin
@Serializable
data class Activity(
    val id: Long,
    val gymId: Long,
    val title: String,
    val description: String? = null,
    val bannerUrl: String? = null,
    val participationMode: String,
    val status: String,
    val isVisible: Boolean,
    val defaultMaxCapacity: Int? = null,
)
```

Al abrir el detalle de una actividad `GROUP_ONLY` o `HYBRID`, cargar sus grupos:

```http
GET /gyms/{gymId}/activities/{activityId}/groups
```

```kotlin
@Serializable
data class ActivityGroup(
    val id: Long,
    val activityId: Long,
    val name: String,
    val joinPolicy: String,
    val status: String,
    val maxMembers: Int? = null,
    val scheduleIds: List<Long>,
    val activeMemberCount: Int,
)
```

Para mostrar los días y horarios de una actividad, cargar también:

```http
GET /gyms/{gymId}/activities/{activityId}/schedules
```

```kotlin
@Serializable
data class ActivitySchedule(
    val id: Long,
    val dayOfWeek: Int,
    val startsAt: String,
    val endsAt: String,
    val maxCapacity: Int? = null,
    val activeFrom: String,
    val activeUntil: String? = null,
    val instructorGymCoachIds: List<Long>,
)
```

`dayOfWeek` usa `1` para lunes y `7` para domingo. Los IDs de `instructorGymCoachIds` son referencias a entrenadores del gimnasio: si la app necesita nombres o fotos, puede cruzarlos con los coaches recibidos en `GET /{gymId}/home`.

Reglas de presentación:

- `OPEN`: la actividad admite reservas puntuales; no hace falta pertenecer a un grupo.
- `GROUP_ONLY`: el usuario sólo participa mediante uno de sus grupos.
- `HYBRID`: permite grupos y también reservas puntuales; los integrantes del grupo ya ocupan sus lugares para sus horarios.
- Ofrecer inscripción sólo si el grupo tiene `status: "ACTIVE"`.
- Si `maxMembers` es `null`, no tiene cupo máximo. Si no es nulo, hay cupo cuando `activeMemberCount < maxMembers`.
- No asumir que `scheduleIds` contienen detalles de día y hora: son identificadores. Consultar los horarios de la actividad y filtrar localmente por esos IDs para mostrar los horarios de cada grupo.

## 2. Inscribirse o solicitar acceso

Para un grupo con `joinPolicy: "OPEN"`:

```http
POST /gyms/{gymId}/activity-groups/{groupId}/memberships/me
```

No lleva body. Devuelve `201 Created` con la membresía:

```json
{
  "id": 31,
  "groupId": 8,
  "userId": 42,
  "startDate": "2026-10-28",
  "endDate": null
}
```

Para un grupo con `joinPolicy: "REQUEST_ONLY"`:

```http
POST /gyms/{gymId}/activity-groups/{groupId}/join-requests
```

No lleva body. Devuelve `201 Created`:

```json
{
  "id": 14,
  "groupId": 8,
  "userId": 42,
  "status": "PENDING",
  "requestedAt": "2026-10-28T14:30:00-03:00"
}
```

No ofrecer un segundo botón mientras se procesa la petición. Ante `409 Conflict`, recargar grupos y mostrar que ya existe una inscripción/solicitud o que no hay cupo. La app no aprueba solicitudes: esa acción es exclusiva de coaches/administradores.

## 3. Mis actividades

Para la pantalla “Mis actividades”:

```http
GET /gyms/{gymId}/me/activities
```

Sólo devuelve membresías de grupo activas. Las reservas puntuales de una clase `OPEN` o `HYBRID` no aparecen en esta lista.

```kotlin
@Serializable
data class ActivityGroupMembership(
    val id: Long,
    val groupId: Long,
    val userId: Long,
    val startDate: String,
    val endDate: String? = null,
)

@Serializable
data class MemberActivity(
    val activity: Activity,
    val group: ActivityGroup,
    val membership: ActivityGroupMembership,
)
```

Usar `membership.id` para abandonar el grupo; no usar `group.id`.

## 4. Salir de un grupo

El propio usuario puede salir de una membresía activa:

```http
DELETE /gyms/{gymId}/activity-group-memberships/{membershipId}/me
```

La respuesta es `204 No Content`, sin JSON. La API conserva el historial y libera el cupo desde la fecha actual. Tras recibir `204`, quitar el elemento del estado local y volver a pedir `GET /me/activities` cuando la pantalla se refresque.

No usar la ruta de traslado de membresía: es una herramienta administrativa para coaches, admins u owners.

## 5. Errores y estado de UI

| HTTP | Tratamiento móvil |
| --- | --- |
| `200` / `201` | Reemplazar el estado con el body recibido. |
| `204` | Confirmar la salida y recargar la lista personal. |
| `400` | Parámetro o solicitud inválida. Mostrar mensaje recuperable. |
| `401` | Renovar token una vez; si falla, limpiar sesión e ir a login. |
| `403` | El usuario ya no pertenece al gimnasio. Refrescar `/status`. |
| `404` | Actividad, grupo o membresía ya no existe; recargar catálogo/lista. |
| `409` | Conflicto funcional: sin cupo, inscripción repetida, grupo no disponible o clase en curso. Recargar y explicar el conflicto. |
| Red / `5xx` | Mantener el último estado confirmado y ofrecer reintento. |

No confirmar optimistamente una inscripción o salida como definitiva hasta recibir la respuesta del backend. Al cambiar de gimnasio, cancelar las cargas del gimnasio anterior para no mezclar actividades de distintos gimnasios.

## Checklist

- [ ] El catálogo se carga por gimnasio seleccionado.
- [ ] Sólo se muestran actividades visibles y activas al miembro.
- [ ] El botón de grupo abierto usa `POST .../memberships/me` sin body.
- [ ] El botón de grupo cerrado usa `POST .../join-requests` sin body.
- [ ] “Mis actividades” usa `GET /me/activities`, no el catálogo general.
- [ ] La salida usa `membership.id`, espera `204` y recarga el estado.
- [ ] Se manejan `401`, `403`, `404`, `409` y fallos de red de forma visible.
