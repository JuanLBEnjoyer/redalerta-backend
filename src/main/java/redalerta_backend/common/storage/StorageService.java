package redalerta_backend.common.storage;

import org.springframework.web.multipart.MultipartFile;
import redalerta_backend.modules.alerta.dto.UploadEvidenciaResponseDto;

/**
 * Puerto de servicio para el almacenamiento y gestion de archivos multimedia en la nube.
 */
public interface StorageService {

    /**
     * Valida, comprime y sube una evidencia fotografica al almacenamiento de objetos.
     *
     * @param archivo Archivo binario enviado en la solicitud multipart.
     * @param carpeta Carpeta o prefijo de destino dentro del almacenamiento (ej. "alertas").
     * @return DTO con la URL, clave de objeto y metadatos de compresion.
     */
    UploadEvidenciaResponseDto subirEvidencia(MultipartFile archivo, String carpeta);

    /**
     * Genera una URL prefirmada temporal para visualizacion segura de un objeto privado.
     *
     * @param claveObjeto Clave o ruta del objeto en el almacenamiento.
     * @param minutosValidez Duracion en minutos antes de que la URL expire.
     * @return URL prefirmada con token de firma temporal.
     */
    String obtenerUrlTemporal(String claveObjeto, int minutosValidez);

    /**
     * Elimina un archivo del almacenamiento a partir de su clave de objeto.
     *
     * @param claveObjeto Clave o ruta del objeto a eliminar.
     */
    void eliminarArchivo(String claveObjeto);
}
