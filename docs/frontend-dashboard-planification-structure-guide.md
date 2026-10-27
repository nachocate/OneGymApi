# Visualización de estructura de planificación — Dashboard

Esta guía explica cómo mostrar una planificación completa en el dashboard: semanas, días, bloques y ejercicios. Para editar y guardar cambios, consultar también la [guía de actualización de planificaciones](frontend-dashboard-planification-update-guide.md).

## Endpoint

```http
GET /gyms/{gymId}/plans/{planId}/structure
Authorization: Bearer <accessToken>
```

- `gymId`: gimnasio seleccionado en el dashboard.
- `planId`: identificador del plan elegido en el listado.
- Requiere una membresía activa en el gimnasio o rol `SUPER_ADMIN`.
- Responde `200 OK` con el árbol completo.

## Flujo de pantalla

```text
Listado de planes
    ↓ usuario elige un plan
GET /gyms/{gymId}/plans/{planId}/structure
    ↓
Estado de carga → árbol recibido → pantalla de detalle
```

No intentar reconstruir el árbol desde `GET /gyms/{gymId}/plans`: esa ruta está optimizada para el listado y solo devuelve datos resumidos, como `name`, `weekCount` y `revision`.

## Ejemplo de respuesta

```json
{
  "id": 100,
  "name": "Fuerza inicial - 4 semanas",
  "gymId": 1,
  "planTypeId": 1,
  "planRootId": null,
  "revision": 3,
  "weeks": [
    {
      "id": 200,
      "number": 1,
      "days": [
        {
          "id": 300,
          "number": 1,
          "blocks": [
            {
              "id": 400,
              "position": 1,
              "name": "Entrada en calor",
              "laps": 3,
              "exercises": [
                {
                  "id": 500,
                  "exerciseId": 25,
                  "quantityTypeId": 1,
                  "position": 1,
                  "repetitions": 12,
                  "quantity": 20.0
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

## Qué representa cada nivel

```text
Planificación
 └─ weeks[]              → semanas de la rutina
     └─ days[]           → días dentro de una semana
         └─ blocks[]     → circuitos o bloques del día
             └─ exercises[] → ejercicios del bloque
```

| Campo | Uso visual |
| --- | --- |
| `weeks[].number` | Título: “Semana 1”, “Semana 2”, etc. |
| `days[].number` | Título: “Día 1”, “Día 2”, etc. |
| `blocks[].name` | Nombre visible del bloque; puede ser `null`. Usar “Bloque {position}” como fallback. |
| `blocks[].position` | Orden de los bloques del día. |
| `blocks[].laps` | Cantidad de vueltas del bloque. |
| `exercises[].position` | Orden de los ejercicios dentro del bloque. |
| `exerciseId` | ID para buscar nombre, imagen y descripción en el catálogo del gimnasio. |
| `repetitions` | Repeticiones; puede ser `null`. |
| `quantity` | Carga, tiempo, distancia u otra cantidad; puede ser `null`. |
| `quantityTypeId` | Unidad asociada a `quantity`; puede ser `null`. |

## Cargar nombres de ejercicios y unidades

La estructura contiene IDs porque evita repetir datos del catálogo. Cargar en paralelo:

```http
GET /gyms/{gymId}/exercises?limit=100&offset=0
GET /quantity-types
```

Crear mapas en el estado del frontend:

```text
exercisesById[exercise.id] = exercise
quantityTypesById[quantityType.id] = quantityType
```

Entonces, para cada ejercicio de un bloque:

```text
exercise = exercisesById[item.exerciseId]
unit     = quantityTypesById[item.quantityTypeId]
```

Ejemplo de renderizado conceptual:

```text
Sentadilla — 3 x 12 — 20 kg
```

Si `repetitions`, `quantity` o `quantityTypeId` es `null`, no mostrar un valor inventado. Si falta el ejercicio en el mapa, mostrar “Ejercicio no disponible” y permitir recargar el catálogo.

## Estados de interfaz

| Estado | Comportamiento |
| --- | --- |
| Cargando | Mostrar skeleton/spinner; no habilitar Editar todavía. |
| Éxito | Ordenar visualmente por `number` y `position`; la API ya los devuelve ordenados. |
| `401` | Intentar refresh una vez; si falla, cerrar sesión. |
| `403` | Informar que no existe membresía activa o permiso para ver el plan. |
| `404` | Mostrar que el plan no existe o no pertenece al gimnasio seleccionado; volver al listado. |
| Error de red | Mostrar Reintentar sin cambiar el gimnasio ni descartar el plan seleccionado. |

## Si la pantalla también permite editar

Conservar exactamente los IDs de todos los nodos y el campo `revision`. Al guardar, el editor usa:

```http
PUT /gyms/{gymId}/plans/{planId}
```

No enviar un `PUT` desde una pantalla de solo lectura. En el editor, el `409 Conflict` indica que otro usuario guardó una versión más reciente: recargar el árbol antes de volver a intentar.

## Checklist

- [ ] La pantalla toma `gymId` del selector activo.
- [ ] Usa `GET .../structure`, no el listado resumido, para el detalle.
- [ ] Carga catálogo de ejercicios y tipos de cantidad para resolver IDs.
- [ ] Renderiza `null` de forma segura.
- [ ] Maneja `401`, `403`, `404` y error de red.
- [ ] Si existe edición, conserva IDs y `revision`.
