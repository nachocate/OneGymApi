# API de registros de ejercicio (`/registers`)

Documentación para registrar el peso utilizado por un usuario al realizar un ejercicio dentro de un bloque de su plan.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `userPlanId`, `exerciseId` y `blockId` deben referenciar recursos existentes.

## Estructura de un registro

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `userPlanId` | number | Sí | ID de la asignación del plan al usuario. |
| `exerciseId` | number | Sí | ID del ejercicio realizado. |
| `blockId` | number | Sí | ID del bloque al que pertenece el ejercicio. |
| `weight` | number | Sí | Peso registrado para el ejercicio. |

**Body de ejemplo**

```json
{
  "userPlanId": 1,
  "exerciseId": 1,
  "blockId": 1,
  "weight": 40.0
}
```

**Respuesta de ejemplo**

```json
{
  "id": 1,
  "userPlanId": 1,
  "exerciseId": 1,
  "blockId": 1,
  "weight": 40.0
}
```

## 1. Listar registros

```http
GET {BASE_URL}/registers
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener un registro por ID

```http
GET {BASE_URL}/registers/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{
  "id": 1,
  "userPlanId": 1,
  "exerciseId": 1,
  "blockId": 1,
  "weight": 40.0
}
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el registro no existe.

## 3. Crear un registro

```http
POST {BASE_URL}/registers
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo de la sección anterior.

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar un registro

```http
PUT {BASE_URL}/registers/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "userPlanId": 1,
  "exerciseId": 1,
  "blockId": 1,
  "weight": 45.0
}
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el registro no existe.

## 5. Eliminar un registro

```http
DELETE {BASE_URL}/registers/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
