# Implementación de coaches en el dashboard

> Esta guía describe el flujo legado. Para las rutas vigentes, atómicas y orientadas al dashboard, usar la [guía actualizada de administración de entrenadores](frontend-dashboard-coaches-v2-guide.md).

## 1. Conceptos que no se deben mezclar

Una persona que trabaja como coach tiene dos datos distintos:

1. **Membresía y rol en el gimnasio.** El usuario debe pertenecer al gimnasio con rol `COACH`.
2. **Asignación de coach.** Registra su especialidad (`coachTypeId`) y el período en que trabaja como coach.

La tabla de la pantalla debe mostrar solamente usuarios que tengan ambas cosas. La API actual no crea ambas relaciones en una sola request; el alta se hace en dos pasos.

```text
Usuario global
   └─ membresía en el gym con rol COACH
         └─ asignación gym-coaches con especialidad y fechas
```

## 2. Permisos de la interfaz

| Acción | MEMBER | COACH | ADMIN | OWNER | SUPER_ADMIN |
| --- | --- | --- | --- | --- | --- |
| Ver coaches | Sí | Sí | Sí | Sí | Sí |
| Crear, editar o finalizar una asignación | No | No | Sí | Sí | Sí |

El frontend debe ocultar botones de administración si no corresponde, pero también debe manejar `403 Forbidden`: el backend es quien valida los permisos.

Obtener el rol actual desde `GET /status`; no usar nombres de presentación como `"Entrenador"` o `"Alumno"`. Los valores válidos son `MEMBER`, `COACH`, `ADMIN` y `OWNER`.

## 3. Datos iniciales de la pantalla

Al ingresar a la pantalla, con `gymId` igual al gimnasio activo, hacer estas requests en paralelo:

```http
GET /gyms/{gymId}/coaches?limit=20&offset=0
GET /gyms/{gymId}/members?limit=100&offset=0
GET /coach-types
```

Headers para las tres:

```http
Authorization: Bearer <accessToken>
```

### Qué devuelve cada ruta

`GET /gyms/{gymId}/coaches` devuelve asignaciones de coach:

```json
[
  {
    "id": 15,
    "userId": 42,
    "gymId": 1,
    "coachTypeId": 2,
    "startDate": "2026-10-01",
    "endDate": null
  }
]
```

`GET /gyms/{gymId}/members` devuelve el perfil público y el rol de cada miembro:

```json
[
  {
    "id": 30,
    "user": {
      "id": 42,
      "firstName": "Lucía",
      "lastName": "Gómez",
      "email": "lucia@example.test",
      "avatarUrl": null,
      "phone": null,
      "address": null
    },
    "role": { "id": 2, "name": "COACH" },
    "startDate": "2026-01-01",
    "endDate": null
  }
]
```

`GET /coach-types` devuelve el catálogo de especialidades, por ejemplo:

```json
[
  { "id": 1, "name": "Musculación" },
  { "id": 2, "name": "Funcional" }
]
```

### Cómo unir los datos

Crear dos mapas en el estado del frontend:

```text
membersByUserId[user.id] = miembro
coachTypesById[coachType.id] = coachType
```

Para cada elemento de `/gyms/{gymId}/coaches`, buscar:

```text
persona      = membersByUserId[assignment.userId]
especialidad = coachTypesById[assignment.coachTypeId]
```

Con eso se renderiza nombre, avatar, email, especialidad, fecha de inicio, fecha de fin y estado. Si falta una referencia, mostrar `"Datos no disponibles"` y registrar el problema de manera segura; no romper toda la pantalla.

`GET /{gymId}/home` sirve para una tarjeta de inicio con coaches activos y datos enriquecidos. Para administrar coaches, usar los tres endpoints anteriores porque incluyen IDs y catálogo para el formulario.

## 4. Paginación

Los listados por gimnasio usan:

```text
?limit=20&offset=0
```

- `limit`: de `1` a `100`; por defecto `20`.
- `offset`: desde `0`; por defecto `0`.
- La respuesta es un arreglo; si llega vacío, no hay más elementos para ese offset.

Para “Cargar más”, aumentar `offset` en la cantidad recibida. No asumir que un gimnasio tendrá menos de 100 miembros o coaches.

## 5. Alta de un coach existente

Usar este flujo cuando la persona ya existe como usuario global. El formulario debe pedir:

- Usuario existente.
- Especialidad.
- Fecha de inicio.
- Fecha de fin opcional.

### Paso 1: asignar la membresía con rol `COACH`

```http
POST /gyms/{gymId}/members
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "userId": 42,
  "roleName": "COACH"
}
```

Respuesta: `201 Created` con la membresía creada.

### Paso 2: crear la asignación de coach

```http
POST /gym-coaches
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "userId": 42,
  "gymId": 1,
  "coachTypeId": 2,
  "startDate": "2026-10-26",
  "endDate": null
}
```

Respuesta: `201 Created` sin body.

Al terminar, volver a cargar coaches y miembros. No agregar objetos inventados al estado: la API es la fuente de verdad.

### Si falla alguno de los pasos

La API no dispone aún de un endpoint atómico que cree simultáneamente la membresía `COACH` y `gym-coaches`.

- Si falla el paso 1, no ejecutar el paso 2.
- Si el paso 1 responde `409`, la persona ya pertenece al gimnasio. Consultar miembros: si ya tiene rol `COACH`, continuar con el paso 2; si tiene otro rol, no intentar crear un duplicado y pedir intervención administrativa.
- Si el paso 2 falla después de que el paso 1 tuvo éxito, informar que se creó la membresía pero faltó la especialidad. Ofrecer reintentar solamente el paso 2. No eliminar automáticamente la membresía: podría ser una relación válida creada previamente.

> **Limitación actual de la API:** no existe una ruta de negocio para cambiar el rol de una membresía existente de `MEMBER` a `COACH`. Por lo tanto, un miembro ya asignado no puede convertirse en coach desde el dashboard con las rutas soportadas actuales. El equipo debe mostrar un mensaje claro y solicitar un endpoint de cambio de rol al backend; no usar `/user-gyms` como atajo.

## 6. Alta de una persona nueva

Primero crear y asignar el usuario con rol `COACH`:

```http
POST /gyms/{gymId}/members/registration
Content-Type: application/json
Authorization: Bearer <accessToken>
```

```json
{
  "user": {
    "firstName": "Lucía",
    "lastName": "Gómez",
    "email": "lucia@example.test",
    "password": "contraseña-inicial-segura",
    "avatarUrl": null,
    "phone": null,
    "address": null
  },
  "roleName": "COACH"
}
```

La respuesta `201 Created` contiene la membresía creada; tomar `userId` de ella y ejecutar el paso 2 de la sección anterior (`POST /gym-coaches`). Nunca guardar ni mostrar nuevamente la contraseña inicial.

## 7. Editar un coach

La edición modifica la asignación de coach, no el perfil del usuario ni el rol de membresía.

```http
PUT /gym-coaches/{coachAssignmentId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "userId": 42,
  "gymId": 1,
  "coachTypeId": 1,
  "startDate": "2026-10-01",
  "endDate": null
}
```

- Mandar el body completo.
- Mantener el mismo `gymId`; no se permite trasladar una asignación a otro gimnasio con `PUT`.
- La respuesta exitosa es `204 No Content`.
- Para editar nombre, email, avatar, teléfono o dirección se usa `PUT /users/{userId}` y solo puede hacerlo el propio usuario o un `SUPER_ADMIN`; no ofrecer esta edición como acción normal de administración del gym.

## 8. Baja o finalización

La opción recomendada para conservar historial es **finalizar** la asignación: editarla y enviar una `endDate`.

```json
{
  "userId": 42,
  "gymId": 1,
  "coachTypeId": 2,
  "startDate": "2026-10-01",
  "endDate": "2026-12-31"
}
```

Para borrar la asignación por completo:

```http
DELETE /gym-coaches/{coachAssignmentId}
Authorization: Bearer <accessToken>
```

La respuesta exitosa es `204 No Content`.

**Importante:** este `DELETE` elimina solo la asignación de especialidad. Actualmente no cambia la membresía: la persona conservará el rol `COACH` dentro del gimnasio. No mostrar un texto que diga “quitar coach completamente” si se ejecuta únicamente este endpoint.

No existe todavía un endpoint de negocio para degradar `COACH` a `MEMBER`. No usar el CRUD legado `/user-gyms` para resolverlo desde el dashboard. Si producto necesita esa operación, debe solicitarse un endpoint específico al backend.

## 9. Manejo de errores

| Código | Acción de UI |
| --- | --- |
| `400` | Revisar datos del formulario: fechas, IDs y campos obligatorios. |
| `401` | Intentar refresh una sola vez; si falla, cerrar sesión. |
| `403` | Mostrar “No tenés permisos para administrar coaches de este gimnasio”. |
| `404` | El coach, usuario, gym o especialidad ya no existe; recargar la lista. |
| `409` | Informar una membresía duplicada o conflicto. No repetir automáticamente el alta. |
| Error de red | Mantener el formulario, mostrar una acción de reintento y no asumir que la operación falló o tuvo éxito. |

## 10. Checklist de implementación

- [ ] Se obtiene `gymId` del selector de gimnasio activo.
- [ ] La tabla combina asignaciones, perfiles públicos y especialidades mediante IDs.
- [ ] El selector de usuarios solo muestra miembros activos que puedan ser coaches.
- [ ] Solo `ADMIN`, `OWNER` y `SUPER_ADMIN` ven acciones de alta, edición y baja.
- [ ] El alta de persona existente crea primero membresía `COACH` y luego la asignación de coach.
- [ ] Se explica y maneja el fallo intermedio entre ambos pasos.
- [ ] La baja se presenta como “finalizar asignación” o “eliminar asignación”, no como baja completa del usuario.
- [ ] La UI trata `204` como éxito sin intentar parsear JSON.
