# API de ejercicios (`/exercises`)

Documentación para administrar el catálogo de ejercicios globales o propios de un gimnasio.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- Un ejercicio global debe usar `gymId: null` e `isVisibleGlobal: true`.
- Un ejercicio específico de un gimnasio debe informar el `gymId` correspondiente. `isVisibleGlobal` controla si también puede verse globalmente.

## Estructura de un ejercicio

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `name` | string | Sí | Nombre del ejercicio. |
| `description` | string \| null | Sí | Descripción o indicaciones. |
| `videoUrl` | string \| null | Sí | URL de video demostrativo. |
| `imageUrl` | string \| null | Sí | URL de imagen del ejercicio. |
| `gymId` | number \| null | Sí | Gimnasio propietario o `null` para ejercicio global. |
| `isVisibleGlobal` | boolean | Sí | Indica si el ejercicio es visible globalmente. Por defecto es `true`. |

**Body de ejemplo**

```json
{
  "name": "Sentadilla con barra",
  "description": "Descender manteniendo la espalda neutra.",
  "videoUrl": "https://cdn.ejemplo.com/videos/sentadilla.mp4",
  "imageUrl": "https://cdn.ejemplo.com/images/sentadilla.png",
  "gymId": 1,
  "isVisibleGlobal": false
}
```

**Respuesta de ejemplo**

```json
{
  "id": 1,
  "name": "Sentadilla con barra",
  "description": "Descender manteniendo la espalda neutra.",
  "videoUrl": "https://cdn.ejemplo.com/videos/sentadilla.mp4",
  "imageUrl": "https://cdn.ejemplo.com/images/sentadilla.png",
  "gymId": 1,
  "isVisibleGlobal": false
}
```

## 1. Listar ejercicios

```http
GET {BASE_URL}/exercises
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener un ejercicio por ID

```http
GET {BASE_URL}/exercises/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un objeto con la estructura de respuesta indicada arriba.

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el ejercicio no existe.

## 3. Crear un ejercicio

```http
POST {BASE_URL}/exercises
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo indicado arriba. Para un ejercicio global:

```json
{
  "name": "Plancha frontal",
  "description": null,
  "videoUrl": null,
  "imageUrl": null,
  "gymId": null,
  "isVisibleGlobal": true
}
```

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar un ejercicio

```http
PUT {BASE_URL}/exercises/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body completo del ejercicio. El ID de la URL determina el recurso a modificar.

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el ejercicio no existe.

## 5. Eliminar un ejercicio

```http
DELETE {BASE_URL}/exercises/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
