# API de semanas de plan (`/weeks`)

Documentación para administrar las semanas que componen un plan de entrenamiento.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `planId` debe referenciar un plan existente.

## Estructura de una semana

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `number` | number | Sí | Número de semana dentro del plan. |
| `planId` | number | Sí | ID del plan al que pertenece. |

**Body de ejemplo**

```json
{ "number": 1, "planId": 1 }
```

**Respuesta de ejemplo**

```json
{ "id": 1, "number": 1, "planId": 1 }
```

## 1. Listar semanas

```http
GET {BASE_URL}/weeks
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
[
  { "id": 1, "number": 1, "planId": 1 },
  { "id": 2, "number": 2, "planId": 1 }
]
```

## 2. Obtener una semana por ID

```http
GET {BASE_URL}/weeks/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{ "id": 1, "number": 1, "planId": 1 }
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la semana no existe.

## 3. Crear una semana

```http
POST {BASE_URL}/weeks
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "number": 1, "planId": 1 }
```

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar una semana

```http
PUT {BASE_URL}/weeks/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "number": 2, "planId": 1 }
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la semana no existe.

## 5. Eliminar una semana

```http
DELETE {BASE_URL}/weeks/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
