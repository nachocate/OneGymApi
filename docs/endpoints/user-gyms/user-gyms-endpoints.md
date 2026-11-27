# API de membresías usuario-gimnasio (`/user-gyms`)

Documentación para administrar el vínculo entre un usuario, un gimnasio y el rol que desempeña en él.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `userId`, `gymId` y `roleId` deben referenciar recursos existentes.
- Las fechas usan formato `YYYY-MM-DD`. Si se informa `endDate`, debe ser igual o posterior a `startDate`.

## Estructura de una membresía

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `userId` | number | Sí | ID del usuario miembro. |
| `gymId` | number | Sí | ID del gimnasio. |
| `roleId` | number | Sí | ID del rol dentro del gimnasio. |
| `startDate` | string | Sí | Inicio de la membresía, `YYYY-MM-DD`. |
| `endDate` | string \| null | Sí | Fin de la membresía, o `null` si permanece vigente. |

**Body de ejemplo**

```json
{
  "userId": 1,
  "gymId": 1,
  "roleId": 1,
  "startDate": "2026-01-01",
  "endDate": null
}
```

**Respuesta de ejemplo**

```json
{
  "id": 1,
  "userId": 1,
  "gymId": 1,
  "roleId": 1,
  "startDate": "2026-01-01",
  "endDate": null
}
```

## 1. Listar membresías

```http
GET {BASE_URL}/user-gyms
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener una membresía por ID

```http
GET {BASE_URL}/user-gyms/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{
  "id": 1,
  "userId": 1,
  "gymId": 1,
  "roleId": 1,
  "startDate": "2026-01-01",
  "endDate": null
}
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la membresía no existe.

## 3. Crear una membresía

```http
POST {BASE_URL}/user-gyms
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo de la sección anterior.

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar una membresía

```http
PUT {BASE_URL}/user-gyms/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "userId": 1,
  "gymId": 1,
  "roleId": 2,
  "startDate": "2026-01-01",
  "endDate": "2026-12-31"
}
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la membresía no existe.

## 5. Eliminar una membresía

```http
DELETE {BASE_URL}/user-gyms/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
