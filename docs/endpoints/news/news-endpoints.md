# API de noticias (`/news`)

Documentación para la integración del frontend con los recursos de noticias de los gimnasios.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente. Por ejemplo: `http://localhost:8080`.
- **Autenticación:** todos los endpoints de este documento requieren un access token JWT válido.
- **Headers requeridos:**

  ```http
  Authorization: Bearer <accessToken>
  Content-Type: application/json
  ```

- Los IDs son números enteros (`Long`) generados por la base de datos. No se debe enviar `id` al crear una noticia.
- Los campos JSON usan `camelCase`.
- `gymId`, `title`, `description` y `date` son obligatorios. `imageUrl` admite `null`.
- `date` debe enviarse en formato ISO-8601 con zona horaria, por ejemplo `2026-10-09T10:00:00Z`.

## Estructura de una noticia

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `gymId` | number | Sí | ID del gimnasio al que pertenece la noticia. |
| `title` | string | Sí | Título de la noticia. Máximo 255 caracteres. |
| `description` | string | Sí | Contenido descriptivo de la noticia. |
| `date` | string | Sí | Fecha y hora de publicación en formato ISO-8601 con zona horaria. |
| `imageUrl` | string \| null | No | URL de una imagen asociada a la noticia. |

Ejemplo de noticia devuelta por la API:

```json
{
  "id": 1,
  "gymId": 1,
  "title": "Horario especial por feriado",
  "description": "El lunes abriremos de 09:00 a 14:00.",
  "date": "2026-10-09T10:00:00Z",
  "imageUrl": "https://cdn.ejemplo.com/news/horario-feriado.png"
}
```

## 1. Listar todas las noticias

```http
GET {BASE_URL}/news
Authorization: Bearer <accessToken>
```

**Respuesta exitosa — `200 OK`**

```json
[
  {
    "id": 1,
    "gymId": 1,
    "title": "Horario especial por feriado",
    "description": "El lunes abriremos de 09:00 a 14:00.",
    "date": "2026-10-09T10:00:00Z",
    "imageUrl": "https://cdn.ejemplo.com/news/horario-feriado.png"
  },
  {
    "id": 2,
    "gymId": 1,
    "title": "Nueva clase de movilidad",
    "description": "Sumamos una clase los miércoles a las 19:00.",
    "date": "2026-10-01T15:30:00Z",
    "imageUrl": null
  }
]
```

Si no existen noticias, responde `200 OK` con un arreglo vacío: `[]`.

## 2. Obtener una noticia por ID

```http
GET {BASE_URL}/news/1
Authorization: Bearer <accessToken>
```

**Respuesta exitosa — `200 OK`**

```json
{
  "id": 1,
  "gymId": 1,
  "title": "Horario especial por feriado",
  "description": "El lunes abriremos de 09:00 a 14:00.",
  "date": "2026-10-09T10:00:00Z",
  "imageUrl": "https://cdn.ejemplo.com/news/horario-feriado.png"
}
```

**Posibles respuestas**

- `400 Bad Request`: el parámetro `id` no es un número válido.
- `404 Not Found`: no existe una noticia con ese ID.

## 3. Crear una noticia

```http
POST {BASE_URL}/news
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "gymId": 1,
  "title": "Horario especial por feriado",
  "description": "El lunes abriremos de 09:00 a 14:00.",
  "date": "2026-10-09T10:00:00Z",
  "imageUrl": "https://cdn.ejemplo.com/news/horario-feriado.png"
}
```

Para crear una noticia sin imagen, enviar `imageUrl` como `null` u omitirlo:

```json
{
  "gymId": 1,
  "title": "Nueva clase de movilidad",
  "description": "Sumamos una clase los miércoles a las 19:00.",
  "date": "2026-10-01T15:30:00Z",
  "imageUrl": null
}
```

**Respuesta exitosa — `201 Created`**

Este endpoint no devuelve body. El ID se genera en la base de datos; para obtener la noticia creada se debe consultar luego `GET /news` o `GET /news/{id}`.

## 4. Actualizar una noticia

La actualización es completa: enviar todos los campos obligatorios de la noticia. El valor de `id` del body no se usa; el ID válido es el de la URL.

```http
PUT {BASE_URL}/news/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "gymId": 1,
  "title": "Horario especial actualizado",
  "description": "El lunes abriremos de 10:00 a 14:00.",
  "date": "2026-10-09T11:00:00Z",
  "imageUrl": "https://cdn.ejemplo.com/news/horario-feriado-v2.png"
}
```

Para quitar la imagen asociada, enviar `imageUrl` explícitamente como `null`:

```json
{
  "gymId": 1,
  "title": "Horario especial actualizado",
  "description": "El lunes abriremos de 10:00 a 14:00.",
  "date": "2026-10-09T11:00:00Z",
  "imageUrl": null
}
```

**Respuesta exitosa — `204 No Content`**

No devuelve body.

**Posibles respuestas**

- `400 Bad Request`: el parámetro `id` no es un número válido.
- `404 Not Found`: no existe una noticia con ese ID.

## 5. Eliminar una noticia

```http
DELETE {BASE_URL}/news/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente la API responde `204` incluso si no existía una noticia con ese ID.

**Posibles respuestas**

- `400 Bad Request`: el parámetro `id` no es un número válido.
- `401 Unauthorized`: el token falta, venció o no es válido. Aplica a todos los endpoints de este documento.

## Ruta de noticias para usuarios suscriptos

Además del CRUD general, existe una ruta orientada a la aplicación del usuario final. Devuelve solamente las noticias de un gimnasio al que el usuario autenticado tiene una membresía activa, ordenadas de la más reciente a la más antigua.

### `GET /{gymId}/news`

```http
GET {BASE_URL}/1/news
Authorization: Bearer <accessToken>
```

**Respuesta exitosa — `200 OK`**

```json
[
  {
    "id": 1,
    "gymId": 1,
    "title": "Horario especial por feriado",
    "description": "El lunes abriremos de 09:00 a 14:00.",
    "date": "2026-10-09T10:00:00Z",
    "imageUrl": "https://cdn.ejemplo.com/news/horario-feriado.png"
  }
]
```

**Posibles respuestas**

- `400 Bad Request`: `gymId` no es numérico.
- `403 Forbidden`: el usuario no tiene una membresía activa en el gimnasio indicado.
- `401 Unauthorized`: el token falta, venció o no es válido.
