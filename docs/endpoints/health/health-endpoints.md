# Disponibilidad de la API

`GET /health/ready` es público y no recibe un body. Comprueba que la API puede obtener una conexión válida del pool de PostgreSQL. Verifica conectividad. Para determinar la versión del esquema, consultar las migraciones aplicadas.

**`200 OK`** cuando PostgreSQL está disponible.

```json
{"status":"ready"}
```

**`503 Service Unavailable`** cuando no se puede obtener o validar una conexión.

```json
{"code":"service_unavailable","message":"Service temporarily unavailable"}
```

`GET /` permite comprobar que el proceso HTTP responde aunque PostgreSQL esté temporalmente fuera de servicio.
