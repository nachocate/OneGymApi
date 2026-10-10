# API de tipos de entrenador (`/coach-types`)

Documentación para administrar las especialidades o tipos de entrenador.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. El nombre del tipo debe ser único.

## Estructura de un tipo de entrenador

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `name` | string | Sí | Nombre del tipo, por ejemplo `Entrenador funcional`. |

**Body de ejemplo**

```json
{ "name": "Entrenador funcional" }
```

**Respuesta de ejemplo**

```json
{ "id": 1, "name": "Entrenador funcional" }
```

## 1. Listar tipos de entrenador

```http
GET {BASE_URL}/coach-types
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
[
  { "id": 1, "name": "Entrenador funcional" },
  { "id": 2, "name": "Nutricionista" }
]
```

## 2. Obtener un tipo por ID

```http
GET {BASE_URL}/coach-types/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{ "id": 1, "name": "Entrenador funcional" }
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el tipo no existe.

## 3. Crear un tipo de entrenador

```http
POST {BASE_URL}/coach-types
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "name": "Entrenador funcional" }
```

**Respuesta — `201 Created`**

No devuelve body; consultar luego el recurso mediante `GET`.

## 4. Actualizar un tipo de entrenador

```http
PUT {BASE_URL}/coach-types/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "name": "Preparador físico" }
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el tipo no existe.

## 5. Eliminar un tipo de entrenador

```http
DELETE {BASE_URL}/coach-types/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
