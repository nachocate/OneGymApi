# Especificación de planificaciones para el dashboard

Este documento define cómo debe funcionar el constructor visual de planificaciones (rutinas) del dashboard. Está dirigido al equipo de frontend y complementa el contrato técnico de [planificaciones atómicas](endpoints/planifications/planifications-endpoints.md).

> **Estado actual:** el backend ya expone rutas atómicas para crear, leer, actualizar, clonar y asignar una planificación. El dashboard debe usar esas rutas de alto nivel en lugar de encadenar los CRUDs individuales de semanas, días, bloques y ejercicios.

## 1. Qué es una planificación

Una planificación es una rutina completa y reutilizable de un gimnasio:

```text
Planificación
 ├─ Datos generales: nombre, tipo, variante/origen
 └─ Semanas ordenadas
     └─ Días ordenados
         └─ Bloques ordenados
             └─ Ejercicios ordenados con series/repeticiones/cantidad
```

Puede asignarse a uno o más miembros del mismo gimnasio. La planificación pertenece siempre a un solo gimnasio.

## 2. Permisos

| Acción | MEMBER | COACH | ADMIN | OWNER | SUPER_ADMIN |
| --- | --- | --- | --- | --- | --- |
| Ver planificación asignada propia | Sí | Sí | Sí | Sí | Sí |
| Consultar catálogo de planificaciones del gym | Sí | Sí | Sí | Sí | Sí |
| Crear, editar, clonar o eliminar una planificación | No | Sí | Sí | Sí | Sí |
| Asignar una planificación a miembros | No | Sí | Sí | Sí | Sí |

La interfaz usa el rol de `GET /status` para mostrar acciones, pero debe manejar `403` si el backend rechaza la operación.

## 3. Experiencia esperada del dashboard

La pantalla debe tener tres áreas:

1. **Listado de planificaciones:** nombre, tipo, cantidad de semanas y acciones.
2. **Editor de planificación:** árbol de semanas, días, bloques y ejercicios.
3. **Asignación:** selección de miembros activos del gimnasio y del plan guardado.

Flujos principales:

```text
Crear planificación → armar árbol → validar → guardar
Editar planificación → cargar árbol completo → modificar → guardar
Clonar planificación → copiar árbol sin IDs → cambiar nombre → guardar
Asignar planificación guardada → elegir miembro activo → confirmar
```

## 4. Estado de edición en el frontend

Mientras el usuario edita, conservar todo el árbol en estado local. No hacer una request por cada click de “agregar día” o “agregar ejercicio”.

Modelo conceptual del borrador:

```ts
type PlanificationDraft = {
  id?: number
  revision?: number
  name: string
  planTypeId: number
  planRootId: number | null
  weeks: WeekDraft[]
}

type WeekDraft = {
  id?: number
  number: number
  days: DayDraft[]
}

type DayDraft = {
  id?: number
  number: number
  blocks: BlockDraft[]
}

type BlockDraft = {
  id?: number
  position: number
  name: string | null
  laps: number
  exercises: BlockExerciseDraft[]
}

type BlockExerciseDraft = {
  id?: number
  exerciseId: number
  quantityTypeId: number | null
  position: number
  repetitions: number | null
  quantity: number | null
}
```

Los IDs opcionales solo existen cuando el árbol ya fue persistido. Al clonar una planificación, quitar todos los IDs para que el backend cree recursos nuevos.

## 5. Validaciones de interfaz antes de guardar

La API debe validar las reglas igualmente, pero el dashboard debe prevenir errores evidentes:

- Nombre de planificación obligatorio y con texto visible.
- Tipo de planificación obligatorio.
- Al menos una semana, un día, un bloque y un ejercicio antes de permitir guardar.
- Números de semana y día positivos y sin duplicados dentro de su padre.
- Posiciones de bloques y ejercicios positivas y sin duplicados dentro de su padre.
- `laps` mayor que cero.
- Cada `exerciseId` debe provenir de `GET /gyms/{gymId}/exercises`.
- `quantityTypeId`, si existe, debe provenir de `GET /quantity-types`.
- Repeticiones y cantidad no negativas cuando se informan.
- Un bloque debe tener al menos un ejercicio antes de guardar.

Mostrar los errores junto al campo correspondiente y conservar el borrador; nunca descartar la planificación por un error HTTP.

## 6. Datos auxiliares para el editor

Al abrir el editor, cargar en paralelo:

```http
GET /gyms/{gymId}/exercises?limit=100&offset=0
GET /plan-types
GET /quantity-types
GET /gyms/{gymId}/members?limit=100&offset=0
```

Usar el catálogo de ejercicios del gym, que incluye ejercicios propios y globales. No usar `GET /exercises`, porque es exclusivo de `SUPER_ADMIN` y no representa necesariamente los ejercicios disponibles del gimnasio activo.

## 7. Contrato de API disponible

Las rutas anidadas bajo el gimnasio no reemplazan los CRUDs existentes; son el contrato de alto nivel para el editor.

| Método | Path | Propósito |
| --- | --- | --- |
| `GET` | `/gyms/{gymId}/plans` | Listado resumido con `weekCount` y `revision`, sin cargar el árbol completo. |
| `GET` | `/gyms/{gymId}/plans/{planId}/structure` | Árbol completo para abrir el editor. |
| `POST` | `/gyms/{gymId}/plans` | Crear un plan completo de forma atómica y devolver el árbol persistido. |
| `PUT` | `/gyms/{gymId}/plans/{planId}` | Reemplazar de forma atómica el árbol completo y devolverlo. |
| `POST` | `/gyms/{gymId}/plans/{planId}/clone` | Clonar una planificación y devolver la nueva. |
| `POST` | `/gyms/{gymId}/plans/{planId}/assignments` | Asignar un plan a un miembro activo. |
| `GET` | `/gyms/{gymId}/members/{userId}/plans` | Consultar asignaciones de un miembro dentro del gym. |

Todas requieren JWT. Las rutas de escritura requieren `COACH` o superior; leer estructura requiere membresía activa. El listado de asignaciones se limita a roles de gestión.

El listado responde elementos como:

```json
{
  "id": 100,
  "name": "Fuerza inicial - 4 semanas",
  "gymId": 1,
  "planTypeId": 1,
  "planRootId": null,
  "revision": 3,
  "weekCount": 4
}
```

## 8. Body para crear o actualizar planificación

El dashboard debe enviar una única request al guardar:

```json
{
  "name": "Fuerza inicial - 4 semanas",
  "planTypeId": 1,
  "planRootId": null,
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

El `gymId` no se incluye en el body: sale del path y evita que el cliente intente crear datos en otro gimnasio.

Para `PUT`, incluir los IDs de las entidades persistidas que continúan existiendo y la `revision` recibida al abrir el árbol o al guardar por última vez. Omitir IDs en nodos nuevos. El backend decide qué hijos fueron eliminados y preserva la operación dentro de una transacción.

Ejemplo mínimo de la diferencia en una actualización:

```json
{
  "name": "Fuerza inicial - 4 semanas",
  "planTypeId": 1,
  "planRootId": null,
  "revision": 3,
  "weeks": ["...árbol completo..."]
}
```

## 9. Respuesta de la API

`POST`, `PUT`, `GET structure` y `clone` responden `200` o `201` con el árbol completo, incluyendo todos los IDs generados:

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

No usar IDs temporales como identificadores persistentes. Reemplazar el borrador local por la respuesta del backend después de guardar, incluido el valor de `revision`.

## 10. Asignar a un miembro

```http
POST /gyms/{gymId}/plans/{planId}/assignments
Content-Type: application/json
Authorization: Bearer <accessToken>
```

```json
{ "userId": 42 }
```

El backend confirma que el miembro tenga una membresía activa en el gym. Respuesta:

```json
{ "id": 900, "userId": 42, "planId": 100 }
```

Para consultar las asignaciones del miembro usar `GET /gyms/{gymId}/members/{userId}/plans`.

## 11. Estados de UI y manejo de errores

| Situación | Comportamiento esperado |
| --- | --- |
| Cargando estructura | Mostrar skeleton/spinner, sin habilitar guardar. |
| Borrador modificado | Mostrar indicador de cambios sin guardar. |
| Guardando | Deshabilitar botón para evitar doble envío. |
| Éxito | Reemplazar el borrador por la respuesta persistida y mostrar confirmación. |
| `400`/`422` | Mostrar errores de campo o estructura; conservar el borrador. |
| `403` | Mostrar falta de permisos y volver al listado. |
| `404` | El plan o sus dependencias ya no existen; recargar listado. |
| `409` | Otro usuario guardó antes. Recargar la estructura; no reintentar automáticamente ni sobrescribir sus cambios. |
| Error de red | Mantener los cambios locales, mostrar reintento y no afirmar que se guardó. |

## Estado de publicación

Actualmente un plan no tiene campo ni endpoint de estado `DRAFT`/`PUBLISHED`. El dashboard puede mantener cambios sin guardar como borrador **local** en memoria o almacenamiento del navegador, pero no debe mostrar ese estado como persistido ni intentar publicar/despublicar contra la API.

Cuando producto requiera esta funcionalidad, backend deberá agregar un estado persistente al plan, una migración y endpoints/operaciones explícitas para cambiarlo. Hasta entonces, una planificación guardada se considera disponible según las reglas actuales de permisos.

## 12. Checklist antes de implementar

- [ ] El dashboard usa las rutas atómicas y recibe el árbol completo con sus IDs.
- [ ] La creación y actualización de una planificación son atómicas: sin árboles parcialmente guardados.
- [ ] El dashboard mantiene el borrador local hasta que el backend confirma la operación.
- [ ] Los selects usan catálogos del gimnasio activo y no listados globales no autorizados.
- [ ] La UI bloquea el guardado con estructura incompleta o posiciones duplicadas.
- [ ] La clonación elimina IDs antes de crear el nuevo árbol.
- [ ] La asignación verifica membresía activa del usuario.
