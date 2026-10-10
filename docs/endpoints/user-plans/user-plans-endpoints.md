# API de planes asignados a usuarios (`/user-plans`)

Documentación para administrar la asignación de planes de entrenamiento a usuarios.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `userId` y `planId` deben referenciar recursos existentes.

## Estructura de una asignación de plan

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `userId` | number | Sí | ID del usuario al que se asigna el plan. |
| `planId` | number | Sí | ID del plan asignado. |

**Body de ejemplo**

```json
{ "userId": 1, "planId": 1 }
```

**Respuesta de ejemplo**

```json
{ "id": 1, "userId": 1, "planId": 1 }
```

## 1. Listar asignaciones de planes

```http
GET {BASE_URL}/user-plans
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
[
  { "id": 1, "userId": 1, "planId": 1 }
]
```

## 2. Obtener una asignación por ID

```http
GET {BASE_URL}/user-plans/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{ "id": 1, "userId": 1, "planId": 1 }
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la asignación no existe.

## 3. Crear una asignación de plan

```http
POST {BASE_URL}/user-plans
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "userId": 1, "planId": 1 }
```

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar una asignación de plan

```http
PUT {BASE_URL}/user-plans/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "userId": 1, "planId": 2 }
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la asignación no existe.

## 5. Eliminar una asignación de plan

```http
DELETE {BASE_URL}/user-plans/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
