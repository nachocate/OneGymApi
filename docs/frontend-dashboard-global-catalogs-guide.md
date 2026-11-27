# Roles de sistema y catálogos globales — Dashboard

Esta guía separa tres conceptos que no deben mezclarse en la interfaz:

1. **Rol global de sistema:** actualmente `SUPER_ADMIN`.
2. **Rol dentro de un gimnasio:** `MEMBER`, `COACH`, `ADMIN`, `OWNER`.
3. **Catálogos globales:** tipos de entrenador y tipos de plan.

## 1. Roles globales de sistema

El único rol global actual es `SUPER_ADMIN`. Se consulta junto con las membresías mediante:

```http
GET /status
Authorization: Bearer <accessToken>
```

```json
{
  "systemRoles": ["SUPER_ADMIN"],
  "gyms": []
}
```

Usar:

```ts
const isSuperAdmin = status.systemRoles.includes("SUPER_ADMIN")
```

para decidir si se muestra el área de configuración global.

### Límite actual de la API

Actualmente **no existen endpoints** para listar, asignar o revocar roles globales. El dashboard no puede convertir a un usuario en `SUPER_ADMIN`, ni quitar ese rol, mediante una ruta soportada.

No usar `/user-roles` para ese fin: ese recurso define roles de membresía de gimnasio y no afecta `systemRoles` ni los permisos de plataforma.

Para incorporar la gestión de superadministradores en el futuro, backend debe definir un contrato específico, por ejemplo:

```text
GET    /system-roles
GET    /users/{userId}/system-roles
POST   /users/{userId}/system-roles
DELETE /users/{userId}/system-roles/{roleId}
```

Debe incluir protección contra quitar el último `SUPER_ADMIN`, auditoría y autorización exclusiva de otro `SUPER_ADMIN`. Hasta que exista ese contrato, ocultar acciones de alta/baja de roles globales.

## 2. Roles dentro de un gimnasio

Los roles de gimnasio válidos son fijos: `MEMBER`, `COACH`, `ADMIN` y `OWNER`.

- No crear, renombrar ni eliminar estos roles desde el dashboard aunque el CRUD técnico `/user-roles` esté disponible para `SUPER_ADMIN`.
- Renombrar o borrar un rol canónico puede romper autorización, membresías y reglas de jerarquía.
- Para asignar personas a un gimnasio usar las rutas de miembros; no `/user-gyms` ni `/user-roles` directamente.

| Acción de membresía | Ruta recomendada |
| --- | --- |
| Listar miembros | `GET /gyms/{gymId}/members` |
| Asociar usuario existente | `POST /gyms/{gymId}/members` |
| Crear usuario y asociarlo | `POST /gyms/{gymId}/members/registration` |
| Quitar membresía | `DELETE /gyms/{gymId}/members/{userId}` |

## 3. Tipos de entrenador

Los tipos de entrenador son un catálogo global reutilizado por todos los gimnasios: por ejemplo, `Musculación`, `Funcional` o `Yoga`.

### Permisos

- Lectura: cualquier usuario autenticado.
- Crear, editar y eliminar: solo `SUPER_ADMIN`.

### Listar

```http
GET /coach-types
Authorization: Bearer <accessToken>
```

```json
[
  { "id": 1, "name": "Musculación" },
  { "id": 2, "name": "Funcional" }
]
```

### Crear

```http
POST /coach-types
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{ "name": "Yoga" }
```

Respuesta: `201 Created` sin body. Recargar el listado al finalizar.

### Editar

```http
PUT /coach-types/{coachTypeId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{ "name": "Entrenamiento funcional" }
```

Respuesta: `204 No Content`.

### Eliminar

```http
DELETE /coach-types/{coachTypeId}
Authorization: Bearer <accessToken>
```

Respuesta: `204 No Content` si se elimina correctamente. Si el tipo tiene asignaciones de coaches, PostgreSQL puede rechazar la operación; mostrar el error y pedir que se reasignen o finalicen esas asignaciones antes de reintentar.

## 4. Tipos de plan

Los tipos de plan también son un catálogo global: por ejemplo, `Fuerza`, `Hipertrofia`, `Rehabilitación` o `Funcional`.

### Permisos

- Lectura: cualquier usuario autenticado.
- Crear, editar y eliminar: solo `SUPER_ADMIN`.

### Listar

```http
GET /plan-types
Authorization: Bearer <accessToken>
```

### Crear

```http
POST /plan-types
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{ "name": "Hipertrofia" }
```

Respuesta: `201 Created` sin body.

### Editar

```http
PUT /plan-types/{planTypeId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{ "name": "Fuerza e hipertrofia" }
```

Respuesta: `204 No Content`.

### Eliminar

```http
DELETE /plan-types/{planTypeId}
Authorization: Bearer <accessToken>
```

Respuesta: `204 No Content` si no tiene dependencias. Si ya existen planes con ese tipo, no ofrecer borrado como acción primaria: mostrar que el tipo está en uso y recomendar editar su nombre o migrar los planes primero.

## 5. Reglas de interfaz

1. Mostrar la sección de administración global únicamente si `systemRoles` contiene `SUPER_ADMIN`.
2. Cargar `GET /status` después de login y cada vez que un `403` sugiera un cambio de permisos.
3. Para crear o editar, deshabilitar el botón mientras haya una request pendiente.
4. Tras `201` o `204`, recargar el catálogo desde la API; no inventar IDs locales.
5. Ante `401`, intentar refresh una sola vez. Ante `403`, no intentar refresh: ocultar/inhabilitar la acción y mostrar falta de permisos.
6. Ante conflicto o dependencia al borrar, conservar el listado y explicar que el elemento está siendo utilizado.

## Checklist

- [ ] La UI distingue `systemRoles` de roles de gimnasio.
- [ ] Solo `SUPER_ADMIN` ve controles de catálogos globales.
- [ ] La UI no permite modificar los roles canónicos `MEMBER`, `COACH`, `ADMIN`, `OWNER`.
- [ ] Los tipos de coach y de plan se recargan después de cada mutación.
- [ ] La interfaz no ofrece asignar o revocar `SUPER_ADMIN` hasta que backend exponga un contrato seguro.
