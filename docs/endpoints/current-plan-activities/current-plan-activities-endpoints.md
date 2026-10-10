# API de actividad actual de planes (`/current-plan-activities`)

Documentación para registrar y consultar el estado activo de un plan asignado a un usuario.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `userPlanId` debe referenciar una asignación de plan existente. Si se informan, `activeWeekId` y `activeDayId` deben referenciar los recursos correspondientes.
- `date` debe usar formato ISO-8601 con zona horaria, por ejemplo `2026-10-09T12:00:00Z`.

## Estructura de una actividad de plan

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `date` | string | Sí | Fecha y hora del registro en formato ISO-8601. |
| `userPlanId` | number | Sí | ID de la asignación usuario-plan. |
| `activeWeekId` | number \| null | Sí | Semana activa, o `null`. |
| `activeDayId` | number \| null | Sí | Día activo, o `null`. |
| `isActive` | boolean | Sí | Indica si esta actividad está activa. Por defecto es `false`. |

**Body de ejemplo**

```json
{
  "date": "2026-10-09T12:00:00Z",
  "userPlanId": 1,
  "activeWeekId": 1,
  "activeDayId": 1,
  "isActive": true
}
```

**Respuesta de ejemplo**

```json
{
  "id": 1,
  "date": "2026-10-09T12:00:00Z",
  "userPlanId": 1,
  "activeWeekId": 1,
  "activeDayId": 1,
  "isActive": true
}
```

## 1. Listar actividades de plan

```http
GET {BASE_URL}/current-plan-activities
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener una actividad por ID

```http
GET {BASE_URL}/current-plan-activities/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un objeto con la estructura anterior.

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la actividad no existe.

## 3. Crear una actividad de plan

```http
POST {BASE_URL}/current-plan-activities
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo de la sección anterior. Para una actividad sin semana ni día seleccionado:

```json
{
  "date": "2026-10-09T12:00:00Z",
  "userPlanId": 1,
  "activeWeekId": null,
  "activeDayId": null,
  "isActive": false
}
```

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar una actividad de plan

```http
PUT {BASE_URL}/current-plan-activities/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body completo de la actividad. El ID de la URL determina el recurso a modificar.

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si la actividad no existe.

## 5. Eliminar una actividad de plan

```http
DELETE {BASE_URL}/current-plan-activities/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
