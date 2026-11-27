# Guía de actualización de planificaciones — Dashboard

Esta guía indica cómo construir la pantalla de edición de rutinas sin perder cambios ni crear semanas, días, bloques o ejercicios de forma parcial. Está dirigida al dashboard administrativo; para el modelo completo consultar la [guía del constructor](frontend-dashboard-planification-builder-guide.md).

## Antes de comenzar

- Usar como base local `http://localhost:8080`.
- Enviar `Authorization: Bearer <accessToken>` en todas las requests.
- Para `POST` y `PUT` JSON, enviar `Content-Type: application/json`.
- El origen local permitido por la API es `http://localhost:5173`.
- Para crear o editar se requiere `COACH`, `ADMIN`, `OWNER` o `SUPER_ADMIN` en el gimnasio activo.

## 1. Mostrar el listado

```http
GET /gyms/{gymId}/plans?limit=20&offset=0
```

Esta ruta no trae el árbol completo. Es suficiente para pintar una tabla o tarjetas:

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

- Mostrar `name` y `weekCount`.
- Usar `id` para navegar al editor.
- No intentar reconstruir el árbol desde este listado.

## 2. Abrir el editor

Cuando el usuario selecciona un plan, cargar en paralelo:

```http
GET /gyms/{gymId}/plans/{planId}/structure
GET /gyms/{gymId}/exercises?limit=100&offset=0
GET /plan-types
GET /quantity-types
```

La primera respuesta incluye todo el árbol y su `revision`:

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

Copiar esta respuesta a un borrador local. No modificar directamente el objeto cacheado por el cliente HTTP.

## 3. Reglas importantes sobre IDs

- Conservar los IDs de semana, día, bloque y ejercicio que devolvió la API.
- Para un nodo nuevo, no enviar `id`.
- Para eliminar un nodo, quitarlo del árbol antes de guardar.
- No enviar IDs inventados ni IDs de otra planificación.

El `PUT` recibe el árbol completo. Omitir un nodo existente significa pedir su eliminación.

## 4. Guardar una edición

Usar una sola request:

```http
PUT /gyms/{gymId}/plans/{planId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

El body debe incluir todos los nodos visibles del borrador y la `revision` obtenida al abrirlo o en el último guardado exitoso:

```json
{
  "name": "Fuerza inicial - 4 semanas",
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
                  "repetitions": 10,
                  "quantity": 25.0
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

Un `200 OK` devuelve el árbol persistido con una `revision` nueva. Reemplazar el borrador local completo por esa respuesta. No actualizar solo la `revision`: pueden haber cambiado IDs de nodos nuevos.

## 5. Crear una planificación

Usar el mismo árbol, pero sin IDs de nodos y sin `revision`:

```http
POST /gyms/{gymId}/plans
```

La respuesta `201 Created` devuelve todos los IDs. Reemplazar inmediatamente el borrador por la respuesta antes de permitir otra edición.

## 6. Validaciones antes de habilitar Guardar

- Nombre y tipo de plan obligatorios.
- Al menos una semana, un día, un bloque y un ejercicio.
- `number` de semanas y días: positivo y único dentro de su padre.
- `position` de bloques y ejercicios: positivo y único dentro de su padre.
- `laps` mayor que cero.
- Cada ejercicio debe pertenecer al catálogo de `GET /gyms/{gymId}/exercises`.
- `quantityTypeId`, si existe, debe existir en `GET /quantity-types`.
- Repeticiones y cantidad no pueden ser negativas.

La API vuelve a validar estas reglas. Las validaciones de interfaz solo evitan una experiencia frustrante.

## 7. Manejo de respuestas y errores

| Respuesta | Qué debe hacer la interfaz |
| --- | --- |
| `200` | Reemplazar el borrador por la respuesta y mostrar confirmación. |
| `201` | Usar el árbol con IDs generado por la API. |
| `400` | Mostrar el error de datos o estructura; conservar el borrador. |
| `401` | Ejecutar una vez el flujo de refresh; si falla, cerrar sesión. |
| `403` | Informar falta de permisos y volver al listado. No intentar refresh. |
| `404` | El gym, plan o dependencia no existe; recargar el listado o catálogos. |
| `409` | Conflicto de edición: no reintentar automáticamente. Recargar estructura y pedir al usuario que vuelva a aplicar sus cambios. |
| `422` | Una eliminación o cambio viola una relación existente; conservar el borrador y mostrar el mensaje recibido. |
| Error de red | Mantener el borrador local y ofrecer Reintentar. |

## 8. Prevención de doble guardado

Mientras la request está en curso:

1. Deshabilitar el botón Guardar.
2. Mostrar un estado de guardado.
3. No enviar un segundo `PUT` por doble click o por atajos de teclado.
4. Rehabilitar al terminar, tanto en éxito como en error.

## 9. Publicación y borradores

Actualmente la API no guarda un estado `DRAFT` ni `PUBLISHED`. El dashboard puede conservar un borrador no guardado en memoria o almacenamiento local, pero no debe presentarlo como publicado ni intentar llamar a un endpoint de publicación.

## Checklist de entrega

- [ ] El listado usa `GET /gyms/{gymId}/plans`.
- [ ] El editor se abre con `GET .../structure`.
- [ ] El `PUT` conserva IDs existentes y envía `revision`.
- [ ] El dashboard reemplaza el borrador con cada respuesta exitosa.
- [ ] Un `409` nunca sobrescribe cambios automáticamente.
- [ ] Los errores y fallos de red conservan el trabajo local.
