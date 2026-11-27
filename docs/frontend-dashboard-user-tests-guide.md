# Tests de usuario — Dashboard

Un test registra una medición concreta de un usuario dentro de una evaluación: por ejemplo, peso levantado, repeticiones máximas, distancia o tiempo. No es un registro de entrenamiento cotidiano ni una evaluación descriptiva.

```text
Usuario
  └─ Evaluación
      └─ Test(s) de ejercicios con mediciones
```

## Permisos

Todas las rutas requieren:

```http
Authorization: Bearer <accessToken>
```

- El usuario titular o cualquier persona que comparte un gimnasio activo con él puede leer sus tests.
- Crear y editar exige `COACH`, `ADMIN`, `OWNER` o `SUPER_ADMIN` que comparta al menos un gimnasio activo con el usuario.
- Por decisión de producto actual, el dashboard no debe mostrar borrado de tests, aunque el backend lo soporte para usuarios autorizados.

## 1. Cargar el perfil de mediciones

Al seleccionar un miembro, cargar en paralelo:

```http
GET /evaluations?userId={userId}
GET /user-tests?userId={userId}
GET /gyms/{gymId}/exercises?limit=100&offset=0
GET /quantity-types
```

Usar la evaluación como agrupador visual de los tests. Los endpoints de evaluaciones y tests pertenecen al usuario global: si el usuario comparte varios gimnasios, sus datos siguen siendo los mismos.

No llamar `GET /user-tests` sin `userId`; responde `400 Bad Request`.

## 2. Listar tests

```http
GET /user-tests?userId=42
Authorization: Bearer <accessToken>
```

Respuesta `200 OK`:

```json
[
  {
    "id": 81,
    "exerciseId": 25,
    "userId": 42,
    "evaluationId": 31,
    "quantityTypeId": 1,
    "repetitions": 12,
    "quantity": 40.0,
    "date": "2026-10-20T14:30:00Z",
    "description": "Serie controlada, técnica correcta."
  }
]
```

La API no declara un orden de salida. El dashboard debe ordenar localmente por `date` descendente o agrupar por `evaluationId` y luego ordenar por fecha.

## 3. Resolver ejercicios, unidades y evaluación

El test contiene IDs para no duplicar información de catálogo. Crear mapas locales:

```text
exercisesById[exercise.id] = exercise
quantityTypesById[quantityType.id] = quantityType
evaluationsById[evaluation.id] = evaluation
```

Para mostrar el test:

```text
exercise = exercisesById[test.exerciseId]
unit     = quantityTypesById[test.quantityTypeId]
group    = evaluationsById[test.evaluationId]
```

Ejemplo de tarjeta:

```text
Evaluación trimestral
Sentadilla — 12 repeticiones — 40 kg
Serie controlada, técnica correcta.
```

Si `quantity`, `quantityTypeId` o `repetitions` es `null`, ocultar solamente esa parte de la medición. No mostrar `null`, `0` ni una unidad inventada.

Si el catálogo no contiene el ejercicio o la unidad, mostrar “Dato no disponible” y ofrecer recargar catálogos; no bloquear toda la pantalla.

## 4. Crear un test

Primero debe existir una evaluación para el mismo usuario. El formulario debe mostrar un selector construido desde `GET /evaluations?userId={userId}` y exigir una opción válida.

> **Limitación actual:** `POST /evaluations` responde `201 Created` sin devolver el ID creado. Por eso el dashboard no debe crear una evaluación y asociarle automáticamente un test en la misma acción: no hay un identificador confiable para usar sin una nueva lectura y sin riesgo de elegir una evaluación concurrente. Crear la evaluación como una acción separada, volver a cargar el listado y pedir al operador que seleccione la evaluación antes de registrar el test. Para un flujo único se requerirá un endpoint backend atómico que devuelva ambos recursos o al menos el ID de la evaluación creada.

```http
POST /user-tests
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "exerciseId": 25,
  "userId": 42,
  "evaluationId": 31,
  "quantityTypeId": 1,
  "repetitions": 12,
  "quantity": 40.0,
  "date": "2026-10-20T14:30:00Z",
  "description": "Serie controlada, técnica correcta."
}
```

Reglas de formulario:

- `exerciseId`, `userId`, `evaluationId`, `date` y `description` son obligatorios.
- La evaluación debe pertenecer al mismo `userId` del test.
- Informar al menos una métrica: `repetitions` o `quantity`.
- Si se informa `quantity`, también se debe informar `quantityTypeId`.
- Si no hay cantidad, enviar `quantity` y `quantityTypeId` como `null`.
- Repeticiones y cantidad deben ser mayores que cero.
- `date` debe ser ISO-8601 con zona, por ejemplo `2026-10-20T14:30:00Z`.

La respuesta es `201 Created` sin body. Luego recargar `GET /user-tests?userId=42`; no inventar un ID local.

## 5. Editar un test

```http
PUT /user-tests/{testId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body completo, incluyendo el mismo `userId` del test original:

```json
{
  "exerciseId": 25,
  "userId": 42,
  "evaluationId": 31,
  "quantityTypeId": 1,
  "repetitions": 10,
  "quantity": 45.0,
  "date": "2026-10-20T14:30:00Z",
  "description": "Ajustado luego de revisar la carga."
}
```

Respuesta: `204 No Content`. Recargar el listado del mismo usuario tras guardar.

No permitir cambiar el usuario en la interfaz. Si se necesita registrar el resultado para otra persona, crear un nuevo test para esa persona.

## 6. Estados y errores

| Resultado | Acción de interfaz |
| --- | --- |
| `200` con arreglo | Resolver catálogos, ordenar y mostrar. |
| `200` con `[]` | Mostrar “Este usuario aún no tiene tests registrados”. |
| `201` / `204` | Recargar tests del usuario seleccionado. |
| `400` | Revisar campos requeridos, fecha y formato. |
| `401` | Intentar refresh una vez; si falla, cerrar sesión. |
| `403` | Informar falta de permiso o membresía compartida. No hacer refresh. |
| `404` | El test, evaluación, ejercicio o unidad ya no existe; recargar catálogos/listado. |
| `422` | El test no cumple integridad: la evaluación pertenece a otro usuario o las métricas son inválidas. Conservar el formulario y marcar el error. |
| Error de red | Mantener el formulario y ofrecer Reintentar. |

Mientras se crea o actualiza, deshabilitar el botón para evitar duplicados.

## 7. Checklist

- [ ] La lista usa `GET /user-tests?userId={userId}`.
- [ ] Se cargan evaluaciones, ejercicios y unidades para resolver IDs.
- [ ] Cada test se agrupa con una evaluación del mismo usuario.
- [ ] La interfaz exige al menos una métrica válida.
- [ ] Al editar se conserva el `userId` original.
- [ ] Tras `201` o `204`, se recarga la lista desde la API.
- [ ] La interfaz no muestra borrado de tests por ahora.
- [ ] Se manejan `401`, `403`, `404`, `422` y errores de red.
