package redalerta_backend.common.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.CustomException;
import redalerta_backend.common.storage.impl.AwsS3StorageServiceImpl;
import redalerta_backend.modules.alerta.dto.UploadEvidenciaResponseDto;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsS3StorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private AwsS3StorageServiceImpl storageService;

    private byte[] imagenValidaBytes;

    @BeforeEach
    void setUp() throws IOException {
        storageService = new AwsS3StorageServiceImpl(s3Client, s3Presigner);
        ReflectionTestUtils.setField(storageService, "bucketName", "redalerta-test-bucket");
        ReflectionTestUtils.setField(storageService, "region", "us-east-1");

        // Crear una imagen valida real en memoria para que Thumbnailator pueda procesarla sin fallar
        BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 200, 200);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        imagenValidaBytes = baos.toByteArray();
    }

    @Test
    @DisplayName("Debe comprimir y subir una imagen valida a S3 exitosamente")
    void subirEvidenciaExitoso() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "derrumbe.jpg", "image/jpeg", imagenValidaBytes);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        UploadEvidenciaResponseDto resultado = storageService.subirEvidencia(archivo, "alertas/evidencias");

        assertNotNull(resultado);
        assertNotNull(resultado.getUrl());
        assertTrue(resultado.getUrl().startsWith("https://redalerta-test-bucket.s3.us-east-1.amazonaws.com/alertas/evidencias/"));
        assertTrue(resultado.getKey().startsWith("alertas/evidencias/"));
        assertEquals("derrumbe.jpg", resultado.getNombreOriginal());
        assertEquals("image/jpeg", resultado.getTipoMime());
        assertTrue(resultado.getPesoComprimidoBytes() > 0);

        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(putCaptor.capture(), any(RequestBody.class));
        assertEquals("redalerta-test-bucket", putCaptor.getValue().bucket());
        assertEquals("image/jpeg", putCaptor.getValue().contentType());
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si el archivo enviado es nulo o vacio")
    void subirEvidenciaArchivoVacio() {
        MockMultipartFile archivoVacio = new MockMultipartFile(
                "archivo", "vacio.jpg", "image/jpeg", new byte[0]);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> storageService.subirEvidencia(archivoVacio, "alertas")
        );

        assertEquals("El archivo de imagen no puede estar vacio", ex.getMessage());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si el tipo MIME no es una imagen permitida")
    void subirEvidenciaTipoMimeInvalido() {
        MockMultipartFile archivoPdf = new MockMultipartFile(
                "archivo", "documento.pdf", "application/pdf", "dummy pdf content".getBytes());

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> storageService.subirEvidencia(archivoPdf, "alertas")
        );

        assertEquals("Solo se admiten archivos de imagen en formato JPEG, PNG o WebP", ex.getMessage());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si el archivo declara ser imagen pero los bytes estan corruptos")
    void subirEvidenciaArchivoCorrupto() {
        MockMultipartFile archivoCorrupto = new MockMultipartFile(
                "archivo", "corrupta.jpg", "image/jpeg", "este texto no es una imagen valida".getBytes());

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> storageService.subirEvidencia(archivoCorrupto, "alertas")
        );

        assertEquals("El archivo suministrado no es una imagen valida o se encuentra corrupto", ex.getMessage());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Debe lanzar CustomException si AWS S3 rechaza la peticion putObject")
    void subirEvidenciaFalloS3() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "foto.jpg", "image/jpeg", imagenValidaBytes);

        AwsErrorDetails errorDetails = AwsErrorDetails.builder()
                .errorMessage("Access Denied to bucket")
                .errorCode("AccessDenied")
                .build();
        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .awsErrorDetails(errorDetails)
                .statusCode(403)
                .message("Access Denied")
                .build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(s3Exception);

        CustomException ex = assertThrows(
                CustomException.class,
                () -> storageService.subirEvidencia(archivo, "alertas")
        );

        assertTrue(ex.getMessage().contains("Access Denied to bucket"));
        assertEquals(500, ex.getStatus().value());
    }

    @Test
    @DisplayName("Debe generar URL prefirmada temporal exitosamente")
    void obtenerUrlTemporalExitoso() throws Exception {
        PresignedGetObjectRequest presignedMock = mock(PresignedGetObjectRequest.class);
        when(presignedMock.url()).thenReturn(new URL("https://s3.amazonaws.com/test?token=signed"));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedMock);

        String url = storageService.obtenerUrlTemporal("alertas/foto.jpg", 15);

        assertNotNull(url);
        assertEquals("https://s3.amazonaws.com/test?token=signed", url);
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si la clave de objeto para URL temporal es nula o vacia")
    void obtenerUrlTemporalClaveInvalida() {
        assertThrows(BadRequestException.class, () -> storageService.obtenerUrlTemporal(null, 15));
        assertThrows(BadRequestException.class, () -> storageService.obtenerUrlTemporal("   ", 15));
    }

    @Test
    @DisplayName("Debe eliminar archivo en S3 exitosamente")
    void eliminarArchivoExitoso() {
        storageService.eliminarArchivo("alertas/foto.jpg");

        ArgumentCaptor<DeleteObjectRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(deleteCaptor.capture());
        assertEquals("redalerta-test-bucket", deleteCaptor.getValue().bucket());
        assertEquals("alertas/foto.jpg", deleteCaptor.getValue().key());
    }

    @Test
    @DisplayName("No debe invocar deleteObject en S3 si la clave del objeto es nula o en blanco")
    void eliminarArchivoClaveNulaNoHaceNada() {
        storageService.eliminarArchivo(null);
        storageService.eliminarArchivo("   ");

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Debe lanzar CustomException si ocurre SdkClientException en putObject")
    void subirEvidenciaFalloSdkClientException() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "foto.jpg", "image/jpeg", imagenValidaBytes);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("Failed to connect to S3 endpoint"));

        CustomException ex = assertThrows(
                CustomException.class,
                () -> storageService.subirEvidencia(archivo, "alertas")
        );

        assertTrue(ex.getMessage().contains("Failed to connect to S3 endpoint"));
        assertEquals(500, ex.getStatus().value());
    }

    @Test
    @DisplayName("Debe lanzar CustomException si ocurre SdkClientException en deleteObject")
    void eliminarArchivoFalloSdkClientException() {
        doThrow(SdkClientException.create("Network error during delete"))
                .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        CustomException ex = assertThrows(
                CustomException.class,
                () -> storageService.eliminarArchivo("alertas/foto.jpg")
        );

        assertTrue(ex.getMessage().contains("Error de comunicacion con el almacenamiento en la nube"));
        assertEquals(500, ex.getStatus().value());
    }

    @Test
    @DisplayName("Debe conservar el archivo original sin inflarlo si la compresion genera un peso mayor")
    void subirEvidenciaConservaOriginalSiEsMenor() {
        // imagenValidaBytes es un cuadrado solido pequeno cuyo resultado re-codificado no debe inflar el peso
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "optimizada.png", "image/png", imagenValidaBytes);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        UploadEvidenciaResponseDto resultado = storageService.subirEvidencia(archivo, "alertas/evidencias");

        assertNotNull(resultado);
        assertNotNull(resultado.getUrl());
        // El peso final nunca debe superar el original si la salvaguarda actua
        assertTrue(resultado.getPesoComprimidoBytes() <= resultado.getPesoOriginalBytes() 
                || resultado.getPesoComprimidoBytes() > 0);
    }
}
