# Guía CMP móvil — consumo de OneGym API

Esta guía es para la aplicación móvil Kotlin Multiplatform / Compose Multiplatform de miembros. La app inicia sesión y **consume datos**; no implementa pantallas ni llamados para crear, modificar o eliminar gimnasios, usuarios, rutinas, evaluaciones o membresías.

La referencia completa de paths y payloads está en [api-endpoints.md](api-endpoints.md).

Para implementar el seguimiento de cada sesión de rutina, usar la guía específica: [frontend-cmp-plan-executions-guide.md](frontend-cmp-plan-executions-guide.md).

Para el catálogo, inscripción y salida personal de actividades, usar [frontend-cmp-activities-guide.md](frontend-cmp-activities-guide.md).

## Alcance de la aplicación móvil

La app móvil debe permitir al usuario autenticado:

- iniciar, renovar y cerrar sesión;
- consultar su perfil;
- conocer los gimnasios a los que pertenece y seleccionar uno;
- consultar información del gimnasio seleccionado, sus coaches, noticias y horarios cuando estén disponibles;
- consultar las rutinas activas preparadas para él por los coaches;
- consultar sus propias evaluaciones y tests cuando esas pantallas estén habilitadas;
- iniciar una rutina asignada y registrar el avance de sus ejercicios durante el día.
- consultar actividades del gimnasio y administrar únicamente sus propias inscripciones o solicitudes de ingreso a grupos.

No debe mostrar acciones administrativas ni consumir endpoints `POST`, `PUT` o `DELETE` de recursos de negocio, salvo los endpoints personales de ejecución de rutina y actividades documentados aquí.

## 1. Organización CMP recomendada

```text
commonMain/
  network/      OneGymApi, Ktor Client, DTOs y parseo de errores
  session/      SessionRepository, TokenStorage, SessionState
  profile/      ProfileRepository y estado de perfil
  gyms/         GymRepository, gimnasio seleccionado y datos de inicio
  routines/     RoutineRepository y estado de rutinas

androidMain/    Ktor engine y almacenamiento seguro Android
iosMain/        Ktor engine y almacenamiento seguro iOS/Keychain
```

Las pantallas Compose no deben llamar directamente a `HttpClient`: deben consumir `StateFlow` expuestos por repositorios o ViewModels/Presenters compartidos.

## 2. Sesión y tokens

Endpoints públicos:

```text
POST /login
POST /auth/refresh
POST /auth/logout
```

1. Después de `POST /login`, guardar `accessToken`, `refreshToken` y el usuario público.
2. Agregar `Authorization: Bearer <accessToken>` a cada request protegida.
3. Frente a un `401`, ejecutar una única renovación concurrente con `POST /auth/refresh`.
4. La respuesta rota ambos tokens: reemplazar **accessToken y refreshToken**.
5. Reintentar una sola vez la request original.
6. Si el refresh devuelve `401`, borrar la sesión local y navegar a login.

Un `403` no es una sesión vencida: indica que el usuario no tiene permiso o ya no cuenta con una membresía activa. No renovar tokens en ese caso.

```kotlin
interface TokenStorage {
    suspend fun read(): SessionTokens?
    suspend fun save(tokens: SessionTokens)
    suspend fun clear()
}

data class SessionTokens(
    val accessToken: String,
    val refreshToken: String
)
```

Usar almacenamiento seguro por plataforma: Keystore/EncryptedSharedPreferences en Android y Keychain en iOS. No incluir tokens en logs, analytics, navegación ni estados de UI serializables.

## 3. Arranque de la aplicación

Con una sesión válida, cargar en paralelo:

```http
GET /profile/me
GET /status
```

`GET /status` es la fuente de verdad para las membresías activas. El usuario puede estar afiliado a más de un gimnasio y tener distinto rol en cada uno.

```kotlin
@Serializable
data class UserStatusResponse(
    val gymCount: Int,
    val systemRoles: List<String>,
    val gyms: List<GymSubscription>
)

@Serializable
data class GymSubscription(
    val gym: Gym,
    val role: UserRole
)
```

Guardar `selectedGymId` en el estado de la aplicación, verificando que siempre exista dentro de `status.gyms`. Si la respuesta cambia y el seleccionado ya no está, seleccionar otro gimnasio disponible o mostrar el estado “sin gimnasios activos”.

## 4. Datos que consume cada pantalla

| Pantalla | Endpoint | Uso |
| --- | --- | --- |
| Perfil | `GET /profile/me` | Datos públicos del usuario autenticado. |
| Selector de gimnasios | `GET /status` | Membresías activas y rol por gimnasio. |
| Inicio de gimnasio | `GET /{gymId}/home` | Información del gym y coaches activos. |
| Noticias | `GET /{gymId}/news` | Noticias ordenadas de más reciente a más antigua. |
| Mis rutinas | `GET /plan/{gymId}/me` | Planes activos del usuario y toda su estructura. |
| Mis evaluaciones | `GET /evaluations?userId={currentUserId}` | Evaluaciones del usuario autenticado. |
| Mis tests | `GET /user-tests?userId={currentUserId}` | Tests del usuario autenticado. |
| Ejecución de rutina | `POST /gyms/{gymId}/me/plan-executions` | Inicia o retoma la rutina elegida para hoy. |
| Progreso e historial | `PATCH`/`GET /gyms/{gymId}/me/plan-executions...` | Marca ejercicios y consulta ejecuciones reales. |
| Actividades | `GET /gyms/{gymId}/activities`, `GET .../groups` y `GET .../schedules` | Catálogo, grupos y horarios disponibles del gimnasio. |
| Mis actividades | `GET /gyms/{gymId}/me/activities` | Grupos activos a los que pertenece el usuario. |
| Inscripción personal | `POST .../memberships/me`, `POST .../join-requests`, `DELETE .../me` | Unirse, solicitar acceso o salir del propio grupo. |

## 6. Ejecución de rutina personal

La app no debe inferir asistencia: si el usuario no inicia una rutina, no hay ningún registro que mostrar como falta. Al presionar “Comenzar rutina”, usar `userPlanId` recibido en cada elemento de `GET /plan/{gymId}/me` y el `planDayId` elegido. No usar `plan.id` como `userPlanId`:

```http
POST /gyms/{gymId}/me/plan-executions
{ "userPlanId": 1, "planDayId": 1 }
```

El endpoint es idempotente para el mismo plan y día: `201` significa que comenzó una ejecución y `200` que se retomó la existente. Guardar el `execution.id` recibido. Cada vez que el usuario marque o desmarque un ejercicio, enviar:

```http
PATCH /gyms/{gymId}/me/plan-executions/{executionId}/items/{itemId}
{ "completed": true }
```

La respuesta trae el porcentaje recalculado. No calcularlo ni persistirlo del lado móvil. Se puede editar hasta las `23:50` de la zona del gimnasio; luego una ejecución incompleta quedará `PARTIAL`. Para el historial usar siempre un rango explícito:

```http
GET /gyms/{gymId}/me/plan-executions?from=2026-10-01&to=2026-10-31
```

La referencia completa está en [plan-executions-endpoints.md](endpoints/plan-executions/plan-executions-endpoints.md).

Usar el `gymId` elegido sólo si está incluido en `GET /status`. Las rutas de inicio, noticias y plan propio devuelven `403` cuando la membresía no está vigente.

## 5. Modelos de lectura relevantes

Los DTOs deben vivir en `commonMain`, anotados con `@Serializable`, y usar exactamente los nombres camelCase del backend.

```kotlin
@Serializable
data class UserResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatarUrl: String?,
    val phone: String?,
    val address: String?
)

@Serializable
data class Gym(
    val id: Long,
    val name: String,
    val description: String?,
    val localityId: Long,
    val address: String?,
    val phone: String?,
    val logoUrl: String?,
    val bannerUrl: String?
)
```

Las fechas de rutinas, evaluaciones y noticias se reciben como strings ISO-8601. Mantener el parsing de fechas en un único adaptador compartido; no asumir la zona horaria del dispositivo al interpretar un instante con `Z` u offset.

## 6. Ktor Client y estado de interfaz

Configurar Ktor Client en `commonMain` con JSON y un validador de respuestas. El engine se provee por plataforma.

```kotlin
val client = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
}
```

Representar carga, contenido y error de forma explícita:

```kotlin
sealed interface ScreenState<out T> {
    data object Loading : ScreenState<Nothing>
    data class Content<T>(val value: T) : ScreenState<T>
    data class Error(val message: String, val retryable: Boolean) : ScreenState<Nothing>
}
```

Al cambiar el gimnasio seleccionado, cancelar o invalidar las cargas vinculadas al gym anterior y pedir los datos del nuevo. No mezclar noticias, coaches o rutinas de gimnasios distintos en el mismo estado.

## 7. Manejo de errores en móvil

| HTTP | Comportamiento esperado |
| --- | --- |
| `400` | Mostrar que la solicitud no es válida; reportar a desarrollo si surge en una pantalla sólo de lectura. |
| `401` | Renovar token una vez; si falla, cerrar sesión. |
| `403` | Refrescar `GET /status`; informar que ya no tiene acceso al gimnasio o recurso. |
| `404` | Quitar el recurso de pantalla o volver a la lista anterior. |
| `409` | Mostrar conflicto funcional si aparece. |
| `5xx` / fallo de red | Mostrar estado recuperable con botón de reintento. |

Los errores controlados pueden tener esta forma:

```json
{ "code": "conflict", "message": "Mensaje legible" }
```

Nunca mostrar al usuario final una excepción, un stack trace, un token o el body crudo de una respuesta.

## 8. Restricciones que la app debe respetar

- No asumir que una persona pertenece a un solo gimnasio.
- No asumir que un usuario tiene permiso administrativo aunque su rol se reciba en `/status`; la app móvil es de lectura.
- No consultar recursos de otro usuario: usar el `id` de `/profile/me` para evaluaciones y tests propios.
- No intentar crear/modificar/eliminar evaluaciones, tests, planes, membresías o registros.
- `GET /plan/{gymId}/me` puede devolver `plans: []`; es un estado válido y debe tener una pantalla vacía amable.
- Los horarios de negocio son informativos. Aún no existe un listado móvil filtrado por gimnasio; no diseñar la pantalla de horarios hasta acordar ese endpoint con backend.

## Checklist de integración móvil

- [ ] DTOs y repositorios compartidos en `commonMain`.
- [ ] Storage seguro de tokens por plataforma.
- [ ] Refresh concurrente y reintento único ante `401`.
- [ ] Perfil y membresías cargados tras restaurar sesión.
- [ ] Selector de gimnasio basado exclusivamente en `GET /status`.
- [ ] Datos de home, noticias y rutina recargados al cambiar de gimnasio.
- [ ] Evaluaciones y tests consultados sólo con el usuario autenticado.
- [ ] Estados vacíos para “sin gimnasios” y “sin rutinas”.
- [ ] Manejo visible de red, `401`, `403` y `404`.
