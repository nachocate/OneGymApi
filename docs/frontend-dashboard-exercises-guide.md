# Implementación de ejercicios en el dashboard

Esta guía explica cómo implementar la administración de ejercicios. Complementa la [guía general del dashboard](frontend-dashboard-guide.md) y está escrita para el equipo junior.

## 1. Dos tipos de ejercicios

La API maneja dos clases de ejercicios:

| Tipo | `gymId` | Quién lo administra | Dónde se muestra |
| --- | --- | --- | --- |
| Global | `null` | Solo `SUPER_ADMIN` | En todos los gimnasios. |
| Propio de un gimnasio | ID del gimnasio | `COACH`, `ADMIN`, `OWNER` del gym o `SUPER_ADMIN` | Solo en ese gimnasio. |

Un ejercicio no puede cambiar de tipo mediante edición. Un ejercicio propio no se transforma en global, ni uno global se asigna a un gimnasio existente: para eso se crea otro ejercicio.

La combinación siguiente es inválida y devuelve `400 Bad Request`:

```json
{ "gymId": null, "isVisibleGlobal": false }
```

Para un ejercicio global usar siempre `gymId: null` e `isVisibleGlobal: true`. Para uno propio, enviar siempre el ID del gimnasio seleccionado; usar `isVisibleGlobal: false` para mantener una intención clara en el frontend.

## 2. Permisos de la interfaz

| Acción | MEMBER | COACH | ADMIN | OWNER | SUPER_ADMIN |
| --- | --- | --- | --- | --- | --- |
| Ver ejercicios disponibles del gym | Sí | Sí | Sí | Sí | Sí |
| Crear, editar o eliminar ejercicios propios del gym | No | Sí | Sí | Sí | Sí |
| Crear, editar o eliminar ejercicios globales | No | No | No | No | Sí |

La UI debe ocultar acciones que el usuario no puede realizar, pero siempre debe manejar `403 Forbidden`: la validación final está en el backend.

## 3. Cargar el catálogo del gimnasio activo

Usar el `gymId` del selector de gimnasio y llamar:

```http
GET /gyms/{gymId}/exercises?limit=20&offset=0
Authorization: Bearer <accessToken>
```

Este es el endpoint correcto para la pantalla del gimnasio. Devuelve:

- Ejercicios creados por ese gimnasio.
- Ejercicios globales disponibles para todos.

Ejemplo:

```json
[
  {
    "id": 10,
    "name": "Plancha frontal",
    "description": "Mantener el abdomen activo.",
    "videoUrl": null,
    "imageUrl": null,
    "gymId": null,
    "isVisibleGlobal": true
  },
  {
    "id": 25,
    "name": "Circuito de trineo",
    "description": "Ejercicio específico del gimnasio.",
    "videoUrl": null,
    "imageUrl": null,
    "gymId": 1,
    "isVisibleGlobal": false
  }
]
```

En la tabla, mostrar una etiqueta:

- `Global` si `gymId === null`.
- `Propio` si `gymId === activeGymId`.

No usar `GET /exercises` para esta pantalla: es un listado global exclusivo de `SUPER_ADMIN` y no debe usarse como catálogo del gimnasio activo.

### Paginación

La ruta acepta:

```text
?limit=20&offset=0
```

- `limit`: entre `1` y `100`; por defecto `20`.
- `offset`: desde `0`; por defecto `0`.
- Una respuesta vacía indica que no hay más resultados.

Para “Cargar más”, aumentar `offset` por la cantidad de resultados recibidos.

## 4. Formulario de ejercicio propio del gimnasio

Campos del formulario:

| Campo | Tipo | Obligatorio |
| --- | --- | --- |
| `name` | texto | Sí |
| `description` | texto o `null` | No |
| `videoUrl` | URL o `null` | No |
| `imageUrl` | URL o `null` | No |

El formulario no debe permitir editar manualmente `gymId`: se toma del gimnasio activo. Tampoco debe enviar `id` al crear.

### Alta

```http
POST /exercises
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "name": "Remo con mancuerna",
  "description": "Mantener la espalda neutra durante el movimiento.",
  "videoUrl": "https://cdn.example.com/videos/remo.mp4",
  "imageUrl": "https://cdn.example.com/images/remo.png",
  "gymId": 1,
  "isVisibleGlobal": false
}
```

Respuesta exitosa: `201 Created` sin body. Luego recargar el listado del gimnasio. No crear un objeto temporal con un ID inventado.

## 5. Editar un ejercicio propio

Primero comprobar que el ejercicio pertenece al gimnasio activo:

```text
exercise.gymId === activeGymId
```

No habilitar “Editar” para ejercicios globales salvo que el usuario sea `SUPER_ADMIN`.

```http
PUT /exercises/{exerciseId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Enviar el body completo y conservar el mismo `gymId`:

```json
{
  "name": "Remo con mancuerna a una mano",
  "description": "Controlar la rotación del torso.",
  "videoUrl": "https://cdn.example.com/videos/remo.mp4",
  "imageUrl": null,
  "gymId": 1,
  "isVisibleGlobal": false
}
```

La respuesta exitosa es `204 No Content`. Actualizar la lista con una nueva consulta o actualizar el elemento local solamente con el mismo body confirmado por el `204`.

## 6. Eliminar un ejercicio propio

Antes de llamar a la API, pedir confirmación explícita al operador. Explicar que un ejercicio puede formar parte de rutinas existentes; si está referenciado, la base puede rechazar el borrado.

```http
DELETE /exercises/{exerciseId}
Authorization: Bearer <accessToken>
```

La respuesta exitosa es `204 No Content`. Quitar el elemento de la tabla o volver a cargar la página.

No habilitar esta acción para ejercicios globales salvo para `SUPER_ADMIN`.

## 7. Catálogo global: solo plataforma

La sección de ejercicios globales debe aparecer únicamente para `SUPER_ADMIN` usando `systemRoles` de `GET /status`.

Para crear uno:

```http
POST /exercises
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "name": "Plancha frontal",
  "description": null,
  "videoUrl": null,
  "imageUrl": null,
  "gymId": null,
  "isVisibleGlobal": true
}
```

El mismo `SUPER_ADMIN` puede editarlo o eliminarlo mediante `PUT /exercises/{id}` y `DELETE /exercises/{id}`. Al editar, debe continuar con `gymId: null` e `isVisibleGlobal: true`.

## 8. Errores y estados de UI

| Código | Qué significa | Acción de UI |
| --- | --- | --- |
| `400` | Body, ID o combinación de `gymId`/`isVisibleGlobal` inválida. | Mantener el formulario y mostrar el error de validación. |
| `401` | Sesión inválida o vencida. | Intentar refresh una sola vez; si falla, cerrar sesión. |
| `403` | El usuario no puede administrar ese catálogo o gimnasio. | Ocultar acciones y mostrar aviso de permisos. |
| `404` | El ejercicio ya no existe. | Recargar la lista y avisar al usuario. |
| `422` | Una regla de integridad rechazó la operación, por ejemplo un ejercicio usado por otros datos. | No reintentar automáticamente; explicar que primero deben revisarse las dependencias. |
| Error de red | No se conoce el resultado de la operación. | Mantener el estado, informar el fallo y ofrecer reintento seguro. |

## 9. Checklist de implementación

- [ ] El gimnasio activo define el `gymId` de los ejercicios propios.
- [ ] La lista usa `GET /gyms/{gymId}/exercises`, no `GET /exercises`.
- [ ] Global y propio se identifican por `gymId`, no por un texto libre.
- [ ] Un coach solo puede modificar ejercicios de su propio gimnasio.
- [ ] Los formularios no permiten cambiar el dueño de un ejercicio durante una edición.
- [ ] Nunca se envía `gymId: null` junto con `isVisibleGlobal: false`.
- [ ] El borrado pide confirmación y maneja dependencias/restricciones.
- [ ] La UI trata `201` y `204` como éxitos sin intentar leer JSON del response.
