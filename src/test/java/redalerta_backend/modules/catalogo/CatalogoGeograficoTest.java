package redalerta_backend.modules.catalogo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redalerta_backend.modules.catalogo.domain.CatalogoGeografico;
import redalerta_backend.modules.catalogo.dto.DepartamentoResponseDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoGeograficoTest {

    @Test
    @DisplayName("Debe listar los 12 municipios oficiales del departamento del Quindío")
    void testMunicipiosQuindio() {
        List<String> municipios = CatalogoGeografico.obtenerMunicipios("Quindío");
        assertNotNull(municipios);
        assertEquals(12, municipios.size());
        assertTrue(municipios.contains("Armenia"));
        assertTrue(municipios.contains("Calarcá"));
        assertTrue(municipios.contains("Circasia"));
        assertTrue(municipios.contains("Montenegro"));
        assertTrue(municipios.contains("Quimbaya"));
        assertTrue(municipios.contains("Salento"));
    }

    @Test
    @DisplayName("Debe canonicalizar nombres con y sin tildes, mayusculas y espacios")
    void testCanonicalizacionDepartamento() {
        assertEquals("Quindío", CatalogoGeografico.canonicalizarDepartamento("quindio"));
        assertEquals("Quindío", CatalogoGeografico.canonicalizarDepartamento("QUINDIO"));
        assertEquals("Quindío", CatalogoGeografico.canonicalizarDepartamento("  Quindío  "));
        assertEquals("Antioquia", CatalogoGeografico.canonicalizarDepartamento("antioquia"));
        assertEquals("Bogotá D.C.", CatalogoGeografico.canonicalizarMunicipio("Cundinamarca", "bogota d.c."));
        assertEquals("Calarcá", CatalogoGeografico.canonicalizarMunicipio("Quindio", "calarca"));
    }

    @Test
    @DisplayName("Debe retornar catalogo completo estructurado")
    void testCatalogoCompleto() {
        List<DepartamentoResponseDto> catalogo = CatalogoGeografico.obtenerCatalogo();
        assertNotNull(catalogo);
        assertFalse(catalogo.isEmpty());
        assertTrue(catalogo.stream().anyMatch(d -> d.departamento().equals("Quindío")));
    }

    @Test
    @DisplayName("Debe validar existencia de departamento y municipio correctamente")
    void testValidaciones() {
        assertTrue(CatalogoGeografico.esDepartamentoValido("quindio"));
        assertTrue(CatalogoGeografico.esDepartamentoValido("Quindío"));
        assertFalse(CatalogoGeografico.esDepartamentoValido("DepartamentoFicticio"));

        assertTrue(CatalogoGeografico.esMunicipioValido("Quindio", "armenia"));
        assertTrue(CatalogoGeografico.esMunicipioValido("Quindío", "Calarcá"));
        assertFalse(CatalogoGeografico.esMunicipioValido("Quindío", "Medellin"));
    }
}
