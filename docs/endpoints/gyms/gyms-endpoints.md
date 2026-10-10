# API de gimnasios (`/gyms`)

Documentación para la integración del frontend con los recursos de gimnasios.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente. Por ejemplo: `http://localhost:8080`.
- **Autenticación:** todos los endpoints requieren un access token JWT válido.
- **Headers requeridos:**

  ```http
  Authorization: Bearer <accessToken>
  Content-Type: application/json
  ```

- Los IDs son números enteros (`Long`) generados por la base de datos. No se debe enviar `id` al crear un gimnasio.
- Los campos JSON usan `camelCase`.
- `name` es obligatorio. Los demás campos admiten `null`.

## Estructura de un gimnasio

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `name` | string | Sí | Nombre del gimnasio. Máximo 150 caracteres. |
| `description` | string \| null | No | Descripción del gimnasio. |
| `schedule` | string \| null | No | Horarios de atención en formato de texto. |
| `address` | string \| null | No | Dirección. |
| `phone` | string \| null | No | Teléfono de contacto. |
| `logoUrl` | string \| null | No | URL del logo. |
| `bannerUrl` | string \| null | No | URL de la imagen de portada. |

Ejemplo de gimnasio devuelto por la API:

```json
{
  "id": 1,
  "name": "Forze Gym",
  "description": "Gimnasio de entrenamiento funcional y fuerza.",
  "schedule": "Lunes a viernes 07:00-22:00; sábados 09:00-14:00",
  "address": "Av. Siempre Viva 123",
  "phone": "+54 11 5555-0101",
  "logoUrl": null,
  "bannerUrl": null
}
```

## 1. Listar gimnasios

```http
GET {BASE_URL}/gyms
Authorization: Bearer <accessToken>
```

**Respuesta exitosa — `200 OK`**

```json
[
  {
    "id": 1,
    "name": "Forze Gym",
    "description": "Gimnasio de entrenamiento funcional y fuerza.",
    "schedule": "Lunes a viernes 07:00-22:00; sábados 09:00-14:00",
    "address": "Av. Siempre Viva 123",
    "phone": "+54 11 5555-0101",
    "logoUrl": null,
    "bannerUrl": null
  }
]
```

Si no existen gimnasios, responde `200 OK` con un arreglo vacío: `[]`.

## 2. Obtener un gimnasio por ID

```http
GET {BASE_URL}/gyms/1
Authorization: Bearer <accessToken>
```

**Respuesta exitosa — `200 OK`**

```json
{
  "id": 1,
  "name": "Forze Gym",
  "description": "Gimnasio de entrenamiento funcional y fuerza.",
  "schedule": "Lunes a viernes 07:00-22:00; sábados 09:00-14:00",
  "address": "Av. Siempre Viva 123",
  "phone": "+54 11 5555-0101",
  "logoUrl": null,
  "bannerUrl": null
}
```

**Posibles respuestas**

- `400 Bad Request`: el parámetro `id` no es un número válido.
- `404 Not Found`: no existe un gimnasio con ese ID.

## 3. Crear un gimnasio

```http
POST {BASE_URL}/gyms
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "name": "Forze Gym",
  "description": "Gimnasio de entrenamiento funcional y fuerza.",
  "schedule": "Lunes a viernes 07:00-22:00; sábados 09:00-14:00",
  "address": "Av. Siempre Viva 123",
  "phone": "+54 11 5555-0101",
  "logoUrl": "https://cdn.ejemplo.com/gyms/forze/logo.png",
  "bannerUrl": "https://cdn.ejemplo.com/gyms/forze/banner.png"
}
```

**Respuesta exitosa — `201 Created`**

Este endpoint no devuelve body. El ID se genera en la base de datos; para obtener el recurso creado se debe consultar luego `GET /gyms` o `GET /gyms/{id}`.

## 4. Actualizar un gimnasio

La actualización es completa: enviar todos los campos del gimnasio. El valor de `id` del body no se usa; el ID válido es el de la URL.

```http
PUT {BASE_URL}/gyms/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "name": "Forze Gym Palermo",
  "description": "Entrenamiento funcional, fuerza y movilidad.",
  "schedule": "Lunes a viernes 06:30-22:30; sábados 09:00-14:00",
  "address": "Av. Siempre Viva 123, Palermo",
  "phone": "+54 11 5555-0101",
  "logoUrl": "https://cdn.ejemplo.com/gyms/forze/logo.png",
  "bannerUrl": "https://cdn.ejemplo.com/gyms/forze/banner-nuevo.png"
}
```

Para borrar un valor opcional, enviarlo explícitamente como `null`:

```json
{
  "name": "Forze Gym Palermo",
  "description": null,
  "schedule": null,
  "address": null,
  "phone": null,
  "logoUrl": null,
  "bannerUrl": null
}
```

**Respuesta exitosa — `204 No Content`**

No devuelve body.

**Posibles respuestas**

- `400 Bad Request`: el parámetro `id` no es un número válido.
- `404 Not Found`: no existe un gimnasio con ese ID.

## 5. Eliminar un gimnasio

```http
DELETE {BASE_URL}/gyms/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente la API responde `204` incluso si no existía un gimnasio con ese ID.

**Posibles respuestas**

- `400 Bad Request`: el parámetro `id` no es un número válido.
- `401 Unauthorized`: el token falta, venció o no es válido. Aplica a todos los endpoints de este documento.

