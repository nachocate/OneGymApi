# API de evaluaciones (`/evaluations`)

Documentación para administrar evaluaciones, como una evaluación inicial o controles periódicos.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `creationDate` debe usar formato ISO-8601 con zona horaria, por ejemplo `2026-10-09T12:00:00Z`.

## Estructura de una evaluación

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `name` | string | Sí | Nombre de la evaluación. |
| `description` | string \| null | Sí | Detalle opcional de la evaluación. |
| `creationDate` | string | Sí | Fecha y hora de creación en formato ISO-8601. |

**Body de ejemplo**

```json
{
  "name": "Evaluación inicial",
  "description": "Mediciones de ingreso del alumno.",
  "creationDate": "2026-10-09T12:00:00Z"
}
```

**Respuesta de ejemplo**

```json
{
  "id": 1,
  "name": "Evaluación inicial",
  "description": "Mediciones de ingreso del alumno.",
  "creationDate": "2026-10-09T12:00:00Z"
}
```

## 1. Listar evaluaciones

```http
GET {BASE_URL}/evaluations
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener una evaluación por ID

```http
GET {BASE_URL}/evaluations/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un objeto con la estructura anterior.

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la evaluación no existe.

## 3. Crear una evaluación

```http
POST {BASE_URL}/evaluations
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo de la sección anterior. Para una evaluación sin descripción, enviar `"description": null`.

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar una evaluación

```http
PUT {BASE_URL}/evaluations/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "name": "Evaluación trimestral",
  "description": "Control de progreso físico.",
  "creationDate": "2026-10-09T12:00:00Z"
}
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la evaluación no existe.

## 5. Eliminar una evaluación

```http
DELETE {BASE_URL}/evaluations/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
