# OneGym API — documentación de endpoints

Guía de integración para frontend basada en las rutas actualmente declaradas en la API.

## Convenciones generales

- Reemplazar `{BASE_URL}` por la URL del ambiente, por ejemplo `http://localhost:8080`.
- Salvo las rutas de autenticación, `/` y `/json/kotlinx-serialization`, todos los endpoints requieren:

  ```http
  Authorization: Bearer <accessToken>
  Content-Type: application/json
  ```

- Los identificadores son `Long` autogenerados. En los `POST` no enviar `id`.
- Todos los `POST` de recursos responden `201 Created` **sin body**. Luego consultar el recurso mediante un `GET`.
- Todos los `PUT` y `DELETE` de recursos responden `204 No Content` **sin body**.
- Un `GET /recurso/{id}` inexistente responde `404`; un `id` no numérico responde `400`.
- Los `DELETE /recurso/{id}` son idempotentes: responden `204` aunque el ID no exista.
- Los `PUT` son actualizaciones completas: enviar todos los campos requeridos del objeto. El `id` del body se ignora; prevalece el ID de la URL.
- Las fechas `date` usan ISO-8601 con zona horaria, por ejemplo `2026-10-09T12:00:00Z`. Las fechas de membresías y entrenadores usan `YYYY-MM-DD`.

> Nota de CORS: la configuración actual permite como origen explícito `http://localhost:5173`. Otros orígenes requerirán una actualización de la configuración del backend.

## Índice

| Área | Endpoints |
| --- | --- |
| Públicos | `GET /`, `GET /json/kotlinx-serialization` |
| Autenticación | `POST /login`, `POST /auth/refresh`, `POST /auth/logout` |
| Sesión del usuario | `GET /profile/me`, `GET /status` |
| Información del gimnasio | `GET /{gymId}/home`, `GET /{gymId}/news`, `GET /plan/{gymId}/me` |
| CRUD | Usuarios, gimnasios, membresías, entrenadores, contenido, planes y evaluaciones |

## Endpoints públicos

### `GET /`

La documentación detallada se encuentra en [root-endpoint.md](endpoints/root/root-endpoint.md).

**Respuesta — `200 OK`**

```text
Hello, World!
```

### `GET /json/kotlinx-serialization`

La documentación detallada se encuentra en [serialization-endpoint.md](endpoints/serialization/serialization-endpoint.md).

**Respuesta — `200 OK`**

```json
{ "hello": "world" }
```

## Autenticación

La documentación detallada se encuentra en [authentication-endpoints.md](endpoints/authentication/authentication-endpoints.md).

### `POST /login`

No requiere bearer token.

**Body**

```json
{
  "email": "sofia@onegym.test",
  "password": "password",
  "deviceInfo": "Web Chrome"
}
```

`deviceInfo` es opcional.

**Respuesta exitosa — `200 OK`**

```json
{
  "user": {
    "id": 1,
    "firstName": "Sofia",
    "lastName": "Martinez",
    "email": "sofia@onegym.test",
    "avatarUrl": null,
    "phone": "+54 11 5555-0101",
    "address": "Av. Siempre Viva 123"
  },
  "accessToken": "eyJ...",
  "refreshToken": "token-de-refresh...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Errores:** `401 Unauthorized` si el email no existe o la contraseña es incorrecta.

### `POST /auth/refresh`

No requiere bearer token. Rota el refresh token: guardar el nuevo `accessToken` **y** el nuevo `refreshToken` de la respuesta.

**Body**

```json
{
  "refreshToken": "token-de-refresh-vigente",
  "deviceInfo": "Web Chrome"
}
```

**Respuesta — `200 OK`**

La respuesta tiene la misma estructura que `POST /login`.

**Errores:** `401 Unauthorized` si el refresh token venció, fue revocado o no es válido.

### `POST /auth/logout`

No requiere bearer token.

**Body**

```json
{ "refreshToken": "token-de-refresh-vigente" }
```

**Respuesta — `204 No Content`**

No devuelve body. Es idempotente: devuelve `204` aun si el token ya no existe.

## Sesión del usuario autenticado

### `GET /profile/me`

La documentación detallada se encuentra en [profile-endpoints.md](endpoints/profile/profile-endpoints.md).

Devuelve el usuario asociado al JWT, sin contraseña.

**Respuesta — `200 OK`**

```json
{
  "id": 1,
  "firstName": "Sofia",
  "lastName": "Martinez",
  "email": "sofia@onegym.test",
  "avatarUrl": null,
  "phone": "+54 11 5555-0101",
  "address": "Av. Siempre Viva 123"
}
```

**Errores:** `401 Unauthorized` por token inválido o ausente; `404 Not Found` si el usuario del token ya no existe.

### `GET /status`

La documentación detallada se encuentra en [status-endpoints.md](endpoints/status/status-endpoints.md).

Devuelve las membresías activas del usuario autenticado y su rol en cada gimnasio.

**Respuesta — `200 OK`**

```json
{
  "gymCount": 1,
  "gyms": [
    {
      "gym": {
        "id": 1,
        "name": "Forze Gym",
        "description": "Gimnasio de entrenamiento funcional y fuerza.",
        "schedule": "Lunes a viernes 07:00-22:00",
        "address": "Av. Siempre Viva 123",
        "phone": "+54 11 5555-0101",
        "logoUrl": null,
        "bannerUrl": null
      },
      "role": { "id": 1, "name": "Alumno" }
    }
  ]
}
```

## Vistas de gimnasio para usuarios suscriptos

Estas rutas requieren que el usuario autenticado tenga una membresía vigente en el gimnasio indicado.

### `GET /{gymId}/home`

La documentación detallada se encuentra en [gym-home-endpoints.md](endpoints/gym-home/gym-home-endpoints.md).

Ejemplo: `GET /1/home`.

**Respuesta — `200 OK`**

```json
{
  "gym": {
    "id": 1,
    "name": "Forze Gym",
    "description": "Gimnasio de entrenamiento funcional y fuerza.",
    "schedule": "Lunes a viernes 07:00-22:00",
    "address": "Av. Siempre Viva 123",
    "phone": "+54 11 5555-0101",
    "logoUrl": null,
    "bannerUrl": null
  },
  "coaches": [
    {
      "user": {
        "id": 2,
        "firstName": "Lucas",
        "lastName": "Fernandez",
        "email": "lucas@onegym.test",
        "avatarUrl": null,
        "phone": "+54 11 5555-0102",
        "address": "Av. Siempre Viva 124"
      },
      "coachType": { "id": 1, "name": "Entrenador" },
      "startDate": "2026-01-01",
      "endDate": null
    }
  ]
}
```

**Errores:** `400` si `gymId` no es numérico; `404` si el gimnasio no existe; `403 Forbidden` si no existe membresía activa.

### `GET /{gymId}/news`

Ejemplo: `GET /1/news`. Las noticias se devuelven de la más reciente a la más antigua.

**Respuesta — `200 OK`**

```json
[
  {
    "id": 1,
    "gymId": 1,
    "title": "Horario feriado",
    "description": "El lunes abrimos de 09:00 a 14:00.",
    "date": "2026-10-09T10:00:00Z",
    "imageUrl": "https://cdn.ejemplo.com/news/feriado.png"
  }
]
```

**Errores:** `400` si `gymId` no es numérico; `403` si no existe membresía activa.

### `GET /plan/{gymId}/me`

La documentación detallada se encuentra en [active-plans-endpoints.md](endpoints/active-plans/active-plans-endpoints.md).

Ejemplo: `GET /plan/1/me`. Devuelve las rutinas activas asignadas al usuario en ese gimnasio, con su estructura completa.

**Respuesta — `200 OK`**

```json
{
  "plans": [
    {
      "id": 1,
      "name": "Fuerza inicial",
      "isActive": true,
      "weeks": [
        {
          "id": 1,
          "number": 1,
          "isActive": true,
          "days": [
            {
              "id": 1,
              "number": 1,
              "isActive": true,
              "blocks": [
                {
                  "id": 1,
                  "position": 1,
                  "name": "Entrada en calor",
                  "laps": 3,
                  "exercises": [
                    {
                      "id": 1,
                      "exerciseId": 1,
                      "name": "Sentadilla",
                      "repetitions": 12,
                      "quantity": null,
                      "quantityType": null,
                      "position": 1
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

`plans` puede ser un arreglo vacío. **Errores:** `400` si `gymId` no es numérico; `403` si no existe membresía activa.

## Recursos CRUD

Los siguientes recursos comparten estas operaciones, siempre autenticadas:

| Operación | Ruta | Respuesta exitosa |
| --- | --- | --- |
| Listar | `GET /{recurso}` | `200` y un arreglo de objetos. |
| Obtener | `GET /{recurso}/{id}` | `200` y un objeto. |
| Crear | `POST /{recurso}` | `201` sin body. |
| Actualizar | `PUT /{recurso}/{id}` | `204` sin body. |
| Eliminar | `DELETE /{recurso}/{id}` | `204` sin body. |

El body de creación que se indica para cada recurso también es el body de actualización (`PUT`), cambiando la URL por `/{id}`. Para ver el objeto creado o actualizado, realizar después un `GET`.

### Usuarios — `/users`

La documentación detallada se encuentra en [users-endpoints.md](endpoints/users/users-endpoints.md).

**Body para `POST /users` o `PUT /users/{id}`**

```json
{
  "firstName": "Sofia",
  "lastName": "Martinez",
  "email": "sofia@onegym.test",
  "password": "una-contraseña-segura",
  "avatarUrl": "https://cdn.ejemplo.com/avatars/sofia.png",
  "phone": "+54 11 5555-0101",
  "address": "Av. Siempre Viva 123"
}
```

**Ejemplo de `GET /users/1` — `200 OK`**

```json
{
  "id": 1,
  "firstName": "Sofia",
  "lastName": "Martinez",
  "email": "sofia@onegym.test",
  "avatarUrl": "https://cdn.ejemplo.com/avatars/sofia.png",
  "phone": "+54 11 5555-0101",
  "address": "Av. Siempre Viva 123"
}
```

Las respuestas no exponen `password`. Al actualizar un usuario se debe enviar una contraseña válida: se vuelve a guardar codificada.

#### `PUT /users/{id}/password`

**Body**

```json
{
  "currentPassword": "contraseña-actual",
  "newPassword": "contraseña-nueva"
}
```

**Respuesta:** `204 No Content` si se actualizó; `401 Unauthorized` si la contraseña actual no coincide. Devuelve `400` si el ID es inválido.

### Gimnasios — `/gyms`

La documentación detallada se encuentra en [gyms-endpoints.md](endpoints/gyms/gyms-endpoints.md).

**Body de ejemplo**

```json
{
  "name": "Forze Gym",
  "description": "Gimnasio de entrenamiento funcional y fuerza.",
  "schedule": "Lunes a viernes 07:00-22:00",
  "address": "Av. Siempre Viva 123",
  "phone": "+54 11 5555-0101",
  "logoUrl": null,
  "bannerUrl": null
}
```

**Ejemplo de respuesta `GET /gyms/1`**: el mismo objeto incluyendo `"id": 1`.

### Roles de usuario — `/user-roles`

La documentación detallada se encuentra en [user-roles-endpoints.md](endpoints/user-roles/user-roles-endpoints.md).

```json
{ "name": "Alumno" }
```

**Respuesta `GET /user-roles/1`**

```json
{ "id": 1, "name": "Alumno" }
```

### Tipos de entrenador — `/coach-types`

La documentación detallada se encuentra en [coach-types-endpoints.md](endpoints/coach-types/coach-types-endpoints.md).

```json
{ "name": "Entrenador" }
```

**Respuesta `GET /coach-types/1`**

```json
{ "id": 1, "name": "Entrenador" }
```

### Membresías usuario-gimnasio — `/user-gyms`

La documentación detallada se encuentra en [user-gyms-endpoints.md](endpoints/user-gyms/user-gyms-endpoints.md).

```json
{
  "userId": 1,
  "gymId": 1,
  "roleId": 1,
  "startDate": "2026-01-01",
  "endDate": null
}
```

**Respuesta `GET /user-gyms/1`**

```json
{
  "id": 1,
  "userId": 1,
  "gymId": 1,
  "roleId": 1,
  "startDate": "2026-01-01",
  "endDate": null
}
```

### Asignaciones de entrenadores — `/gym-coaches`

La documentación detallada se encuentra en [gym-coaches-endpoints.md](endpoints/gym-coaches/gym-coaches-endpoints.md).

```json
{
  "userId": 2,
  "gymId": 1,
  "coachTypeId": 1,
  "startDate": "2026-01-01",
  "endDate": null
}
```

**Respuesta `GET /gym-coaches/1`**

```json
{
  "id": 1,
  "userId": 2,
  "gymId": 1,
  "coachTypeId": 1,
  "startDate": "2026-01-01",
  "endDate": null
}
```

### Noticias — `/news`

La documentación detallada se encuentra en [news-endpoints.md](endpoints/news/news-endpoints.md).

```json
{
  "gymId": 1,
  "title": "Horario feriado",
  "description": "El lunes abrimos de 09:00 a 14:00.",
  "date": "2026-10-09T10:00:00Z",
  "imageUrl": "https://cdn.ejemplo.com/news/feriado.png"
}
```

**Respuesta `GET /news/1`**: el mismo objeto incluyendo `"id": 1`.

### Tipos de plan — `/plan-types`

La documentación detallada se encuentra en [plan-types-endpoints.md](endpoints/plan-types/plan-types-endpoints.md).

```json
{ "name": "Entrenamiento" }
```

**Respuesta `GET /plan-types/1`**

```json
{ "id": 1, "name": "Entrenamiento" }
```

### Planes — `/plans`

La documentación detallada se encuentra en [plans-endpoints.md](endpoints/plans/plans-endpoints.md).

```json
{
  "name": "Fuerza inicial",
  "gymId": 1,
  "planTypeId": 1,
  "planRootId": null
}
```

**Respuesta `GET /plans/1`**

```json
{
  "id": 1,
  "name": "Fuerza inicial",
  "gymId": 1,
  "planTypeId": 1,
  "planRootId": null
}
```

### Semanas — `/weeks`

La documentación detallada se encuentra en [weeks-endpoints.md](endpoints/weeks/weeks-endpoints.md).

```json
{ "number": 1, "planId": 1 }
```

**Respuesta `GET /weeks/1`**

```json
{ "id": 1, "number": 1, "planId": 1 }
```

### Días — `/days`

La documentación detallada se encuentra en [days-endpoints.md](endpoints/days/days-endpoints.md).

```json
{ "number": 1, "weekId": 1 }
```

**Respuesta `GET /days/1`**

```json
{ "id": 1, "number": 1, "weekId": 1 }
```

### Ejercicios — `/exercises`

La documentación detallada se encuentra en [exercises-endpoints.md](endpoints/exercises/exercises-endpoints.md).

```json
{
  "name": "Sentadilla",
  "description": "Sentadilla con barra.",
  "videoUrl": "https://cdn.ejemplo.com/videos/sentadilla.mp4",
  "imageUrl": "https://cdn.ejemplo.com/images/sentadilla.png",
  "gymId": 1,
  "isVisibleGlobal": false
}
```

**Respuesta `GET /exercises/1`**: el mismo objeto incluyendo `"id": 1`. Para un ejercicio global enviar `"gymId": null` e `"isVisibleGlobal": true`.

### Tipos de cantidad — `/quantity-types`

La documentación detallada se encuentra en [quantity-types-endpoints.md](endpoints/quantity-types/quantity-types-endpoints.md).

```json
{ "name": "Kilogramos" }
```

**Respuesta `GET /quantity-types/1`**

```json
{ "id": 1, "name": "Kilogramos" }
```

### Bloques — `/blocks`

La documentación detallada se encuentra en [blocks-endpoints.md](endpoints/blocks/blocks-endpoints.md).

```json
{
  "position": 1,
  "name": "Entrada en calor",
  "dayId": 1,
  "laps": 3
}
```

**Respuesta `GET /blocks/1`**: el mismo objeto incluyendo `"id": 1`. `name` puede ser `null`.

### Ejercicios de bloque — `/block-exercises`

La documentación detallada se encuentra en [block-exercises-endpoints.md](endpoints/block-exercises/block-exercises-endpoints.md).

```json
{
  "blockId": 1,
  "exerciseId": 1,
  "quantityTypeId": 1,
  "position": 1,
  "repetitions": 12,
  "quantity": 20.0
}
```

**Respuesta `GET /block-exercises/1`**: el mismo objeto incluyendo `"id": 1`. `quantityTypeId`, `repetitions` y `quantity` pueden ser `null`.

### Asignación de plan a usuario — `/user-plans`

La documentación detallada se encuentra en [user-plans-endpoints.md](endpoints/user-plans/user-plans-endpoints.md).

```json
{ "userId": 1, "planId": 1 }
```

**Respuesta `GET /user-plans/1`**

```json
{ "id": 1, "userId": 1, "planId": 1 }
```

### Actividad actual de un plan — `/current-plan-activities`

La documentación detallada se encuentra en [current-plan-activities-endpoints.md](endpoints/current-plan-activities/current-plan-activities-endpoints.md).

```json
{
  "date": "2026-10-09T12:00:00Z",
  "userPlanId": 1,
  "activeWeekId": 1,
  "activeDayId": 1,
  "isActive": true
}
```

**Respuesta `GET /current-plan-activities/1`**: el mismo objeto incluyendo `"id": 1`. `activeWeekId` y `activeDayId` pueden ser `null`.

### Registros de ejercicio — `/registers`

La documentación detallada se encuentra en [registers-endpoints.md](endpoints/registers/registers-endpoints.md).

```json
{
  "userPlanId": 1,
  "exerciseId": 1,
  "blockId": 1,
  "weight": 40.0
}
```

**Respuesta `GET /registers/1`**: el mismo objeto incluyendo `"id": 1`.

### Evaluaciones — `/evaluations`

La documentación detallada se encuentra en [evaluations-endpoints.md](endpoints/evaluations/evaluations-endpoints.md).

```json
{
  "name": "Evaluación inicial",
  "description": "Mediciones de ingreso.",
  "creationDate": "2026-10-09T12:00:00Z"
}
```

**Respuesta `GET /evaluations/1`**: el mismo objeto incluyendo `"id": 1`. `description` puede ser `null`.

### Tests de usuario — `/user-tests`

La documentación detallada se encuentra en [user-tests-endpoints.md](endpoints/user-tests/user-tests-endpoints.md).

```json
{
  "exerciseId": 1,
  "userId": 1,
  "evaluationId": 1,
  "quantityTypeId": 1,
  "repetitions": 12,
  "quantity": 40.0,
  "date": "2026-10-09T12:00:00Z",
  "description": "Serie de evaluación inicial."
}
```

**Respuesta `GET /user-tests/1`**: el mismo objeto incluyendo `"id": 1`. `quantityTypeId`, `repetitions` y `quantity` pueden ser `null`.

## Resumen de rutas CRUD declaradas

Para cada uno de estos recursos existen las cinco operaciones descritas en [Recursos CRUD](#recursos-crud):

| Recurso | Rutas |
| --- | --- |
| Usuarios | `/users`, `/users/{id}` |
| Gimnasios | `/gyms`, `/gyms/{id}` |
| Roles | `/user-roles`, `/user-roles/{id}` |
| Tipos de entrenador | `/coach-types`, `/coach-types/{id}` |
| Membresías | `/user-gyms`, `/user-gyms/{id}` |
| Entrenadores de gimnasio | `/gym-coaches`, `/gym-coaches/{id}` |
| Noticias | `/news`, `/news/{id}` |
| Tipos de plan | `/plan-types`, `/plan-types/{id}` |
| Planes | `/plans`, `/plans/{id}` |
| Semanas | `/weeks`, `/weeks/{id}` |
| Días | `/days`, `/days/{id}` |
| Ejercicios | `/exercises`, `/exercises/{id}` |
| Tipos de cantidad | `/quantity-types`, `/quantity-types/{id}` |
| Bloques | `/blocks`, `/blocks/{id}` |
| Ejercicios de bloque | `/block-exercises`, `/block-exercises/{id}` |
| Planes de usuario | `/user-plans`, `/user-plans/{id}` |
| Actividades de plan | `/current-plan-activities`, `/current-plan-activities/{id}` |
| Registros | `/registers`, `/registers/{id}` |
| Evaluaciones | `/evaluations`, `/evaluations/{id}` |
| Tests de usuario | `/user-tests`, `/user-tests/{id}` |

