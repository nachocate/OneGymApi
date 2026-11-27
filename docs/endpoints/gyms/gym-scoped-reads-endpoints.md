# Lecturas de recursos por gimnasio

Estas rutas son la fuente de datos del dashboard para un gimnasio concreto. Requieren JWT y una membresía activa en el gimnasio; el listado de miembros requiere rol `COACH` o superior. `SUPER_ADMIN` puede leer cualquier gimnasio.

Todas aceptan paginación opcional:

```text
?limit=20&offset=0
```

`limit` tiene valor por defecto `20`, máximo `100`; `offset` tiene valor por defecto `0`. Un valor inválido devuelve `400 Bad Request`.

| Método | Path | Rol mínimo | Respuesta |
| --- | --- | --- | --- |
| `GET` | `/gyms/{gymId}/members` | COACH | Miembros con usuario público, rol y vigencia de la membresía. |
| `GET` | `/gyms/{gymId}/coaches` | MEMBER | Asignaciones `GymCoach` del gimnasio. |
| `GET` | `/gyms/{gymId}/business-hours` | MEMBER | Horarios informativos `GymBusinessHour`. |
| `GET` | `/gyms/{gymId}/plans` | MEMBER | Planes del gimnasio. |
| `GET` | `/gyms/{gymId}/exercises` | MEMBER | Ejercicios propios del gym más ejercicios globales. |

Ejemplos:

```http
GET /gyms/1/members?limit=20&offset=0
GET /gyms/1/plans?limit=20&offset=40
GET /gyms/1/exercises?limit=50&offset=0
```

Las respuestas son arreglos; una respuesta vacía indica que no existen más resultados para ese offset. Los horarios son informativos y no restringen operaciones de la API.

Ejemplo de miembro:

```json
{
  "id": 12,
  "user": { "id": 42, "firstName": "Sofía", "lastName": "Martínez", "email": "sofia@example.test", "avatarUrl": null, "phone": null, "address": null },
  "role": { "id": 1, "name": "MEMBER" },
  "startDate": "2026-10-21",
  "endDate": null
}
```
