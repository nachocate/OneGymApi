# API de tests de usuario (`/user-tests`)

Documentación para registrar el resultado de un usuario en un ejercicio dentro de una evaluación.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `exerciseId`, `userId`, `evaluationId` y, cuando se informe, `quantityTypeId` deben referenciar recursos existentes.
- `date` debe usar formato ISO-8601 con zona horaria, por ejemplo `2026-10-09T12:00:00Z`.

## Estructura de un test de usuario

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `exerciseId` | number | Sí | ID del ejercicio evaluado. |
| `userId` | number | Sí | ID del usuario evaluado. |
| `evaluationId` | number | Sí | ID de la evaluación. |
| `quantityTypeId` | number \| null | Sí | Unidad de `quantity`, o `null`. |
| `repetitions` | number \| null | Sí | Cantidad de repeticiones, o `null`. |
| `quantity` | number \| null | Sí | Valor medido, o `null`. |
| `date` | string | Sí | Fecha y hora del test en formato ISO-8601. |
| `description` | string | Sí | Observaciones del resultado. |

**Body de ejemplo**

```json
{
  "exerciseId": 1,
  "userId": 1,
  "evaluationId": 1,
  "quantityTypeId": 1,
  "repetitions": 12,
  "quantity": 40.0,
  "date": "2026-10-09T12:00:00Z",
  "description": "Serie de evaluación inicial."
}
```

**Respuesta de ejemplo**

```json
{
  "id": 1,
  "exerciseId": 1,
  "userId": 1,
  "evaluationId": 1,
  "quantityTypeId": 1,
  "repetitions": 12,
  "quantity": 40.0,
  "date": "2026-10-09T12:00:00Z",
  "description": "Serie de evaluación inicial."
}
```

## 1. Listar tests de usuario

```http
GET {BASE_URL}/user-tests
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener un test por ID

```http
GET {BASE_URL}/user-tests/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un objeto con la estructura anterior.

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el test no existe.

## 3. Crear un test de usuario

```http
POST {BASE_URL}/user-tests
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo de la sección anterior. Las métricas no utilizadas pueden enviarse como `null`:

```json
{
  "exerciseId": 2,
  "userId": 1,
  "evaluationId": 1,
  "quantityTypeId": null,
  "repetitions": 30,
  "quantity": null,
  "date": "2026-10-09T12:00:00Z",
  "description": "Test de repeticiones máximas."
}
```

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar un test de usuario

```http
PUT {BASE_URL}/user-tests/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body completo del test. El ID de la URL determina el recurso a modificar.

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el test no existe.

## 5. Eliminar un test de usuario

```http
DELETE {BASE_URL}/user-tests/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
