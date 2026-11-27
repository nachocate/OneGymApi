# API de ejercicios de bloque (`/block-exercises`)

Documentación para administrar los ejercicios incluidos en cada bloque de entrenamiento.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Todos los endpoints requieren `Authorization: Bearer <accessToken>`.
- En `POST` y `PUT`, enviar `Content-Type: application/json`.
- Los IDs son `Long` autogenerados. No enviar `id` al crear.
- `blockId`, `exerciseId` y, cuando se informe, `quantityTypeId` deben referenciar recursos existentes.
- `position` define el orden del ejercicio dentro del bloque.

## Estructura de un ejercicio de bloque

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la API. |
| `blockId` | number | Sí | ID del bloque que contiene el ejercicio. |
| `exerciseId` | number | Sí | ID del ejercicio. |
| `quantityTypeId` | number \| null | Sí | Unidad de medida de `quantity`, o `null`. |
| `position` | number | Sí | Posición del ejercicio dentro del bloque. |
| `repetitions` | number \| null | Sí | Cantidad de repeticiones, o `null`. |
| `quantity` | number \| null | Sí | Valor de la carga, distancia o tiempo, o `null`. |

**Body de ejemplo**

```json
{
  "blockId": 1,
  "exerciseId": 1,
  "quantityTypeId": 1,
  "position": 1,
  "repetitions": 12,
  "quantity": 20.0
}
```

**Respuesta de ejemplo**

```json
{
  "id": 1,
  "blockId": 1,
  "exerciseId": 1,
  "quantityTypeId": 1,
  "position": 1,
  "repetitions": 12,
  "quantity": 20.0
}
```

## 1. Listar ejercicios de bloque

```http
GET {BASE_URL}/block-exercises
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un arreglo de objetos con la estructura anterior.

## 2. Obtener un ejercicio de bloque por ID

```http
GET {BASE_URL}/block-exercises/1
Authorization: Bearer <accessToken>
```

**Respuesta — `200 OK`**: un objeto con la estructura anterior.

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el recurso no existe.

## 3. Crear un ejercicio de bloque

```http
POST {BASE_URL}/block-exercises
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de ejemplo de la sección anterior. Se pueden usar valores nulos cuando no correspondan repeticiones, cantidad o unidad:

```json
{
  "blockId": 1,
  "exerciseId": 2,
  "quantityTypeId": null,
  "position": 2,
  "repetitions": 30,
  "quantity": null
}
```

**Respuesta — `201 Created`**

No devuelve body. Consultar luego el recurso mediante `GET`.

## 4. Actualizar un ejercicio de bloque

```http
PUT {BASE_URL}/block-exercises/1
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body completo del ejercicio de bloque. El ID de la URL determina el recurso a modificar.

**Respuesta — `204 No Content`**

**Errores:** `400 Bad Request` si el ID no es numérico; `404 Not Found` si el recurso no existe.

## 5. Eliminar un ejercicio de bloque

```http
DELETE {BASE_URL}/block-exercises/1
Authorization: Bearer <accessToken>
```

**Respuesta — `204 No Content`**

No devuelve body. Actualmente responde `204` aun si el ID no existía.
