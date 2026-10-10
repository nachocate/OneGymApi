# API de planes (`/plans`)

Documentación para administrar planes de entrenamiento asociados a un gimnasio.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `gymId` y `planTypeId` deben referenciar recursos existentes.
- `planRootId` puede ser `null`; si se informa, referencia otro plan y permite modelar una variante o derivación.

## Estructura de un plan

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `name` | string | Sí | Nombre del plan. |
| `gymId` | number | Sí | ID del gimnasio propietario. |
| `planTypeId` | number | Sí | ID del tipo de plan. |
| `planRootId` | number \| null | Sí | Plan de origen, o `null` si es un plan raíz. |

**Body de ejemplo**

```json
{
  "name": "Fuerza inicial",
  "gymId": 1,
  "planTypeId": 1,
  "planRootId": null
}
```

**Respuesta de ejemplo**

```json
{
  "id": 1,
  "name": "Fuerza inicial",
  "gymId": 1,
  "planTypeId": 1,
  "planRootId": null
}
```

## 1. Listar planes

```http
GET {BASE_URL}/plans
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener un plan por ID

```http
GET {BASE_URL}/plans/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**

```json
{
  "id": 1,
  "name": "Fuerza inicial",
  "gymId": 1,
  "planTypeId": 1,
  "planRootId": null
}
```

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el plan no existe.

## 3. Crear un plan

```http
POST {BASE_URL}/plans
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo de la sección anterior.

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar un plan

```http
PUT {BASE_URL}/plans/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Body de ejemplo**

```json
{
  "name": "Fuerza inicial - revisión 2",
  "gymId": 1,
  "planTypeId": 1,
  "planRootId": 1
}
```

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el plan no existe.

## 5. Eliminar un plan

```http
DELETE {BASE_URL}/plans/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
