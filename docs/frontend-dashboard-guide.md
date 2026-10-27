# Guía de integración — Dashboard OneGym

Esta es la guía operativa para el dashboard administrativo. Describe el contrato vigente de la API, incluidos los cambios de roles, membresías, gimnasios, ejercicios, rutinas, evaluaciones y tests. Para cada payload completo consultar también el [índice de endpoints](api-endpoints.md). La visualización del árbol de una rutina se detalla en la [guía de estructura de planificaciones](frontend-dashboard-planification-structure-guide.md), la edición segura en la [guía de actualización de planificaciones](frontend-dashboard-planification-update-guide.md), la gestión de entrenadores en la [guía actualizada de coaches](frontend-dashboard-coaches-v2-guide.md), los catálogos globales en la [guía de roles y catálogos](frontend-dashboard-global-catalogs-guide.md), la consulta de evaluaciones en la [guía de evaluaciones](frontend-dashboard-evaluations-guide.md) y la gestión de mediciones en la [guía de tests](frontend-dashboard-user-tests-guide.md). La aplicación móvil de miembros tiene una guía distinta: [frontend-cmp-integration-guide.md](frontend-cmp-integration-guide.md).

## 1. Convenciones de cliente HTTP

- La API no tiene prefijo de versión actualmente. Ejemplo local: `http://localhost:8080`.
- Todas las rutas de esta guía, salvo `POST /login`, `POST /auth/refresh` y `POST /auth/logout`, requieren `Authorization: Bearer <accessToken>`.
- Enviar `Content-Type: application/json` en toda request con body JSON (`POST` y `PUT`). No es necesario en `GET` ni `DELETE`.
- Los IDs son `Long` generados por el servidor. No enviar `id` en un alta; en una actualización el ID de la URL es el que identifica al recurso.
- Las fechas sin hora usan `YYYY-MM-DD`; las fechas/hora usan ISO-8601 con zona, por ejemplo `2026-10-21T12:00:00Z`.

Centralizar el cliente HTTP. Debe agregar el bearer token, serializar JSON y transformar errores de red y códigos HTTP en un estado visible de la aplicación.

## 2. Sesión

### Login

```http
POST /login
Content-Type: application/json
```

```json
{
  "email": "admin@example.test",
  "password": "contraseña",
  "deviceInfo": "dashboard-web"
}
```

Respuesta `200 OK`:

```json
{
  "user": {
    "id": 7,
    "firstName": "Ana",
    "lastName": "Pérez",
    "email": "admin@example.test",
    "avatarUrl": null,
    "phone": null,
    "address": null
  },
  "accessToken": "<jwt>",
  "refreshToken": "<opaque-token>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Guardar los dos tokens de forma segura. Nunca registrar tokens ni contraseñas.

### Refresh y logout

- Ante un `401`, intentar **una única vez** `POST /auth/refresh` con `{ "refreshToken": "...", "deviceInfo": "dashboard-web" }`.
- La respuesta rota ambos tokens: reemplazar `accessToken` y `refreshToken` antes de reintentar la request original.
- Si el refresh devuelve `401`, o el reintento vuelve a devolver `401`, limpiar la sesión y volver a login.
- Para salir, llamar `POST /auth/logout` con `{ "refreshToken": "..." }`. Su respuesta es `204`; limpiar la sesión incluso si hay un fallo de red.
- Un `403` significa falta de autorización, no una sesión vencida: no intentar refresh.

## 3. Bootstrap, gimnasio activo y roles

Luego del login, cargar en paralelo:

```http
GET /profile/me
GET /status
```

`/profile/me` devuelve el perfil público. `/status` es la fuente de verdad para roles y membresías activas:

```json
{
  "gymCount": 2,
  "systemRoles": ["SUPER_ADMIN"],
  "gyms": [
    {
      "gym": { "id": 1, "name": "Forze Gym", "localityId": 1 },
      "role": { "id": 4, "name": "OWNER" }
    }
  ]
}
```

- Usar `gyms` como selector de gimnasio y conservar el `gymId` activo en el estado de la UI.
- Un usuario puede tener distintos roles en varios gimnasios. No inferir un rol global desde el primer gimnasio.
- Usar `systemRoles.includes("SUPER_ADMIN")` para la navegación de plataforma. `/profile/me` no contiene roles.
- Si una ruta de un gimnasio devuelve `403`, refrescar `/status` y retirar esa membresía del selector si ya no está vigente.

Roles canónicos de una membresía: `MEMBER`, `COACH`, `ADMIN`, `OWNER`. El único rol de sistema actual es `SUPER_ADMIN`.

| Capacidad | MEMBER | COACH | ADMIN | OWNER | SUPER_ADMIN |
| --- | --- | --- | --- | --- | --- |
| Consultar datos del gimnasio, noticias, planes y ejercicios | Sí | Sí | Sí | Sí | Sí |
| Gestionar rutinas, ejercicios, evaluaciones y tests | No | Sí | Sí | Sí | Sí |
| Gestionar información, horarios, noticias y coaches del gym | No | No | Sí | Sí | Sí |
| Asignar o quitar miembros | Puede quitarse a sí mismo | Solo `MEMBER` | Roles inferiores | Todos los roles | Todos los gimnasios |
| Crear/eliminar gimnasios y catálogos globales | No | No | No | No | Sí |

La visibilidad de botones es una ayuda de UX; el backend siempre decide la autorización.

## 4. Lecturas que debe usar el dashboard

Para el gimnasio seleccionado, usar estas rutas en lugar de filtrar listados globales en el navegador:

| Path | Rol mínimo | Uso |
| --- | --- | --- |
| `GET /{gymId}/home` | MEMBER | Resumen del gimnasio y coaches vigentes. |
| `GET /{gymId}/news` | MEMBER | Noticias del gimnasio. |
| `GET /plan/{gymId}/me` | MEMBER | Planes activos del usuario autenticado. |
| `GET /gyms/{gymId}/coaches?limit=20&offset=0` | MEMBER | Asignaciones de coaches. |
| `GET /gyms/{gymId}/business-hours?limit=20&offset=0` | MEMBER | Horarios informativos. |
| `GET /gyms/{gymId}/plans?limit=20&offset=0` | MEMBER | Planes del gimnasio. |
| `GET /gyms/{gymId}/exercises?limit=20&offset=0` | MEMBER | Ejercicios del gym y globales disponibles. |
| `GET /gyms/{gymId}/members?limit=20&offset=0` | COACH | Miembros, perfil público, rol y vigencia. |

Los cinco listados paginados aceptan `limit` entre `1` y `100` (por defecto `20`) y `offset` mayor o igual a `0` (por defecto `0`). Responden arreglos; un arreglo vacío indica que no quedan elementos para ese offset. Los horarios son informativos: no habilitan ni bloquean operaciones.

Los endpoints globales `GET /gyms`, `GET /users`, `GET /plans`, `GET /exercises` y similares son administrativos o de plataforma; no deben ser la fuente de datos habitual del gimnasio activo.

## 5. Flujos de administración

### Crear un gimnasio

Solo `SUPER_ADMIN`. `POST /gyms` crea atómicamente el gimnasio, la suscripción inicial y el primer `OWNER`. No existe un flujo válido para crear un gimnasio sin owner.

```json
{
  "name": "Nuevo Gym",
  "description": null,
  "localityId": 1,
  "address": null,
  "phone": null,
  "logoUrl": null,
  "bannerUrl": null,
  "subscription": {
    "startDate": "2026-10-21",
    "expiresAt": null,
    "isActive": true
  },
  "owner": {
    "firstName": "Ana",
    "lastName": "Dueña",
    "email": "ana@example.test",
    "password": "contraseña-segura",
    "avatarUrl": null,
    "phone": null,
    "address": null
  }
}
```

Como alternativa, enviar `ownerUserId` en lugar de `owner`. Debe enviarse exactamente una de ambas opciones. La respuesta es `201 Created` con el `Gym` creado.

Para editar un gimnasio, `PUT /gyms/{gymId}` recibe el objeto `Gym` completo: `name`, `description`, `localityId`, `address`, `phone`, `logoUrl` y `bannerUrl`. No enviar `schedule`: los horarios viven en `GymBusinessHour`.

### Miembros y owners

No usar el CRUD legado `/user-gyms` desde el dashboard. Usar:

| Acción | Endpoint | Body |
| --- | --- | --- |
| Listar miembros | `GET /gyms/{gymId}/members` | — |
| Asignar usuario existente | `POST /gyms/{gymId}/members` | `{ "userId": 12, "roleName": "MEMBER" }` |
| Crear usuario y asignarlo | `POST /gyms/{gymId}/members/registration` | `{ "user": { ... }, "roleName": "MEMBER" }` |
| Quitar del gimnasio | `DELETE /gyms/{gymId}/members/{userId}` | — |

La baja elimina únicamente la membresía, nunca el usuario global. El propio miembro puede salir del gimnasio. El backend rechaza quitar el último `OWNER`.

### Perfil y contraseña

- `PUT /users/{userId}` actualiza perfil público. Body: `firstName`, `lastName`, `email`, `avatarUrl`, `phone`, `address`. **No incluye `password`**.
- `PUT /users/{userId}/password` solo puede hacerlo el titular, con `{ "currentPassword": "...", "newPassword": "..." }`.
- El CRUD global de `/users` es de `SUPER_ADMIN` y no reemplaza las operaciones de membresía.

### Coaches, horarios y noticias

- `POST|PUT|DELETE /gym-coaches` requiere `ADMIN`, `OWNER` o `SUPER_ADMIN`; el modelo contiene `userId`, `gymId`, `coachTypeId`, `startDate`, `endDate`.
- Para implementar la pantalla y sus flujos de alta, edición y finalización, seguir la [guía de coaches](frontend-dashboard-coaches-guide.md).
- `POST|PUT|DELETE /gym-business-hours` requiere `ADMIN`, `OWNER` o `SUPER_ADMIN`. Cada horario contiene `gymId`, `dayOfWeek` (`1` lunes a `7` domingo), `opensAt` y `closesAt` (`HH:mm:ss`).
- `POST|PUT|DELETE /news` requiere `ADMIN`, `OWNER` o `SUPER_ADMIN` y el body contiene `gymId`, `title`, `description`, `date`, `imageUrl`.

## 6. Rutinas, ejercicios y datos de entrenamiento

Una rutina se administra mediante las rutas atómicas de planificación; los CRUD individuales de `plans`, `weeks`, `days`, `blocks` y `block-exercises` permanecen como rutas técnicas/legadas. Las escrituras requieren `COACH` o superior dentro del gimnasio propietario. No permitir que la UI mueva una entidad existente a otro gimnasio mediante `PUT`.

Para el flujo de administración, la jerarquía y las limitaciones actuales del contrato, seguir la [guía de planes](frontend-dashboard-plans-guide.md).

Para el editor visual completo y el contrato de API pendiente que lo habilita, seguir la [especificación de planificaciones](frontend-dashboard-planification-builder-guide.md).

| Recurso | Campos clave de escritura |
| --- | --- |
| `Plan` | `name`, `gymId`, `planTypeId`, `planRootId` |
| `Week` | `number`, `planId` |
| `Day` | `number`, `weekId` |
| `Block` | `position`, `name`, `dayId`, `laps` |
| `BlockExercise` | `blockId`, `exerciseId`, `quantityTypeId`, `position`, `repetitions`, `quantity` |

Para ejercicios:

- Un ejercicio global tiene `gymId: null` e `isVisibleGlobal: true`; solo lo gestiona `SUPER_ADMIN`.
- Un ejercicio del gimnasio debe incluir su `gymId`; lo gestiona `COACH` o superior de ese gimnasio.
- La combinación `gymId: null` e `isVisibleGlobal: false` es inválida y devuelve `400`.
- Para el selector de ejercicios de una rutina, usar `GET /gyms/{gymId}/exercises`, no el listado global.
- Para la pantalla de administración y sus flujos de alta, edición y baja, seguir la [guía de ejercicios](frontend-dashboard-exercises-guide.md).

### Asignaciones, actividad y registros

Los endpoints `POST` y `PUT` de estas rutas usan DTOs de escritura sin `id`:

| Recurso | Endpoint | Body |
| --- | --- | --- |
| Asignar plan | `/user-plans` | `{ "userId": 1, "planId": 2 }` |
| Actividad de un plan | `/current-plan-activities` | `{ "date": "2026-10-21T12:00:00Z", "userPlanId": 1, "activeWeekId": 1, "activeDayId": 1, "isActive": true }` |
| Registro de peso | `/registers` | `{ "userPlanId": 1, "exerciseId": 1, "blockId": 1, "weight": 40.0 }` |

Para asignar un plan, el usuario debe tener una membresía activa en el gimnasio del plan. La API y PostgreSQL lo validan. La pantalla no debe ofrecer mover una asignación, actividad o registro a otro gimnasio.

### Evaluaciones y tests

Las evaluaciones y los tests pertenecen al usuario global, no a un gimnasio. Un usuario puede conservarlos al pertenecer a varios gimnasios; un coach puede verlos y gestionarlos si comparte al menos un gimnasio activo con ese usuario.

```http
GET /evaluations?userId=42
GET /user-tests?userId=42
```

Los bodies completos de `POST` y `PUT` deben incluir `userId`; no permitir cambiarlo durante una edición.

```json
{
  "userId": 42,
  "name": "Evaluación inicial",
  "description": "Mediciones de ingreso",
  "creationDate": "2026-10-21T12:00:00Z"
}
```

```json
{
  "exerciseId": 1,
  "userId": 42,
  "evaluationId": 10,
  "quantityTypeId": 1,
  "repetitions": 12,
  "quantity": 40.0,
  "date": "2026-10-21T12:00:00Z",
  "description": "Control inicial"
}
```

Aunque el backend permite `DELETE` a un coach autorizado, por decisión de producto el dashboard no debe mostrar la acción de eliminar evaluaciones ni tests por ahora.

## 7. Carga de imágenes

Para logo, banner, noticias o ejercicios, subir primero el archivo mediante `POST /gyms/{gymId}/uploads/images` usando `multipart/form-data` y un único campo `image`. La respuesta incluye una URL relativa bajo `/media`; guardar esa URL en el recurso correspondiente después de que la carga sea exitosa.

La carga requiere `COACH` o superior. Usar la [documentación de imágenes](endpoints/images/image-upload-endpoints.md) para límites, formatos y ejemplos. No enviar la imagen dentro de JSON ni guardarla como base64 en PostgreSQL.

## 8. Respuestas y errores

- `POST` de CRUD estándar responde `201 Created` sin body. Excepciones: `POST /gyms` y los dos `POST /gyms/{gymId}/members...` devuelven el recurso creado.
- `PUT` y `DELETE` exitosos responden `204 No Content`.
- `400`: path, query o payload inválido.
- `401`: token ausente, inválido o vencido.
- `403`: token válido, pero sin permiso o membresía vigente.
- `404`: recurso o referencia inexistente.
- `409`: conflicto de negocio, como email/membresía duplicados o intentar quitar el último owner.
- `422`: una regla de integridad de PostgreSQL rechazó el estado solicitado.

Los errores producidos por el manejador central pueden tener la forma `{ "code": "...", "message": "..." }`. Algunos rechazos de rutas existentes aún pueden llegar sin body; por lo tanto la UI debe decidir principalmente por el código HTTP y usar `message` solo cuando esté presente. Los errores de red también requieren una pantalla o notificación global.

## 9. Checklist de entrega del dashboard

- [ ] Interceptor con bearer y un único refresh controlado ante `401`.
- [ ] Limpieza de sesión si refresh o el reintento fallan con `401`.
- [ ] Estado global para perfil, `systemRoles`, membresías y gimnasio activo.
- [ ] Roles canónicos, sin valores de presentación como `"Alumno"`.
- [ ] Listas del gimnasio obtenidas de `/gyms/{gymId}/...` con `limit` y `offset`.
- [ ] Formularios de gimnasio sin `schedule`, con `localityId` en el `PUT` completo.
- [ ] Edición de perfil sin contraseña; cambio de contraseña por endpoint dedicado.
- [ ] Altas/bajas de miembros mediante `/gyms/{gymId}/members`, nunca mediante `/user-gyms`.
- [ ] Evaluaciones y tests filtrados por `userId`, conservando ese usuario al editar.
- [ ] Las imágenes se suben por `multipart/form-data` y solo se persiste la URL retornada.
- [ ] Manejo visible de errores de red, `403`, `404`, `409` y `422`.
