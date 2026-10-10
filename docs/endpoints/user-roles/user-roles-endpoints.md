# API de roles de usuario (`/user-roles`)

Documentación para administrar los roles que se asignan a un usuario dentro de un gimnasio.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. El nombre del rol debe ser único.

## Estructura de un rol

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `name` | string | Sí | Nombre del rol, por ejemplo `Alumno`, `Entrenador` o `Administrador`. |

**Body de ejemplo**

```json
{ "name": "Alumno" }
```

**Respuesta de ejemplo**

```json
{ "id": 1, "name": "Alumno" }
```

## 1. Listar roles

```http
GET {BASE_URL}/user-roles
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
[
  { "id": 1, "name": "Alumno" },
  { "id": 2, "name": "Administrador" }
]
```

## 2. Obtener un rol por ID

```http
GET {BASE_URL}/user-roles/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{ "id": 1, "name": "Alumno" }
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el rol no existe.

## 3. Crear un rol

```http
POST {BASE_URL}/user-roles
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "name": "Alumno" }
```

**Respuesta — `201 Created`**

No devuelve body; consultar luego el recurso mediante `GET`.

## 4. Actualizar un rol

```http
PUT {BASE_URL}/user-roles/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body**

```json
{ "name": "Socio" }
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el rol no existe.

## 5. Eliminar un rol

```http
DELETE {BASE_URL}/user-roles/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
