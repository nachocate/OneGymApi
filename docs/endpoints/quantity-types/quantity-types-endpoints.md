# API de tipos de cantidad (`/quantity-types`)

Documentación para administrar las unidades empleadas en las cantidades de los ejercicios, por ejemplo kilogramos, metros o segundos.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. El nombre del tipo debe ser único.

## Estructura de un tipo de cantidad

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `name` | string | Sí | Nombre de la unidad, por ejemplo `Kilogramos`. |

**Body de ejemplo**

```json
{ "name": "Kilogramos" }
```

**Respuesta de ejemplo**

```json
{ "id": 1, "name": "Kilogramos" }
```

## 1. Listar tipos de cantidad

```http
GET {BASE_URL}/quantity-types
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
[
  { "id": 1, "name": "Kilogramos" },
  { "id": 2, "name": "Segundos" }
]
```

## 2. Obtener un tipo por ID

```http
GET {BASE_URL}/quantity-types/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{ "id": 1, "name": "Kilogramos" }
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el tipo no existe.

## 3. Crear un tipo de cantidad

```http
POST {BASE_URL}/quantity-types
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "name": "Kilogramos" }
```

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar un tipo de cantidad

```http
PUT {BASE_URL}/quantity-types/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "name": "Kg" }
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el tipo no existe.

## 5. Eliminar un tipo de cantidad

```http
DELETE {BASE_URL}/quantity-types/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
