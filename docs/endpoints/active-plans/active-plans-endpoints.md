# API de planes activos del usuario (`/plan/{gymId}/me`)

Documentación para obtener los planes activos del usuario autenticado dentro de un gimnasio, incluyendo semanas, días, bloques y ejercicios.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Requiere `Authorization: Bearer <accessToken>`.
- El usuario autenticado debe tener una membresía activa en el gimnasio indicado.
- Solo se devuelven planes que tengan una actividad marcada como `isActive: true`.

## Consultar mis planes activos en un gimnasio

```http
GET {BASE_URL}/plan/1/me
Authorization: Bearer <accessToken>
```

**Respuesta exitosa — `200 OK`**

```json
{
  "plans": [
    {
      "id": 1,
      "name": "Fuerza inicial",
      "isActive": true,
      "weeks": [
        {
          "id": 1,
          "number": 1,
          "isActive": true,
          "days": [
            {
              "id": 1,
              "number": 1,
              "isActive": true,
              "blocks": [
                {
                  "id": 1,
                  "position": 1,
                  "name": "Entrada en calor",
                  "laps": 3,
                  "exercises": [
                    {
                      "id": 1,
                      "exerciseId": 1,
                      "name": "Sentadilla con barra",
                      "repetitions": 12,
                      "quantity": 20.0,
                      "quantityType": {
                        "id": 1,
                        "name": "Kilogramos"
                      },
                      "position": 1
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `plans` | array | Planes activos del usuario. Puede ser `[]`. |
| `plans[].isActive` | boolean | Siempre `true` para los planes incluidos. |
| `weeks[].isActive` | boolean | Indica si la semana es la seleccionada o contiene el día activo. |
| `days[].isActive` | boolean | Indica si el día es el seleccionado en la actividad activa. |
| `blocks[].laps` | number | Cantidad de vueltas del bloque. |
| `exercises[].id` | number | ID de la asignación ejercicio-bloque, no el ID del ejercicio. |
| `exercises[].exerciseId` | number | ID del ejercicio del catálogo. |
| `exercises[].quantityType` | object \| null | Unidad de `quantity`, o `null` cuando no corresponde. |

**Ejemplo sin planes activos**

```json
{ "plans": [] }
```

**Posibles respuestas**

- `400 Bad Request`: `gymId` no es un número válido.
- `401 Unauthorized`: el token falta, venció o no es válido.
- `403 Forbidden`: el usuario no tiene una membresía activa en el gimnasio.
