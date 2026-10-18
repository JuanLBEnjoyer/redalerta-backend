package redalerta_backend.modules.catalogo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.modules.catalogo.dto.DepartamentoResponseDto;
import redalerta_backend.modules.catalogo.service.CatalogoGeograficoService;

import java.util.List;

/**
 * Controlador REST publico para la consulta de catalogos geograficos.
 * Provee datos predefinidos para inicializar listas desplegables en el cliente frontend.
 */
@RestController
@RequestMapping("/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catálogos", description = "Endpoints públicos para consulta de catálogos y listas predefinidas")
public class CatalogoController {

    private final CatalogoGeograficoService catalogoService;

    @GetMapping("/ubicaciones")
    @Operation(
            summary = "Obtener catálogo geográfico completo",
            description = "Retorna todos los departamentos y sus respectivos municipios disponibles en el sistema"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Catálogo recuperado exitosamente")
    })
    public ResponseEntity<ApiResponse<List<DepartamentoResponseDto>>> obtenerUbicaciones() {
        List<DepartamentoResponseDto> catalogo = catalogoService.obtenerCatalogoCompleto();
        return ResponseEntity.ok(ApiResponse.success(catalogo, "Catálogo geográfico recuperado exitosamente"));
    }

    @GetMapping("/departamentos")
    @Operation(
            summary = "Listar departamentos disponibles",
            description = "Retorna la lista de nombres de departamentos soportados en el sistema"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Departamentos recuperados exitosamente")
    })
    public ResponseEntity<ApiResponse<List<String>>> obtenerDepartamentos() {
        List<String> departamentos = catalogoService.obtenerDepartamentos();
        return ResponseEntity.ok(ApiResponse.success(departamentos, "Departamentos recuperados exitosamente"));
    }

    @GetMapping("/departamentos/{departamento}/municipios")
    @Operation(
            summary = "Listar municipios por departamento",
            description = "Retorna la lista oficial de municipios para un departamento dado (búsqueda agnóstica a tildes)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Municipios recuperados exitosamente")
    })
    public ResponseEntity<ApiResponse<List<String>>> obtenerMunicipios(
            @Parameter(description = "Nombre del departamento", example = "Quindío")
            @PathVariable String departamento
    ) {
        List<String> municipios = catalogoService.obtenerMunicipiosPorDepartamento(departamento);
        return ResponseEntity.ok(ApiResponse.success(municipios, "Municipios recuperados exitosamente"));
    }
}
