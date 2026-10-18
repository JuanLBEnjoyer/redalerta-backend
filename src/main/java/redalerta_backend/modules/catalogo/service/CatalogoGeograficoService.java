package redalerta_backend.modules.catalogo.service;

import redalerta_backend.modules.catalogo.dto.DepartamentoResponseDto;

import java.util.List;

/**
 * Contrato de servicio para la consulta y canonicalizacion del catalogo geografico.
 */
public interface CatalogoGeograficoService {

    /**
     * Retorna todos los departamentos y sus municipios asociados.
     */
    List<DepartamentoResponseDto> obtenerCatalogoCompleto();

    /**
     * Retorna la lista de nombres de departamentos disponibles.
     */
    List<String> obtenerDepartamentos();

    /**
     * Retorna los municipios correspondientes a un departamento especifico.
     *
     * @param departamento Nombre del departamento (busqueda agnostica a tildes).
     * @return Lista de municipios asociados o lista vacia si no existe.
     */
    List<String> obtenerMunicipiosPorDepartamento(String departamento);

    /**
     * Normaliza un departamento a su nombre canonico oficial.
     *
     * @param departamento Cadena de entrada.
     * @return Nombre canonico con tildes y mayusculas oficiales.
     */
    String normalizarDepartamento(String departamento);

    /**
     * Normaliza un municipio dentro de un departamento a su nombre canonico oficial.
     *
     * @param departamento Nombre del departamento.
     * @param municipio    Cadena de entrada del municipio.
     * @return Nombre canonico con tildes y mayusculas oficiales.
     */
    String normalizarMunicipio(String departamento, String municipio);
}
