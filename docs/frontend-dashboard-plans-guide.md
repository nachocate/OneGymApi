# Implementación de planes y rutinas en el dashboard

Esta guía describe cómo trabajar con planes de entrenamiento en OneGym. Complementa la [guía general del dashboard](frontend-dashboard-guide.md) y está dirigida al equipo junior.

## 1. Estructura de una rutina

Una rutina no es un único objeto. Tiene esta jerarquía:

```text
Plan
 └─ Semana (Week)
     └─ Día (Day)
         └─ Bloque (Block)
             └─ Ejercicio del bloque (BlockExercise)
```

Además, un `UserPlan` asigna un plan a un miembro del gimnasio.

| Recurso | Campos principales |
| --- | --- |
| `Plan` | `name`, `gymId`, `planTypeId`, `planRootId` |
| `Week` | `number`, `planId` |
| `Day` | `number`, `weekId` |
| `Block` | `position`, `name`, `dayId`, `laps` |
| `BlockExercise` | `blockId`, `exerciseId`, `quantityTypeId`, `position`, `repetitions`, `quantity` |
| `UserPlan` | `userId`, `planId` |

No enviar IDs en los `POST`. En los `PUT`, el ID de la URL identifica al recurso; enviar el body completo.

## 2. Permisos

| Acción | MEMBER | COACH | ADMIN | OWNER | SUPER_ADMIN |
| --- | --- | --- | --- | --- | --- |
| Ver lista de planes del gimnasio | Sí | Sí | Sí | Sí | Sí |
| Crear, editar o eliminar rutina y su estructura | No | Sí | Sí | Sí | Sí |
| Asignar o quitar un plan a un miembro | No | Sí | Sí | Sí | Sí |
| Ver listados globales de todos los planes o asignaciones | No | No | No | No | Sí |

Usar el rol de `GET /status` solamente para la visibilidad de la UI. El backend vuelve a validar el permiso y puede responder `403`.

## 3. Datos iniciales de una pantalla de planes

Con el gimnasio activo (`gymId`), hacer estas consultas:

```http
GET /gyms/{gymId}/plans?limit=20&offset=0
GET /gyms/{gymId}/exercises?limit=100&offset=0
GET /plan-types
GET /quantity-types
```

Headers:

```http
Authorization: Bearer <accessToken>
```

Usar:

- `/gyms/{gymId}/plans` para el listado del gimnasio activo.
- `/gyms/{gymId}/exercises` para el selector de ejercicios: incluye los propios del gym y globales.
- `/plan-types` para el selector de tipo de plan.
- `/quantity-types` para elegir la unidad de `quantity`, cuando corresponde.

No usar `GET /plans`, `GET /weeks`, `GET /days`, `GET /blocks` ni `GET /block-exercises` como fuentes de la pantalla: los listados globales son solo para `SUPER_ADMIN`.

Los listados por gym usan `limit` entre `1` y `100`, y `offset` desde `0`. Una respuesta vacía indica que no hay más resultados.

## 4. Crear y editar una planificación completa

El dashboard debe usar `POST /gyms/{gymId}/plans` para crear el árbol completo y `PUT /gyms/{gymId}/plans/{planId}` para reemplazarlo de forma atómica. Ambas rutas reciben/devuelven el árbol y todos sus IDs; no encadenar requests a `/weeks`, `/days`, `/blocks` o `/block-exercises`.

Para abrir el editor de una planificación existente usar:

```http
GET /gyms/{gymId}/plans/{planId}/structure
```

El body completo, el uso de IDs durante la edición, clonación, asignación y manejo de errores están documentados en la [guía del constructor de planificaciones](frontend-dashboard-planification-builder-guide.md) y en el [contrato técnico](endpoints/planifications/planifications-endpoints.md).

## 5. Asignar un plan a un miembro

Usar:

```http
POST /gyms/{gymId}/plans/{planId}/assignments
```

con `{ "userId": 42 }`. El backend valida que el miembro esté activo en el gimnasio y devuelve el `UserPlan` creado. Consultar asignaciones con:

```http
GET /gyms/{gymId}/members/{userId}/plans
```

Para quitar una asignación existente se mantiene `DELETE /user-plans/{userPlanId}`.

## 6. Constructor completo disponible

El editor no debe encadenar los CRUDs técnicos de `/plans`, `/weeks`, `/days`, `/blocks` y `/block-exercises`. Usar las rutas atómicas:

```text
GET  /gyms/{gymId}/plans/{planId}/structure
POST /gyms/{gymId}/plans
PUT  /gyms/{gymId}/plans/{planId}
POST /gyms/{gymId}/plans/{planId}/clone
POST /gyms/{gymId}/plans/{planId}/assignments
GET  /gyms/{gymId}/members/{userId}/plans
```

Estas rutas reciben o devuelven el árbol completo dentro de una transacción. La especificación completa de experiencia y payload se encuentra en la [guía del constructor de planificaciones](frontend-dashboard-planification-builder-guide.md).

## 7. Planes que consume el usuario final

No usar los CRUD de estructura para mostrar rutinas a un miembro. Para el usuario autenticado usar:

```http
GET /plan/{gymId}/me
```

Devuelve los planes activos del usuario con semanas, días, bloques y ejercicios ya anidados. Es la fuente correcta para la vista de ejecución del plan, no para el editor administrativo.

## 8. Manejo de errores

| Código | Acción de UI |
| --- | --- |
| `400` | Revisar IDs, tipos de datos y campos requeridos. |
| `401` | Intentar refresh una sola vez; si falla, cerrar sesión. |
| `403` | Informar que no tiene permiso o que el miembro no pertenece activamente al gimnasio. |
| `404` | El padre o recurso ya no existe; actualizar la pantalla. |
| `409` | Existe un conflicto, por ejemplo una asignación duplicada. No reintentar automáticamente. |
| `422` | Una regla de integridad rechazó el árbol o una eliminación. Mostrar un mensaje y conservar el formulario. |
| Error de red | No asumir éxito ni fracaso; mostrar reintento y conservar los cambios sin enviar. |

## 9. Checklist

- [ ] El gimnasio activo determina el `gymId` al crear un plan.
- [ ] Solo `COACH`, `ADMIN`, `OWNER` y `SUPER_ADMIN` ven acciones de edición.
- [ ] Los selectores cargan planes, ejercicios, tipos de plan y unidades desde la API.
- [ ] No se usan listados globales reservados a `SUPER_ADMIN` como datos del gimnasio.
- [ ] La UI no inventa IDs después de un `201` sin body.
- [ ] La asignación de plan verifica que el miembro pertenece al gimnasio seleccionado.
- [ ] La vista de rutina del usuario usa `GET /plan/{gymId}/me`.
- [ ] El editor jerárquico completo queda bloqueado hasta contar con endpoints de lectura/escritura atómicos adecuados.
