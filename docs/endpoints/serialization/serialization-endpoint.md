# Endpoint de prueba de serialización (`/json/kotlinx-serialization`)

Endpoint público de prueba que devuelve un objeto JSON serializado.

## Consultar la prueba de serialización

```http
GET {BASE_URL}/json/kotlinx-serialization
```

No requiere autenticación ni body.

**Respuesta — `200 OK`**

```json
{ "hello": "world" }
```
