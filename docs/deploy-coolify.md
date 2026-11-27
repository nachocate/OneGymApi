# Despliegue de OneGymApi en Coolify

## 1. Preparar PostgreSQL

- Si la base está en Coolify, conectarla a la misma red interna que la API. Usar su host interno para formar la URL JDBC.
- Para una base vacía, ejecutar [`onegym_schema.sql`](../src/main/resources/onegym_schema.sql) con `psql -v ON_ERROR_STOP=1 -d <base> -f <archivo>`. El rol que prepara la base debe poder crear `btree_gist`, o un administrador debe instalar la extensión previamente.
- Para una base existente, respaldarla y aplicar solo los SQL pendientes de [`migrations/`](../src/main/resources/migrations) en orden numérico. La API no ejecuta migraciones al iniciar. Reservar el reset y los datos de prueba para bases descartables.

## 2. Configurar la aplicación

1. Crear una aplicación desde el repositorio Git. Elegir **Dockerfile** como estrategia de build. Usar `/` como directorio base y `Dockerfile` en la raíz. El build ejecuta las pruebas y empaqueta la API con Gradle y Java 21.
2. Configurar el puerto interno **8080** y asignar el dominio HTTPS de la API. Ktor escucha en `0.0.0.0:8080` con el perfil del contenedor.
3. Cargar estas variables en **Environment Variables** con alcance **Runtime only**. El perfil del contenedor requiere las cinco primeras.

   | Variable | Valor |
   | --- | --- |
   | `DB_URL` | URL JDBC con host interno, por ejemplo `jdbc:postgresql://postgres-interno:5432/onegym_db`. |
   | `DB_USER` | Usuario de PostgreSQL que usa la API. |
   | `DB_PASSWORD` | Contraseña de ese usuario. |
   | `JWT_SECRET` | Secreto aleatorio de al menos 32 caracteres para firmar access tokens. |
   | `JWT_REFRESH_TOKEN_HASH_SECRET` | Segundo secreto aleatorio e independiente de al menos 32 caracteres para los refresh tokens. |
   | `CORS_ALLOWED_ORIGIN` | Origen del dashboard, por ejemplo `https://dashboard.example.com`, sin ruta ni barra final. Se puede omitir si no hay un frontend web en otro origen. |
   | `DB_POOL_SIZE` | Opcional. El tamaño máximo del pool es 10 por defecto. |

   Si se migra una instancia y se quieren conservar sus sesiones, usar los mismos dos secretos JWT, `JWT_ISSUER` y `JWT_AUDIENCE` efectivos de la instancia anterior. Mantener también la base con sus refresh tokens.

4. En **Persistent Storage**, montar un volumen en `/app/uploads`. El proceso corre con UID y GID `10001`. Si se usa un directorio del host, darle permisos de lectura y escritura para ese usuario. Si se migra una instancia, copiar su directorio `uploads/` al volumen para conservar las imágenes que la base referencia bajo `/media`.
5. El `Dockerfile` trae un chequeo HTTP de `GET /health/ready`. Para usar un chequeo gestionado desde Coolify, configurarlo con `GET`, `http`, host `127.0.0.1`, puerto `8080`, path `/health/ready` y timeout de 20 segundos. La configuración del panel reemplaza el chequeo de la imagen si está habilitada.

## 3. Verificar el despliegue

Tras desplegar, comprobar que `https://<dominio-api>/health/ready` devuelve `200` y `{"status":"ready"}`. Probar un login y una consulta autenticada con una cuenta del entorno. Si se migraron imágenes, comprobar que sigue disponible una URL existente bajo `/media`. El chequeo de disponibilidad cubre la conexión a la base. El login verifica además una ruta que usa el esquema de la aplicación.

Referencias de Coolify [Dockerfile](https://coolify.io/docs/applications/builds/dockerfile), [variables de entorno](https://coolify.io/docs/applications/configuration/environment-variables), [almacenamiento persistente](https://coolify.io/docs/applications/configuration/persistent-storage) y [health checks](https://coolify.io/docs/applications/configuration/health-checks).
