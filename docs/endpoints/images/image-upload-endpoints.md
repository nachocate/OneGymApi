# Carga local de imágenes

Las imágenes se almacenan como archivos locales y PostgreSQL conserva únicamente la URL relativa devuelta por la API. La API sirve los archivos bajo `/media`.

## Subir una imagen de un gimnasio

```http
POST /gyms/{gymId}/uploads/images
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

Enviar exactamente un campo de archivo llamado `image`.

Ejemplo con cURL:

```bash
curl -X POST "http://localhost:8080/gyms/1/uploads/images" \
  -H "Authorization: Bearer <accessToken>" \
  -F "image=@C:/imagenes/remo.png"
```

Requiere `COACH`, `ADMIN`, `OWNER` del gimnasio o `SUPER_ADMIN`.

## Respuesta

`201 Created`:

```json
{
  "url": "/media/images/gym-1/6fbbd8bf-7fa5-41e7-8741-2b2cce0dd7bb.jpg",
  "fileName": "6fbbd8bf-7fa5-41e7-8741-2b2cce0dd7bb.jpg",
  "contentType": "image/jpeg",
  "width": 1920,
  "height": 1080,
  "sizeBytes": 184320
}
```

Persistir el valor de `url` en `Gym.logoUrl`, `Gym.bannerUrl`, `News.imageUrl` o `Exercise.imageUrl`. La URL es relativa: el frontend la resuelve contra su `BASE_URL`, por ejemplo `http://localhost:8080/media/...`.

## Validaciones y optimización

- Acepta únicamente JPEG y PNG validados por su contenido real, no solo por el header o la extensión.
- Tamaño máximo de upload: 5 MiB.
- Máximo de 16.000.000 píxeles antes de decodificar la imagen completa.
- Las imágenes se redimensionan para que su lado mayor no supere 1920 px.
- Las imágenes sin transparencia se guardan como JPEG con calidad 0,82; las que preservan transparencia se guardan como PNG.
- Los archivos se guardan en `uploads/images/gym-{gymId}/`, carpeta ignorada por Git.

La compresión y la escritura se ejecutan en `Dispatchers.IO`, fuera de los hilos de atención HTTP de Ktor.

## Errores

- `400 Bad Request`: falta el campo `image`, se envió más de una imagen, el archivo supera el límite, no es JPEG/PNG o sus dimensiones son inválidas.
- `401 Unauthorized`: falta o venció el JWT.
- `403 Forbidden`: el usuario no puede gestionar contenido del gimnasio.

## Ciclo de vida

No hay endpoint de borrado de archivos por ahora. No borrar un archivo simplemente al editar una URL: podría estar referenciado por otra entidad. La limpieza de archivos sin referencias debe implementarse después como una tarea administrativa que compruebe referencias en base de datos.
