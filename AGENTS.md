# Guía de contribución para agentes — API Kotlin/Ktor

## Objetivo y principios

Este documento es una plantilla para un backend Kotlin/JVM con Ktor, PostgreSQL y Exposed. Al adoptarlo en otro repositorio, sustituir los marcadores `<proyecto>`, `<paquete-raiz>` y las rutas de ejemplo por las convenciones reales del proyecto. Mantener los cambios pequeños, coherentes con la arquitectura existente y listos para que otro integrante pueda ejecutarlos localmente. Priorizar claridad, tipos seguros, manejo explícito de errores y compatibilidad hacia atrás cuando sea posible.

No modificar versiones de Gradle, Kotlin, Ktor ni dependencias ya presentes sin autorización explícita del usuario. Antes de agregar una dependencia, comprobar que sea compatible con las versiones declaradas en `build.gradle.kts` y `gradle/libs.versions.toml`; si exige actualizar una dependencia existente, detenerse y solicitar aprobación.

## Arquitectura actual

El código fuente debería estar bajo `src/main/kotlin/<paquete-raiz>` y usar estas capas:

```text
Ktor route (`routes/`) 
  -> interfaz de repositorio (`respository/`; conservar el nombre actual)
  -> implementación (`data/*RepositoryImp.kt`) y transacciones Exposed
  -> DAO (`dao/`) y definición Exposed de tablas (`data/<Proyecto>Tables.kt` o equivalente)
  -> PostgreSQL

Modelo de API (`model/`, @Serializable) <-> mapper (`mappers/DaoMappers.kt`) <-> DAO
```

- `Application.kt` instala serialización, Koin, CORS y autenticación; `Routing.kt` registra rutas públicas y protegidas. Ajustar sus nombres si el proyecto usa otra organización.
- `modules/AppDiModules.kt` es el único registro de dependencias de aplicación. Todo repositorio o servicio nuevo debe quedar registrado allí como interfaz → implementación.
- `database/Database.kt` configura un pool Hikari y la conexión Exposed; las consultas deben ejecutarse dentro de `transaction(Database.connection)`.
- Las tablas Exposed viven juntas en `data/<Proyecto>Tables.kt` o su equivalente; los DAOs extienden `LongEntity` y los IDs persistidos son `Long` generados por la base.
- Los modelos de transporte usan `kotlinx.serialization`. No exponer secretos: en particular, las respuestas de usuario deben usar `UserResponse`, nunca el modelo que contiene el hash de contraseña.
- Las rutas actuales mantienen respuestas consistentes: `POST` de recursos devuelve `201 Created` sin body; `PUT` y `DELETE`, `204 No Content`; ID inválido, `400`; recurso inexistente, `404`; borrado idempotente cuando aplique.

## Cambios en endpoints o modelos: lista obligatoria

Ante la creación, modificación o eliminación de un endpoint, entidad, campo o relación, revisar y actualizar en el mismo cambio todo lo afectado:

1. Modelo(s) de API en `model/` y, si corresponde, DTOs de request/response separados.
2. Tabla Exposed en el archivo central de tablas (por ejemplo, `data/<Proyecto>Tables.kt`), DAO en `dao/`, mapper(s) en `mappers/DaoMappers.kt`, interfaz de repositorio en `respository/`, implementación en `data/` y el binding de Koin en `modules/AppDiModules.kt`; adaptar estos nombres si el proyecto adopta otra estructura.
3. Ruta en `routes/` y su registro en `Routing.kt`, definiendo claramente si es pública o queda dentro de `authenticate("auth-jwt")`.
4. Base de datos: esquema fresco, migración, datos de prueba y reset, según las reglas de la siguiente sección.
5. Tests existentes afectados y pruebas nuevas proporcionales al cambio (éxito, validación, autorización y casos no encontrados cuando correspondan).
6. Documentación: crear o actualizar `docs/endpoints/<dominio>/<dominio>-endpoints.md` y actualizar el índice/resumen pertinente en `docs/api-endpoints.md`. Documentar método, path, autenticación, body, respuestas, códigos de error y ejemplos JSON reales.

No crear un endpoint que lea o escriba una tabla sin que los modelos y scripts de base de datos representen el mismo contrato.

## Contrato de PostgreSQL y scripts

PostgreSQL es la única base de datos objetivo. Exposed debe reflejar el esquema SQL, no reemplazarlo como fuente de verdad.

- Definir los scripts principales usando el nombre real del proyecto: `src/main/resources/<proyecto>_schema.sql`, `src/main/resources/seed_test_data.sql` y `src/main/resources/reset_<proyecto>_schema.sql`. Por ejemplo, para `ecommerce`: `ecommerce_schema.sql` y `reset_ecommerce_schema.sql`.
- Para cada cambio de esquema, actualizar `src/main/resources/<proyecto>_schema.sql` para instalaciones limpias.
- Para una base existente, agregar una migración SQL incremental, ordenada y versionada en `src/main/resources/migrations/` (`V<n>__descripcion_en_snake_case.sql`). Nunca editar migraciones ya aplicadas.
- Actualizar `src/main/resources/seed_test_data.sql` para que una instalación limpia tenga datos de prueba válidos, relaciones consistentes y contraseñas hasheadas con BCrypt. No incluir datos reales ni secretos.
- Actualizar `src/main/resources/reset_<proyecto>_schema.sql` con toda tabla nueva y respetar dependencias/FKs. Debe poder borrar únicamente las tablas del proyecto y permitir luego ejecutar `<proyecto>_schema.sql` y el seed.
- Mantener alineados restricciones, nullability, defaults, índices, tipos, nombres de columnas y reglas `ON DELETE` entre SQL, definiciones de tablas Exposed, DAOs y modelos.

Antes de finalizar un cambio de persistencia, validar al menos la secuencia: reset → schema → seed, y ejecutar las pruebas que no requieran infraestructura externa.

## Diseño y revisión de PostgreSQL

Tratar el schema PostgreSQL como un contrato crítico de la aplicación. Escribir SQL claro, portable dentro de PostgreSQL, con nombres consistentes y sin asumir que la validación de la API reemplaza la integridad de datos.

### Convenciones de modelado

- Usar `snake_case` para tablas, columnas, restricciones e índices. Nombrar tablas en plural cuando representen colecciones de entidades y usar nombres descriptivos para tablas de relación (`user_roles`, `order_items`).
- Toda tabla persistente debe tener una clave primaria numérica autogenerada: `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`. No aceptar IDs creados por el cliente ni depender de `MAX(id) + 1`.
- Declarar explícitamente `NOT NULL` cuando un dato sea obligatorio. Definir `DEFAULT` solo si representa una regla válida de negocio; no usar defaults para ocultar datos faltantes.
- Elegir tipos según el dominio: `BOOLEAN` para flags, `DATE` para fechas sin hora, `TIMESTAMPTZ` para instantes, `NUMERIC(precision, scale)` para dinero o medidas exactas, y `TEXT` o `VARCHAR(n)` cuando el límite de longitud sea una regla real. Evitar `DOUBLE PRECISION` para importes monetarios.
- Usar claves foráneas para cada relación persistida y elegir deliberadamente `ON DELETE RESTRICT`, `CASCADE`, `SET NULL` o una baja lógica según la regla de negocio. Documentar y revisar especialmente toda cascada, porque puede borrar datos relacionados de forma irreversible.
- Modelar relaciones muchos-a-muchos con una tabla intermedia, FKs y una restricción `UNIQUE` compuesta que impida duplicados cuando corresponda.
- Aplicar restricciones de base para reglas que deben cumplirse siempre: `UNIQUE`, `CHECK`, FKs y `NOT NULL`. Ejemplos: importes no negativos, fechas de fin posteriores a inicio y valores dentro de un rango permitido.
- Preferir tablas de catálogo o `CHECK` para valores controlados. Usar tipos `ENUM` de PostgreSQL solo tras evaluar su coste de evolución mediante migraciones.
- Agregar `created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP` y, cuando el dominio lo requiera, `updated_at`; definir claramente cómo se actualiza este último (aplicación o trigger) y mantenerlo uniforme.

### Rendimiento, integridad y seguridad

- Crear índices para PKs, FKs consultadas con frecuencia, columnas usadas en `JOIN`, filtros, ordenamientos y restricciones únicas. No añadir índices especulativos: cada índice tiene coste de escritura y mantenimiento.
- Revisar índices compuestos según el orden real de filtros y ordenamientos de las consultas. Para cambios relevantes, validar el plan con `EXPLAIN (ANALYZE, BUFFERS)` sobre datos representativos antes de afirmar una mejora.
- No almacenar contraseñas, refresh tokens ni secretos en claro. Persistir hashes seguros y minimizar datos personales; no guardar datos que la funcionalidad no necesita.
- Mantener las consultas parametrizadas a través de Exposed/JDBC. No construir SQL concatenando entrada de usuarios.
- Usar transacciones para preservar invariantes que involucren varias consultas o tablas. Para operaciones sensibles a concurrencia, complementar con constraints, locks o actualizaciones condicionales según el caso.

### Revisión de consistencia y cambios de schema

- Al analizar o modificar la base, buscar activamente: tablas/columnas sin PK o FK, FKs con tipos incompatibles, nullability distinta entre SQL y Exposed, relaciones sin índices útiles, duplicados posibles sin `UNIQUE`, defaults incorrectos, cascadas peligrosas, restricciones ausentes, nombres inconsistentes, datos huérfanos, campos de fecha incoherentes y diferencias entre schema, migraciones, seed y reset.
- Cuando se detecte una mejora estructural que no fue solicitada expresamente, no aplicarla de forma silenciosa. Informar el hallazgo, su riesgo/beneficio, tablas y datos afectados, alternativa de migración y compatibilidad con la API; esperar aprobación antes de alterar el schema.
- Una vez aprobado un cambio de tablas, columnas, tipos, restricciones, índices o relaciones, revisar obligatoriamente su impacto en la API: modelos/DTOs, tablas Exposed, DAOs, mappers, repositorios, rutas, autorización, documentación, OpenAPI si existe, tests, schema fresco, migración incremental, seed y reset.
- Las migraciones deben ser deterministas, ordenadas, transaccionales cuando PostgreSQL lo permita y seguras para datos existentes. Para cambios grandes, separar expansión, migración/backfill y limpieza; evitar bloquear tablas grandes innecesariamente.
- Si el seed inserta IDs explícitos con `OVERRIDING SYSTEM VALUE`, sincronizar las secuencias/identities al final para que el siguiente insert generado no choque con IDs existentes.
- El reset debe eliminar solo los objetos propiedad del proyecto. Nunca incluir comandos destructivos generales (`DROP DATABASE`, `DROP SCHEMA public`, etc.) salvo solicitud expresa y confirmación del usuario.

## Seguridad y autenticación

Cuando una funcionalidad requiera autenticación, usar JWT mediante la configuración existente `auth-jwt`; no inventar un mecanismo alternativo.

- Declarar de forma explícita qué rutas son públicas y cuáles están protegidas. Las protegidas deben quedar dentro de `authenticate("auth-jwt")` en `Routing.kt`.
- Implementar controles de pertenencia/autorización de dominio además de validar el JWT cuando el recurso pertenece a un usuario o gimnasio.
- Cuando se implemente autenticación, proveer y documentar endpoints equivalentes a login, refresh y logout (por ejemplo, `POST /login`, `POST /auth/refresh` y `POST /auth/logout`): access token JWT, refresh token opaco aleatorio, almacenamiento exclusivo de su hash, rotación atómica y revocación idempotente. Los paths concretos los define cada proyecto.
- Si la autenticación se agrega a un proyecto o área nueva, incluir las tablas necesarias en el esquema, su migración, seed y reset, además de documentar login, refresh, logout y las rutas protegidas/públicas.
- Usar `PasswordService`/Spring Security Crypto (BCrypt) para hashear y verificar contraseñas. Nunca almacenar, registrar, devolver ni seedear contraseñas en texto plano.

## Contrato REST, validación y errores

- Diseñar recursos con nombres plurales y sustantivos (`/products`, `/orders`). Usar subrecursos solo cuando expresen una relación real (`/users/{userId}/orders`); no crear endpoints de estilo RPC salvo que el caso de uso lo justifique y se documente.
- Usar el método HTTP correcto y conservar semántica REST: `GET` no cambia estado; `POST` crea; `PUT` reemplaza completamente; `PATCH` aplica cambios parciales si el proyecto lo admite; `DELETE` elimina o revoca según el dominio.
- No alterar paths, payloads, semántica ni códigos de respuesta de una API publicada sin evaluar compatibilidad, actualizar documentación y contar con aprobación cuando el cambio sea breaking.
- Definir DTOs de request y response separados de las entidades de dominio/persistencia cuando sus contratos difieran. Por ejemplo, `CreateProductRequest`, `UpdateProductRequest` y `ProductResponse`. No aceptar campos controlados por el servidor (`id`, auditoría, propietario) como fuente de verdad desde el cliente.
- Validar antes de persistir: `Content-Type`, campos requeridos, formato, longitud, rangos, enumeraciones y referencias/relaciones existentes. Normalizar solo cuando sea seguro y explícito (por ejemplo, `trim` en email).
- Instalar y mantener un manejo centralizado con `StatusPages`. Las excepciones de validación, serialización, autorización, no encontrado, conflicto y fallos inesperados deben producir respuestas controladas.
- Usar un formato de error estable y documentado. Como mínimo debe incluir `code` legible por máquina y `message` legible por personas; para validaciones de campos, incluir `fields` con el detalle por campo. Nunca enviar stack traces, secretos, hashes, tokens, consultas SQL ni detalles internos al cliente.
- Conservar y documentar el uso local de `400` para requests malformados o parámetros inválidos, `401` sin autenticación válida, `403` sin permiso, `404` inexistente, `409` para conflictos de estado/duplicados y `422` para contenido sintácticamente válido pero semánticamente inválido si el proyecto adopta ese código.

## Autorización y operaciones consistentes

- Autenticación y autorización son responsabilidades distintas: el JWT identifica al usuario, pero cada endpoint protegido debe verificar roles, permisos y propiedad/pertenencia del recurso según el dominio.
- Centralizar helpers o servicios de autorización reutilizables; no copiar comprobaciones de rol de forma divergente entre rutas.
- Para colecciones que puedan crecer, implementar paginación, filtros y ordenamiento. Validar límites máximos, usar valores por defecto explícitos y documentar todos los parámetros y el formato de la respuesta paginada.
- Proteger operaciones concurrentes y críticas (por ejemplo, stock, pagos, cupos, asignaciones o rotación de tokens) con transacciones, restricciones/índices de PostgreSQL y actualizaciones condicionales cuando corresponda. No confiar únicamente en validaciones previas en memoria.
- Mantener operaciones idempotentes cuando su semántica lo permita, especialmente `DELETE`, revocaciones y comandos que puedan reintentarse por red.

## Pruebas, documentación y operación

- Añadir o actualizar pruebas de ruta/integración para cada endpoint: respuesta exitosa, request inválido, no autenticado, no autorizado, recurso inexistente y conflictos relevantes. Añadir pruebas unitarias para lógica de negocio, mappers y servicios de seguridad.
- Las pruebas que dependan de PostgreSQL deben partir de un esquema/seed controlado y no usar datos locales del desarrollador. No conectar tests automáticamente a una base de producción o compartida.
- Mantener la documentación Markdown por endpoint y el índice de API. Si el proyecto incorpora OpenAPI/Swagger, tratar su especificación como contrato: actualizarla en el mismo cambio que las rutas y mantenerla consistente con ejemplos, autenticación y respuestas reales.
- Incluir un endpoint de healthcheck sin datos sensibles cuando el entorno lo requiera. Registrar fallos y eventos relevantes de forma estructurada, sin contraseñas, tokens, hashes, datos personales innecesarios ni cuerpos completos de requests sensibles.
- Configurar CORS con orígenes, métodos y headers explícitos por entorno; no habilitar todos los orígenes en producción sin aprobación explícita.
- Para cada migración, documentar el impacto sobre datos existentes y comprobar que preserve información. Cuando la naturaleza del cambio lo permita, incluir una estrategia de reversión; nunca usar el script de reset como mecanismo de migración en entornos con datos persistentes.

## Evolución de API y resiliencia

- Definir una estrategia de versionado antes de publicar la API (por ejemplo, prefijo `/api/v1` o versionado por header) y aplicarla de manera uniforme. No introducir una versión nueva por cambios compatibles; reservarla para rupturas reales de contrato.
- Para cambios incompatibles, identificar consumidores, documentar la migración, mantener un período de deprecación acordado cuando sea necesario y no eliminar el contrato anterior sin aprobación explícita.
- Establecer límites razonables para tamaño de body, parámetros de paginación y cargas de archivos si el proyecto las admite. Validar tipo, tamaño y contenido de archivos antes de almacenarlos; no confiar en extensión o `Content-Type` enviados por el cliente.
- Cuando una operación de creación pueda repetirse por reintentos de red y genere efectos sensibles (pagos, reservas, altas externas), evaluar y documentar claves de idempotencia. No reintentar automáticamente escrituras no idempotentes sin un mecanismo que evite duplicados.
- Para llamadas HTTP, mensajería, correo, almacenamiento u otras dependencias externas, definir timeout explícito, manejo de errores y reintentos limitados solo donde sea seguro. No hacer llamadas de red sin timeout ni registrar secretos de terceros.
- No retornar detalles de proveedores externos directamente al cliente. Traducirlos a errores de dominio documentados y registrar el diagnóstico técnico de forma segura.

## Configuración, observabilidad y entrega

- Mantener un README de arranque que indique requisitos (JDK, PostgreSQL), cómo configurar el entorno, crear/resetear/seedear la base, ejecutar migraciones, correr pruebas y levantar la API. Mantenerlo alineado con los scripts reales.
- Separar configuración por entorno con archivos de configuración explícitos y reproducibles. Los valores sensibles de producción deben inyectarse mediante el mecanismo seguro aprobado por infraestructura y nunca versionarse; los valores de desarrollo y pruebas previas al deploy deben permanecer en configuración de proyecto según esta guía.
- Definir una configuración de logging con niveles apropiados. Incluir, cuando sea viable, un identificador de correlación por request en logs y respuestas de error para poder investigar incidentes sin exponer internals.
- Distinguir healthcheck de liveness y readiness cuando el despliegue lo necesite: la readiness puede comprobar dependencias esenciales como PostgreSQL, mientras que la liveness no debe fallar por una dependencia temporal. No incluir credenciales ni detalles internos en estas respuestas.
- Añadir métricas o trazas solo si existe una plataforma definida para consumirlas; no incorporar una dependencia de observabilidad sin aprobación. Como mínimo, los logs deben permitir detectar errores, latencia y fallos de dependencias.
- Ejecutar en integración continua las verificaciones ya disponibles en el proyecto (como compilación, tests y chequeos de formato). Si no existe CI, documentar el comando reproducible que debe ejecutarse antes de integrar cambios. No agregar herramientas de lint, análisis estático o dependencias de build sin verificar compatibilidad y respetar la política de aprobación de dependencias.
- Usar el Gradle Wrapper versionado para toda compilación y automatización. No depender de una instalación global de Gradle ni modificar el wrapper para resolver un cambio funcional sin aprobación.

## Arranque de un proyecto nuevo

Antes del primer endpoint de negocio, establecer y documentar: paquete raíz, estructura de capas, convención de rutas, estrategia de versionado, formato de errores, autenticación/autorización esperada, nombres de scripts SQL, perfiles de configuración y comandos de desarrollo/prueba.

Crear desde el inicio el schema, seed y reset con el nombre del proyecto; una migración inicial si el mecanismo de migración ya está definido; `.gitignore`; `.env.example` sin secretos; `application.conf` de ejemplo; README; pruebas mínimas de arranque y healthcheck; y documentación de endpoints. La base debe poder recrearse de forma reproducible sin depender de estado local no versionado.

No habilitar automáticamente creación o modificación de schema por el ORM al iniciar la aplicación en entornos persistentes. Los cambios de estructura deben pasar por los scripts SQL y migraciones revisables definidos en este documento.

## Configuración y dependencias base

La configuración de desarrollo, pruebas previas al deploy y despliegues internos debe residir en archivos de configuración del proyecto (HOCON, por ejemplo `src/main/resources/application.conf` o un archivo de entorno explícito), para que el equipo pueda reproducirla. No introducir nuevas variables de entorno para esa fase. No versionar secretos reales: usar placeholders/valores locales no sensibles y mantener el archivo local sensible fuera de Git.

La base tecnológica esperada es Ktor, Koin, Exposed, HikariCP, PostgreSQL, `ktor-server-auth-jwt`, `kotlinx.serialization` y Spring Security Crypto. Para un proyecto nuevo Kotlin/Ktor con esta guía, dejar preconfigurados desde el inicio Ktor/Netty, Koin, PostgreSQL + Hikari + Exposed, serialización JSON, JWT, BCrypt, scripts `<proyecto>_schema.sql`/`seed_test_data.sql`/`reset_<proyecto>_schema.sql`, pruebas mínimas, `.gitignore`, `.env.example` y documentación inicial antes de construir endpoints de negocio.

`.env.example` debe incluir únicamente ejemplos de variables de base de datos (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_POOL_SIZE`) y nunca credenciales reales. En este repositorio ya existe y debe mantenerse actualizado si cambia ese contrato. `.gitignore` debe conservar ignorados archivos locales, artefactos de Gradle/IDE, logs y secretos, dejando permitido explícitamente `.env.example` y el wrapper de Gradle.

## Kotlin, Ktor y calidad

- Usar `data class` inmutables para modelos/DTOs, nullability precisa y `@Serializable` en payloads JSON. Mantener fechas en formatos ISO-8601 acordados por la documentación.
- Mantener rutas finas: parsear/validar parámetros HTTP, delegar al repositorio y devolver el status adecuado. No duplicar SQL ni lógica de persistencia en rutas.
- Reutilizar mappers y helpers de referencias existentes; no filtrar entidades Exposed/DAO directamente como respuesta HTTP.
- Hacer las operaciones que cambian varios registros de manera atómica dentro de una transacción.
- Seguir las convenciones locales de nombres y paquetes. No hacer refactors o reformateos ajenos al objetivo.
- Ejecutar `./gradlew.bat test` para cambios que toquen Kotlin o pruebas; ejecutar una compilación apropiada cuando sea viable. Informar de forma clara si una verificación no pudo realizarse y por qué.

## Antes de entregar

Comprobar que el cambio no contiene secretos, que los modelos/API/Exposed/SQL están alineados, que la documentación refleja el comportamiento real y que los tests relevantes pasan. Resumir los archivos modificados y toda verificación ejecutada.
