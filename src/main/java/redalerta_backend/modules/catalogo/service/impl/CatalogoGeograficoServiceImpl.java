package redalerta_backend.modules.catalogo.service.impl;

import org.springframework.stereotype.Service;
import redalerta_backend.modules.catalogo.domain.CatalogoGeografico;
import redalerta_backend.modules.catalogo.dto.DepartamentoResponseDto;
import redalerta_backend.modules.catalogo.service.CatalogoGeograficoService;

import java.util.List;

/**
 * Implementacion del servicio de catalogo geografico.
 * Delega en la estructura canónica en memoria para proveer respuestas de alta velocidad.
 */
@Service
public class CatalogoGeograficoServiceImpl implements CatalogoGeograficoService {

    @Override
    public List<DepartamentoResponseDto> obtenerCatalogoCompleto() {
        return CatalogoGeografico.obtenerCatalogo();
    }

    @Override
    public List<String> obtenerDepartamentos() {
        return CatalogoGeografico.obtenerDepartamentos();
    }

    @Override
    public List<String> obtenerMunicipiosPorDepartamento(String departamento) {
        return CatalogoGeografico.obtenerMunicipios(departamento);
    }

    @Override
    public String normalizarDepartamento(String departamento) {
        return CatalogoGeografico.canonicalizarDepartamento(departamento);
    }

    @Override
    public String normalizarMunicipio(String departamento, String municipio) {
        return CatalogoGeografico.canonicalizarMunicipio(departamento, municipio);
    }
}
