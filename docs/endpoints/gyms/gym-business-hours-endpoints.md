# Horarios informativos de gimnasios (`/gym-business-hours`)

Estos horarios se muestran como información del gimnasio. No restringen reservas, accesos ni ninguna otra operación de la API.

- **Autenticación:** todos los endpoints requieren un access token JWT válido.
- `dayOfWeek` usa ISO-8601: `1` lunes y `7` domingo.
- Un gimnasio puede tener más de un intervalo el mismo día (por ejemplo, apertura mañana y tarde), siempre que no repita la misma hora de apertura.

## Estructura

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `id` | number | Solo respuesta | Identificador generado por la base. |
| `gymId` | number | Sí | Gimnasio al que pertenece el intervalo. |
| `dayOfWeek` | number | Sí | Día ISO de 1 a 7. |
| `opensAt` | string | Sí | Hora local en formato `HH:mm` o `HH:mm:ss`. |
| `closesAt` | string | Sí | Hora local posterior a `opensAt`. |

Ejemplo:

```json
{
  "id": 1,
  "gymId": 1,
  "dayOfWeek": 1,
  "opensAt": "07:00",
  "closesAt": "22:00"
}
```

## Endpoints

| Método | Path | Resultado exitoso |
| --- | --- | --- |
| `GET` | `/gym-business-hours` | `200 OK` con todos los intervalos. |
| `GET` | `/gym-business-hours/{id}` | `200 OK` con el intervalo; `404` si no existe. |
| `POST` | `/gym-business-hours` | `201 Created` sin body. |
| `PUT` | `/gym-business-hours/{id}` | `204 No Content`; reemplaza todos los campos. |
| `DELETE` | `/gym-business-hours/{id}` | `204 No Content`. |

Ejemplo para crear o actualizar:

```json
{
  "gymId": 1,
  "dayOfWeek": 6,
  "opensAt": "09:00",
  "closesAt": "14:00"
}
```

La base de datos rechaza días fuera de `1..7`, intervalos con cierre anterior o igual a la apertura, intervalos duplicados y referencias a gimnasios inexistentes.
