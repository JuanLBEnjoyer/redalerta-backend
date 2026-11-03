package redalerta_backend.modules.ubicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.util.TextoGeograficoUtil;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;
import redalerta_backend.modules.ubicacion.dto.DepartamentoMunicipiosDto;
import redalerta_backend.modules.ubicacion.service.impl.UbicacionCatalogoServiceImpl;
import redalerta_backend.modules.usuario.valueobject.Ubicacion;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UbicacionCatalogoServiceTest {

    private UbicacionCatalogoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UbicacionCatalogoServiceImpl();
        service.inicializarCatalogo();
    }

    @Test
    @DisplayName("Debe cargar los 32 departamentos de Colombia mas Bogota D.C. desde el archivo JSON")
    void debeCargarDepartamentosDesdeJson() {
        List<String> departamentos = service.listarDepartamentos();
        assertNotNull(departamentos);
        assertEquals(33, departamentos.size());
        assertTrue(departamentos.contains("Quindío"));
        assertTrue(departamentos.contains("Antioquia"));
        assertTrue(departamentos.contains("Bogotá, D.C."));
    }

    @Test
    @DisplayName("Debe retornar los 12 municipios del Quindio buscando con o sin tilde y en cualquier capitalizacion")
    void debeListarMunicipiosInsensibleATildesYMayusculas() {
        List<String> conTilde = service.listarMunicipiosPorDepartamento("Quindío");
        List<String> sinTilde = service.listarMunicipiosPorDepartamento("Quindio");
        List<String> enMayusculas = service.listarMunicipiosPorDepartamento("  QUINDIO  ");

        assertEquals(12, conTilde.size());
        assertEquals(conTilde, sinTilde);
        assertEquals(conTilde, enMayusculas);
        assertTrue(conTilde.contains("Armenia"));
        assertTrue(conTilde.contains("Calarcá"));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el departamento no existe")
    void debeLanzarNotFoundParaDepartamentoInexistente() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.listarMunicipiosPorDepartamento("Wakanda"));
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException cuando el departamento es nulo o vacio")
    void debeLanzarBadRequestParaDepartamentoVacio() {
        assertThrows(BadRequestException.class,
                () -> service.listarMunicipiosPorDepartamento("   "));
        assertThrows(BadRequestException.class,
                () -> service.listarMunicipiosPorDepartamento(null));
    }

    @Test
    @DisplayName("Debe validar combinaciones reales de departamento y municipio con o sin tildes")
    void debeValidarCombinacionesGeograficas() {
        assertTrue(service.esUbicacionValida("Quindío", "Calarcá"));
        assertTrue(service.esUbicacionValida("Quindio", "Calarca"));
        assertTrue(service.esUbicacionValida("Amazonas", "Puerto Nariño"));
        assertFalse(service.esUbicacionValida("Quindío", "Medellín"));
        assertFalse(service.esUbicacionValida("Inexistente", "Armenia"));
    }

    @Test
    @DisplayName("Debe garantizar que Ubicacion de registro y UbicacionAlerta de GPS coincidan tras normalizar tildes")
    void debeGarantizarConsistenciaEntreRegistroManualYGps() {
        Ubicacion ubicacionUsuario = new Ubicacion("Quindío", "Calarcá", "Carrera 25 # 39-12");
        UbicacionAlerta ubicacionGps = new UbicacionAlerta("Quindio", "Calarca", null, 4.5278, -75.6406, 1573.0);

        assertEquals(ubicacionUsuario.departamento(), ubicacionGps.departamento());
        assertEquals(ubicacionUsuario.municipio(), ubicacionGps.municipio());
        assertEquals("Quindio", ubicacionUsuario.departamento());
        assertEquals("Calarca", ubicacionUsuario.municipio());
        assertEquals("Nariño", TextoGeograficoUtil.normalizarNombreGeografico("Nariño"));
    }

    @Test
    @DisplayName("Debe obtener el catalogo completo con sus municipios")
    void debeObtenerCatalogoCompleto() {
        List<DepartamentoMunicipiosDto> catalogo = service.obtenerCatalogoCompleto();
        assertEquals(33, catalogo.size());
        int totalMunicipios = catalogo.stream().mapToInt(d -> d.municipios().size()).sum();
        assertTrue(totalMunicipios >= 1100);
    }
}

