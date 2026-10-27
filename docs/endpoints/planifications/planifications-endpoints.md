# API de planificaciones atómicas

Estas rutas permiten al dashboard crear, leer, actualizar, clonar y asignar una rutina completa sin dejar semanas, días, bloques o ejercicios parcialmente persistidos. Todas requieren `Authorization: Bearer <accessToken>`.

## Permisos

- Lectura de estructura: cualquier miembro activo del gimnasio o `SUPER_ADMIN`.
- Crear, actualizar, clonar y asignar: `COACH`, `ADMIN`, `OWNER` del gimnasio o `SUPER_ADMIN`.
- Consultar las asignaciones de otro miembro: `COACH` o superior del gimnasio.

## Árbol de planificación

El body de creación y actualización contiene el árbol completo. Los nodos nuevos no llevan `id`; en una actualización los nodos existentes conservan su `id`.

```json
{
  "name": "Fuerza inicial - 4 semanas",
  "planTypeId": 1,
  "planRootId": null,
  "revision": 3,
  "weeks": [
    {
      "number": 1,
      "days": [
        {
          "number": 1,
          "blocks": [
            {
              "position": 1,
              "name": "Entrada en calor",
              "laps": 3,
              "exercises": [
                {
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

La respuesta incluye todos los IDs generados y la `revision` actual. En `PUT`, enviar la `revision` recibida en la última lectura o guardado. Los nodos que no figuren en el body se eliminan como parte de la misma transacción. Si tienen dependencias que impiden borrarlos, toda la actualización se revierte y responde `422`.

El árbol debe tener al menos una semana, día, bloque y ejercicio. Los números de semanas/días y posiciones de bloques/ejercicios deben ser positivos y únicos dentro de su padre. Un ejercicio debe pertenecer al gimnasio o ser global.

## Endpoints

### Listar planificaciones del gimnasio

```http
GET /gyms/{gymId}/plans?limit=20&offset=0
Authorization: Bearer <accessToken>
```

Responde un listado liviano, adecuado para la pantalla de catálogo. No carga semanas, días ni ejercicios:

```json
[
  {
    "id": 100,
    "name": "Fuerza inicial - 4 semanas",
    "gymId": 1,
    "planTypeId": 1,
    "planRootId": null,
    "revision": 3,
    "weekCount": 4
  }
]
```

Usar `GET /gyms/{gymId}/plans/{planId}/structure` al abrir el editor.

### Obtener estructura

```http
GET /gyms/{gymId}/plans/{planId}/structure
Authorization: Bearer <accessToken>
```

Responde `200 OK` con el árbol completo y `404` si el plan no pertenece al gimnasio indicado.

### Crear planificación completa

```http
POST /gyms/{gymId}/plans
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body de árbol. Responde `201 Created` con la planificación persistida y todos sus IDs.

### Reemplazar planificación completa

```http
PUT /gyms/{gymId}/plans/{planId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el árbol completo. Responde `200 OK` con el árbol actualizado. No se permite mover el plan a otro gimnasio mediante este endpoint.

Si la `revision` no coincide con la versión actual, responde `409 Conflict`. El cliente debe volver a pedir la estructura y permitir al usuario decidir cómo continuar; no debe reintentar sobrescribiendo datos automáticamente.

### Clonar

```http
POST /gyms/{gymId}/plans/{planId}/clone
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Body opcional para dar otro nombre:

```json
{ "name": "Fuerza inicial - copia" }
```

Enviar `{}` para usar el nombre automático. Responde `201 Created` con el nuevo árbol. La copia referencia el plan original mediante `planRootId`.

### Asignar a un miembro

```http
POST /gyms/{gymId}/plans/{planId}/assignments
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{ "userId": 42 }
```

El usuario debe tener una membresía activa en el gimnasio. Responde `201 Created`:

```json
{ "id": 900, "userId": 42, "planId": 100 }
```

### Listar asignaciones de un miembro en un gimnasio

```http
GET /gyms/{gymId}/members/{userId}/plans
Authorization: Bearer <accessToken>
```

Responde `200 OK` con una lista de `UserPlan`:

```json
[{ "id": 900, "userId": 42, "planId": 100 }]
```

## Errores

- `400 Bad Request`: árbol incompleto, posiciones/números duplicados, datos inválidos o IDs de nodos que no pertenecen al padre indicado.
- `401 Unauthorized`: JWT inválido, ausente o vencido.
- `403 Forbidden`: rol o membresía insuficiente.
- `404 Not Found`: gimnasio, plan, usuario, ejercicio, tipo de plan o unidad inexistentes.
- `409 Conflict`: plan ya asignado al mismo usuario, o la `revision` del `PUT` está desactualizada.
- `422 Unprocessable Entity`: una restricción de PostgreSQL impidió persistir o eliminar parte del árbol; la transacción se revierte completa.
