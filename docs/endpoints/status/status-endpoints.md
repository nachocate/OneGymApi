# API de estado de usuario (`/status`)

Documentación para consultar los gimnasios en los que el usuario autenticado tiene una membresía activa y su rol en cada uno.

## Consideraciones generales

- **Base URL:** reemplazar `{BASE_URL}` por la URL del entorno correspondiente.
- Este endpoint requiere `Authorization: Bearer <accessToken>`.
- Las membresías consideradas activas son aquellas cuyo inicio ya ocurrió y cuyo fin es nulo o no ha pasado.

## Consultar mi estado

```http
GET {BASE_URL}/status
Authorization: Bearer <accessToken>
```

**Respuesta exitosa — `200 OK`**

```json
{
  "gymCount": 1,
  "systemRoles": ["SUPER_ADMIN"],
  "gyms": [
    {
      "gym": {
        "id": 1,
        "name": "Forze Gym",
        "description": "Gimnasio de entrenamiento funcional y fuerza.",
        "localityId": 1,
        "address": "Av. Siempre Viva 123",
        "phone": "+54 11 5555-0101",
        "logoUrl": null,
        "bannerUrl": null
      },
      "role": {
        "id": 1,
        "name": "MEMBER"
      }
    }
  ]
}
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `gymCount` | number | Cantidad de gimnasios con membresía activa. |
| `systemRoles` | array de string | Roles globales del usuario. Actualmente puede incluir `SUPER_ADMIN`. |
| `gyms` | array | Membresías activas del usuario. Puede ser `[]`. |
| `gyms[].gym` | object | Datos básicos del gimnasio. |
| `gyms[].role` | object | Rol del usuario en ese gimnasio. |

**Ejemplo sin membresías activas**

```json
{
  "gymCount": 0,
  "systemRoles": [],
  "gyms": []
}
```

**Posibles respuestas**

- `401 Unauthorized`: el token falta, venció o no es válido.
