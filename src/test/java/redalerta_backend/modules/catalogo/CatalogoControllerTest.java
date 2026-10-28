package redalerta_backend.modules.catalogo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.modules.catalogo.controller.CatalogoController;
import redalerta_backend.modules.catalogo.dto.DepartamentoResponseDto;
import redalerta_backend.modules.catalogo.service.impl.CatalogoGeograficoServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoControllerTest {

    private final CatalogoController controller = new CatalogoController(new CatalogoGeograficoServiceImpl());

    @Test
    @DisplayName("Debe retornar catalogo completo exitosamente")
    void testObtenerUbicaciones() {
        ResponseEntity<ApiResponse<List<DepartamentoResponseDto>>> response = controller.obtenerUbicaciones();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertFalse(response.getBody().getData().isEmpty());
    }

    @Test
    @DisplayName("Debe retornar departamentos exitosamente")
    void testObtenerDepartamentos() {
        ResponseEntity<ApiResponse<List<String>>> response = controller.obtenerDepartamentos();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getData().contains("Quindío"));
    }

    @Test
    @DisplayName("Debe retornar municipios de un departamento por busqueda agnostica a tildes")
    void testObtenerMunicipios() {
        ResponseEntity<ApiResponse<List<String>>> response = controller.obtenerMunicipios("quindio");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(12, response.getBody().getData().size());
        assertTrue(response.getBody().getData().contains("Armenia"));
    }
}
