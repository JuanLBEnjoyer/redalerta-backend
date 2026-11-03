package redalerta_backend.modules.ubicacion.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.util.TextoGeograficoUtil;
import redalerta_backend.modules.ubicacion.dto.DepartamentoMunicipiosDto;
import redalerta_backend.modules.ubicacion.service.UbicacionCatalogoService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion del servicio de catalogo geografico de Colombia.
 * Carga en memoria al iniciar la aplicacion el archivo de datos estructurado
 * {@code data/colombia-divipola.json} para responder consultas en tiempo constante O(1)
 * sin acoplar ni quemar valores territoriales en el codigo fuente.
 */
@Slf4j
@Service
public class UbicacionCatalogoServiceImpl implements UbicacionCatalogoService {

    private static final String RUTA_RECURSO_JSON = "data/colombia-divipola.json";

    private List<DepartamentoMunicipiosDto> catalogoCompleto = Collections.emptyList();
    private List<String> listaDepartamentos = Collections.emptyList();
    private final Map<String, DepartamentoMunicipiosDto> indicePorClaveDepartamento = new LinkedHashMap<>();

    @PostConstruct
    public void inicializarCatalogo() {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource recurso = new ClassPathResource(RUTA_RECURSO_JSON);

        try (InputStream inputStream = recurso.getInputStream()) {
            List<DepartamentoMunicipiosDto> datosCargados = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<DepartamentoMunicipiosDto>>() {
                    }
            );

            this.catalogoCompleto = List.copyOf(datosCargados);
            this.listaDepartamentos = this.catalogoCompleto.stream()
                    .map(DepartamentoMunicipiosDto::departamento)
                    .toList();

            this.indicePorClaveDepartamento.clear();
            for (DepartamentoMunicipiosDto item : this.catalogoCompleto) {
                String clave = TextoGeograficoUtil.claveComparacion(item.departamento());
                this.indicePorClaveDepartamento.put(clave, item);
            }

            log.info("Catalogo geografico DIVIPOLA cargado en memoria: {} departamentos disponibles.",
                    this.catalogoCompleto.size());
        } catch (IOException ex) {
            log.error("Error critico al leer el archivo de recursos geograficos {}: {}",
                    RUTA_RECURSO_JSON, ex.getMessage());
            throw new IllegalStateException("No fue posible inicializar el catalogo de ubicaciones de Colombia", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listarDepartamentos() {
        return this.listaDepartamentos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listarMunicipiosPorDepartamento(String departamento) {
        if (departamento == null || departamento.isBlank()) {
            throw new BadRequestException("Debe especificar el nombre del departamento a consultar");
        }

        String claveBusqueda = TextoGeograficoUtil.claveComparacion(departamento);
        DepartamentoMunicipiosDto encontrado = this.indicePorClaveDepartamento.get(claveBusqueda);

        if (encontrado == null) {
            throw new ResourceNotFoundException(
                    "No se encontro el departamento '" + departamento.trim() + "' en el catalogo nacional"
            );
        }

        return encontrado.municipios();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DepartamentoMunicipiosDto> obtenerCatalogoCompleto() {
        return this.catalogoCompleto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean esUbicacionValida(String departamento, String municipio) {
        if (departamento == null || departamento.isBlank() || municipio == null || municipio.isBlank()) {
            return false;
        }

        String claveDepto = TextoGeograficoUtil.claveComparacion(departamento);
        DepartamentoMunicipiosDto deptoDto = this.indicePorClaveDepartamento.get(claveDepto);
        if (deptoDto == null) {
            return false;
        }

        String claveMuni = TextoGeograficoUtil.claveComparacion(municipio);
        return deptoDto.municipios().stream()
                .anyMatch(m -> TextoGeograficoUtil.claveComparacion(m).equals(claveMuni));
    }
}

