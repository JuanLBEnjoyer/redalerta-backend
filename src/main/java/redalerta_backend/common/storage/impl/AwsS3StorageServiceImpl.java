package redalerta_backend.common.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.CustomException;
import redalerta_backend.common.storage.StorageService;
import redalerta_backend.modules.alerta.dto.UploadEvidenciaResponseDto;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Implementacion del servicio de almacenamiento utilizando AWS S3 y optimizacion automatica de imagenes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AwsS3StorageServiceImpl implements StorageService {

    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final int MAX_ANCHO_PIXELES = 1920;
    private static final int MAX_ALTO_PIXELES = 1080;
    private static final double CALIDAD_COMPRESION = 0.80;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Override
    public UploadEvidenciaResponseDto subirEvidencia(MultipartFile archivo, String carpeta) {
        validarArchivo(archivo);

        byte[] bytesComprimidos = comprimirImagen(archivo);

        String nombreLimpio = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "evidencia.jpg";
        String prefijoCarpeta = (carpeta != null && !carpeta.isBlank()) ? carpeta.trim().replaceAll("^/+|/+$", "") + "/" : "";
        String claveObjeto = prefijoCarpeta + UUID.randomUUID() + ".jpg";

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(claveObjeto)
                    .contentType("image/jpeg")
                    .contentLength((long) bytesComprimidos.length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(bytesComprimidos));
            log.info("Evidencia subida a S3 con exito: bucket={}, key={}, pesoOriginal={} bytes, pesoComprimido={} bytes",
                    bucketName, claveObjeto, archivo.getSize(), bytesComprimidos.length);
        } catch (S3Exception e) {
            log.error("Fallo al subir objeto a AWS S3: {}", e.awsErrorDetails().errorMessage(), e);
            throw new CustomException("Error en el almacenamiento de evidencias en la nube: " + e.awsErrorDetails().errorMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String urlPublica = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, claveObjeto);

        return UploadEvidenciaResponseDto.builder()
                .url(urlPublica)
                .key(claveObjeto)
                .nombreOriginal(nombreLimpio)
                .pesoOriginalBytes(archivo.getSize())
                .pesoComprimidoBytes((long) bytesComprimidos.length)
                .tipoMime("image/jpeg")
                .build();
    }

    @Override
    public String obtenerUrlTemporal(String claveObjeto, int minutosValidez) {
        if (claveObjeto == null || claveObjeto.isBlank()) {
            throw new BadRequestException("La clave del objeto no puede estar vacia");
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(claveObjeto)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(minutosValidez > 0 ? minutosValidez : 15))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            return presigned.url().toString();
        } catch (Exception e) {
            log.error("Error al generar URL prefirmada para el objeto: {}", claveObjeto, e);
            throw new CustomException("Error al generar enlace seguro de visualizacion", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void eliminarArchivo(String claveObjeto) {
        if (claveObjeto == null || claveObjeto.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(claveObjeto)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Objeto eliminado de S3: bucket={}, key={}", bucketName, claveObjeto);
        } catch (S3Exception e) {
            log.error("Error al eliminar objeto de S3: {}", claveObjeto, e);
            throw new CustomException("Error al eliminar archivo del almacenamiento en la nube", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("El archivo de imagen no puede estar vacio");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Solo se admiten archivos de imagen en formato JPEG, PNG o WebP");
        }
    }

    private byte[] comprimirImagen(MultipartFile archivo) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(archivo.getInputStream())
                    .size(MAX_ANCHO_PIXELES, MAX_ALTO_PIXELES)
                    .outputFormat("jpg")
                    .outputQuality(CALIDAD_COMPRESION)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error al procesar y comprimir la imagen: {}", archivo.getOriginalFilename(), e);
            throw new BadRequestException("El archivo suministrado no es una imagen valida o se encuentra corrupto");
        }
    }
}
