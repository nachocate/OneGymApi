# API de inicio de gimnasio (`/{gymId}/home`)

Documentación para obtener la información de inicio de un gimnasio y los entrenadores actualmente asignados.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Requiere `Authorization: Bearer <accessToken>`.
- El usuario autenticado debe tener una membresía activa en el gimnasio solicitado.
- Solo se incluyen entrenadores cuya asignación está vigente en la fecha de consulta.

## Consultar el inicio de un gimnasio

```http
GET {BASE_URL}/1/home
Authorization: Bearer <accessToken>
```

**Respuesta exitosa — `200 OK`**

```json
{
  "gym": {
    "id": 1,
    "name": "Forze Gym",
    "description": "Gimnasio de entrenamiento funcional y fuerza.",
    "schedule": "Lunes a viernes 07:00-22:00",
    "address": "Av. Siempre Viva 123",
    "phone": "+54 11 5555-0101",
    "logoUrl": "https://cdn.ejemplo.com/gyms/forze/logo.png",
    "bannerUrl": "https://cdn.ejemplo.com/gyms/forze/banner.png"
  },
  "coaches": [
    {
      "user": {
        "id": 2,
        "firstName": "Lucas",
        "lastName": "Fernandez",
        "email": "lucas@onegym.test",
        "avatarUrl": null,
        "phone": "+54 11 5555-0102",
        "address": "Av. Siempre Viva 124"
      },
      "coachType": {
        "id": 1,
        "name": "Entrenador funcional"
      },
      "startDate": "2026-01-01",
      "endDate": null
    }
  ]
}
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `gym` | object | Datos completos del gimnasio. |
| `coaches` | array | Entrenadores activos del gimnasio. Puede ser `[]`. |
| `coaches[].user` | object | Datos públicos del entrenador. |
| `coaches[].coachType` | object | Especialidad asignada al entrenador. |
| `coaches[].startDate` | string | Inicio de la asignación, `YYYY-MM-DD`. |
| `coaches[].endDate` | string \| null | Fin de la asignación, o `null` si sigue vigente. |

**Posibles respuestas**

- `400 Bad Request`: `gymId` no es un número válido.
- `401 Unauthorized`: el token falta, venció o no es válido.
- `403 Forbidden`: el usuario no tiene una membresía activa en el gimnasio.
- `404 Not Found`: el gimnasio no existe.
