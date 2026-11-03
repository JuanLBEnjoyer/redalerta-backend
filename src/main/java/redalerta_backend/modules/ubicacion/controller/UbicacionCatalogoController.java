package redalerta_backend.modules.ubicacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.modules.ubicacion.dto.DepartamentoMunicipiosDto;
import redalerta_backend.modules.ubicacion.service.UbicacionCatalogoService;

import java.util.List;

/**
 * Controlador REST publico para proveer el catalogo oficial de departamentos y municipios de Colombia.
 * Alimenta las listas desplegables (Select / Spinner) en los formularios de registro y filtros de consulta.
 */
@RestController
@RequestMapping("/ubicaciones")
@Tag(name = "Catalogo de Ubicaciones", description = "Consulta publica de departamentos y municipios de Colombia (DIVIPOLA)")
public class UbicacionCatalogoController {

    private final UbicacionCatalogoService ubicacionCatalogoService;

    public UbicacionCatalogoController(UbicacionCatalogoService ubicacionCatalogoService) {
        super();
        this.ubicacionCatalogoService = ubicacionCatalogoService;
    }

    @Operation(
            summary = "Listar departamentos de Colombia",
            description = "Retorna la lista alfabetica de los 32 departamentos de Colombia y Bogota, D.C."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lista de departamentos obtenida exitosamente"
            )
    })
    @GetMapping("/departamentos")
    public ResponseEntity<ApiResponse<List<String>>> listarDepartamentos() {
        List<String> departamentos = ubicacionCatalogoService.listarDepartamentos();
        return ResponseEntity.ok(
                ApiResponse.success(departamentos, "Departamentos consultados exitosamente")
        );
    }

    @Operation(
            summary = "Listar municipios por departamento",
            description = "Retorna los municipios pertenecientes al departamento indicado. "
                    + "La busqueda es insensible a mayusculas y tildes (ej. 'Quindio' o 'Quindío')."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lista de municipios obtenida exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Parametro de departamento vacio o invalido"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Departamento no encontrado en el catalogo nacional"
            )
    })
    @GetMapping("/municipios")
    public ResponseEntity<ApiResponse<List<String>>> listarMunicipiosPorDepartamento(
            @Parameter(description = "Nombre del departamento a consultar", example = "Quindío", required = true)
            @RequestParam("departamento") String departamento) {
        List<String> municipios = ubicacionCatalogoService.listarMunicipiosPorDepartamento(departamento);
        return ResponseEntity.ok(
                ApiResponse.success(municipios, "Municipios consultados exitosamente")
        );
    }

    @Operation(
            summary = "Obtener catalogo completo de departamentos y municipios",
            description = "Retorna el arbol completo de departamentos con sus listas de municipios para almacenamiento en cache local del cliente."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Catalogo completo obtenido exitosamente"
            )
    })
    @GetMapping("/catalogo")
    public ResponseEntity<ApiResponse<List<DepartamentoMunicipiosDto>>> obtenerCatalogoCompleto() {
        List<DepartamentoMunicipiosDto> catalogo = ubicacionCatalogoService.obtenerCatalogoCompleto();
        return ResponseEntity.ok(
                ApiResponse.success(catalogo, "Catalogo geografico completo obtenido exitosamente")
        );
    }
}
