# Visualización de evaluaciones — Dashboard

Esta guía describe cómo consultar y mostrar las evaluaciones de un usuario. Una evaluación es un registro descriptivo, por ejemplo una evaluación inicial o un control periódico. Los valores de fuerza, peso o repeticiones pertenecen a **tests de usuario**, que se consultan por otra ruta.

## Permisos y alcance

Todas las rutas requieren:

```http
Authorization: Bearer <accessToken>
```

- El propio usuario puede leer sus evaluaciones.
- Un usuario puede leerlas si comparte al menos un gimnasio activo con el titular de la evaluación.
- `SUPER_ADMIN` puede leer cualquier evaluación.
- Crear, editar y eliminar requieren `COACH`, `ADMIN`, `OWNER` o `SUPER_ADMIN` con un gimnasio activo en común.

Si una persona deja el gimnasio y ya no comparte otro gimnasio activo con el usuario, el backend responde `403`.

## 1. Flujo recomendado de pantalla

```text
Seleccionar gimnasio activo
      ↓
Seleccionar miembro del gimnasio
      ↓
GET /evaluations?userId={userId}
      ↓
Lista de evaluaciones del miembro
      ↓
Seleccionar una evaluación para ver el detalle
```

Para elegir el usuario desde un dashboard de gestión:

```http
GET /gyms/{gymId}/members?limit=100&offset=0
```

No llamar `GET /evaluations` sin `userId`: responde `400 Bad Request`. No existe un endpoint para listar todas las evaluaciones de todos los usuarios.

## 2. Listar evaluaciones de un usuario

```http
GET /evaluations?userId=42
Authorization: Bearer <accessToken>
```

Respuesta `200 OK`:

```json
[
  {
    "id": 31,
    "userId": 42,
    "name": "Evaluación trimestral",
    "description": "Mejoró movilidad de cadera y técnica de sentadilla.",
    "creationDate": "2026-10-20T14:30:00Z"
  },
  {
    "id": 12,
    "userId": 42,
    "name": "Evaluación inicial",
    "description": null,
    "creationDate": "2026-07-01T15:00:00Z"
  }
]
```

Un arreglo vacío es válido: significa que el usuario todavía no tiene evaluaciones.

La API no garantiza un orden explícito. El dashboard debe ordenar una copia local por `creationDate` descendente antes de mostrar la línea de tiempo.

## 3. Ver detalle

Si la lista no conserva el objeto completo o existe navegación directa por ID:

```http
GET /evaluations/{evaluationId}
Authorization: Bearer <accessToken>
```

La respuesta es el objeto `Evaluation` completo. Antes de presentarlo, confirmar que `response.userId` coincide con el usuario seleccionado; si no coincide, volver a la lista en lugar de mezclar información de personas distintas en la pantalla.

## 4. Modelo de estado sugerido

```ts
type Evaluation = {
  id: number
  userId: number
  name: string
  description: string | null
  creationDate: string // ISO-8601 con zona
}

type EvaluationsScreenState =
  | { kind: "idle" }
  | { kind: "loading"; userId: number }
  | { kind: "content"; userId: number; items: Evaluation[] }
  | { kind: "empty"; userId: number }
  | { kind: "error"; userId: number; status?: number }
```

Al cambiar de miembro, cancelar o ignorar la respuesta pendiente anterior para no mostrar las evaluaciones de A mientras la pantalla ya indica que se seleccionó B.

## 5. Renderizado

| Campo | Uso en la interfaz |
| --- | --- |
| `name` | Título de tarjeta o ítem de línea de tiempo. |
| `creationDate` | Fecha/hora localizada del registro. Parsear ISO-8601; no cortar el string manualmente. |
| `description` | Cuerpo de texto. Si es `null`, mostrar “Sin observaciones”. |
| `id` | Clave estable de lista y navegación de detalle. |

No exponer ni pedir contraseña, tokens, datos internos o el modelo de usuario completo como parte de esta pantalla.

## 6. Evaluaciones versus tests

| Necesidad | Recurso |
| --- | --- |
| Observaciones, diagnóstico o progreso descriptivo | `GET /evaluations?userId={userId}` |
| Medición concreta de un ejercicio: peso, repeticiones, cantidad | `GET /user-tests?userId={userId}` |

Si la UX muestra ambas cosas en un perfil, cargarlas en paralelo y mantenerlas en secciones distintas. No interpretar `description` de una evaluación como una medición numérica estructurada.

## 7. Manejo de errores

| Resultado | Comportamiento de UI |
| --- | --- |
| `200` con arreglo | Ordenar y mostrar la lista. |
| `200` con `[]` | Estado vacío: “Este usuario aún no tiene evaluaciones”. |
| `400` | Revisar que `userId` sea numérico y esté presente. |
| `401` | Intentar refresh una sola vez; si falla, limpiar sesión e ir a login. |
| `403` | Informar que no existe una membresía compartida o permiso vigente. No hacer refresh. |
| `404` en detalle | La evaluación ya no existe; volver a la lista y recargarla. |
| Error de red | Mostrar Reintentar y conservar el miembro seleccionado. |

## 8. Consideraciones futuras

La ruta de listado no tiene `limit` ni `offset`. No simular paginación en el cliente escondiendo datos: si el historial crece y la carga deja de ser razonable, solicitar al backend un contrato paginado y documentado.

## Checklist

- [ ] El listado siempre incluye `userId` como query parameter.
- [ ] La UI conserva el miembro seleccionado durante carga y error.
- [ ] Las fechas se ordenan por `creationDate` de forma descendente.
- [ ] `description: null` se muestra de manera segura.
- [ ] El detalle valida que pertenezca al usuario seleccionado.
- [ ] Los tests numéricos se consultan en `/user-tests`, no se mezclan con evaluaciones.
- [ ] La interfaz maneja `401`, `403`, `404` y error de red.
