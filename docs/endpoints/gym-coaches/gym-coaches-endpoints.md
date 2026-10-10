# API de entrenadores de gimnasio (`/gym-coaches`)

Documentación para asignar entrenadores a un gimnasio y definir su tipo de entrenador durante un período determinado.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `userId`, `gymId` y `coachTypeId` deben referenciar recursos existentes.
- Las fechas usan formato `YYYY-MM-DD`. Si se informa `endDate`, debe ser igual o posterior a `startDate`.

## Estructura de una asignación

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `userId` | number | Sí | ID del usuario que actuará como entrenador. |
| `gymId` | number | Sí | ID del gimnasio. |
| `coachTypeId` | number | Sí | ID de la especialidad o tipo de entrenador. |
| `startDate` | string | Sí | Inicio de la asignación, `YYYY-MM-DD`. |
| `endDate` | string \| null | No | Fin de la asignación; `null` cuando permanece vigente. |

**Body de ejemplo**

```json
{
  "userId": 2,
  "gymId": 1,
  "coachTypeId": 1,
  "startDate": "2026-01-01",
  "endDate": null
}
```

**Respuesta de ejemplo**

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

## 1. Listar asignaciones

```http
GET {BASE_URL}/gym-coaches
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener una asignación por ID

```http
GET {BASE_URL}/gym-coaches/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

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

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la asignación no existe.

## 3. Crear una asignación

```http
POST {BASE_URL}/gym-coaches
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo de la sección anterior.

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar una asignación

```http
PUT {BASE_URL}/gym-coaches/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "userId": 2,
  "gymId": 1,
  "coachTypeId": 2,
  "startDate": "2026-01-01",
  "endDate": "2026-12-31"
}
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la asignación no existe.

## 5. Eliminar una asignación

```http
DELETE {BASE_URL}/gym-coaches/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
