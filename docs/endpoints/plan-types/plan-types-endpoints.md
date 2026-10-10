# API de tipos de plan (`/plan-types`)

Documentación para administrar las categorías de planes de entrenamiento.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. El nombre del tipo debe ser único.

## Estructura de un tipo de plan

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `name` | string | Sí | Nombre de la categoría, por ejemplo `Fuerza` o `Hipertrofia`. |

**Body de ejemplo**

```json
{ "name": "Fuerza" }
```

**Respuesta de ejemplo**

```json
{ "id": 1, "name": "Fuerza" }
```

## 1. Listar tipos de plan

```http
GET {BASE_URL}/plan-types
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
[
  { "id": 1, "name": "Fuerza" },
  { "id": 2, "name": "Hipertrofia" }
]
```

## 2. Obtener un tipo por ID

```http
GET {BASE_URL}/plan-types/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{ "id": 1, "name": "Fuerza" }
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el tipo no existe.

## 3. Crear un tipo de plan

```http
POST {BASE_URL}/plan-types
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "name": "Fuerza" }
```

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar un tipo de plan

```http
PUT {BASE_URL}/plan-types/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "name": "Fuerza y potencia" }
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el tipo no existe.

## 5. Eliminar un tipo de plan

```http
DELETE {BASE_URL}/plan-types/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
