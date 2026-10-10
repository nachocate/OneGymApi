# API de bloques de entrenamiento (`/blocks`)

Documentación para administrar los bloques que agrupan ejercicios dentro de un día de un plan.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `dayId` debe referenciar un día existente.
- `position` define el orden de visualización del bloque dentro del día.

## Estructura de un bloque

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `position` | number | Sí | Posición del bloque dentro del día. |
| `name` | string \| null | Sí | Nombre opcional del bloque. |
| `dayId` | number | Sí | ID del día al que pertenece. |
| `laps` | number | Sí | Cantidad de vueltas que debe realizar el bloque. |

**Body de ejemplo**

```json
{
  "position": 1,
  "name": "Entrada en calor",
  "dayId": 1,
  "laps": 3
}
```

**Respuesta de ejemplo**

```json
{
  "id": 1,
  "position": 1,
  "name": "Entrada en calor",
  "dayId": 1,
  "laps": 3
}
```

## 1. Listar bloques

```http
GET {BASE_URL}/blocks
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener un bloque por ID

```http
GET {BASE_URL}/blocks/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{
  "id": 1,
  "position": 1,
  "name": "Entrada en calor",
  "dayId": 1,
  "laps": 3
}
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el bloque no existe.

## 3. Crear un bloque

```http
POST {BASE_URL}/blocks
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo de la sección anterior. Para un bloque sin nombre, enviar `"name": null`.

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar un bloque

```http
PUT {BASE_URL}/blocks/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "position": 2,
  "name": "Circuito principal",
  "dayId": 1,
  "laps": 4
}
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el bloque no existe.

## 5. Eliminar un bloque

```http
DELETE {BASE_URL}/blocks/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
