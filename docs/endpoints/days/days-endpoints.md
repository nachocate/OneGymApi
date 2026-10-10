# API de días de plan (`/days`)

Documentación para administrar los días que componen una semana de entrenamiento.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `weekId` debe referenciar una semana existente.

## Estructura de un día

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `number` | number | Sí | Número de día dentro de la semana. |
| `weekId` | number | Sí | ID de la semana a la que pertenece. |

**Body de ejemplo**

```json
{ "number": 1, "weekId": 1 }
```

**Respuesta de ejemplo**

```json
{ "id": 1, "number": 1, "weekId": 1 }
```

## 1. Listar días

```http
GET {BASE_URL}/days
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
[
  { "id": 1, "number": 1, "weekId": 1 },
  { "id": 2, "number": 2, "weekId": 1 }
]
```

## 2. Obtener un día por ID

```http
GET {BASE_URL}/days/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{ "id": 1, "number": 1, "weekId": 1 }
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el día no existe.

## 3. Crear un día

```http
POST {BASE_URL}/days
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "number": 1, "weekId": 1 }
```

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar un día

```http
PUT {BASE_URL}/days/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "number": 2, "weekId": 1 }
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el día no existe.

## 5. Eliminar un día

```http
DELETE {BASE_URL}/days/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
