package redalerta_backend.modules.ubicacion.service;

import redalerta_backend.modules.ubicacion.dto.DepartamentoMunicipiosDto;

import java.util.List;

/**
 * Servicio para la consulta del catalogo geografico de departamentos y municipios de Colombia.
 */
public interface UbicacionCatalogoService {

    /**
     * Obtiene la lista ordenada de todos los departamentos de Colombia.
     *
     * @return Lista de nombres de departamentos.
     */
    List<String> listarDepartamentos();

    /**
     * Obtiene la lista ordenada de municipios pertenecientes a un departamento especifico.
     * La busqueda del departamento es insensible a mayusculas, minusculas y tildes.
     *
     * @param departamento Nombre del departamento a consultar (con o sin tilde).
     * @return Lista de municipios del departamento solicitado.
     */
    List<String> listarMunicipiosPorDepartamento(String departamento);

    /**
     * Obtiene el catalogo completo de departamentos junto con sus listas de municipios.
     *
     * @return Lista de DTOs con cada departamento y sus municipios.
     */
    List<DepartamentoMunicipiosDto> obtenerCatalogoCompleto();

    /**
     * Verifica si una combinacion de departamento y municipio existe dentro del catalogo oficial,
     * comparando de forma insensible a mayusculas y tildes.
     *
     * @param departamento Nombre del departamento.
     * @param municipio    Nombre del municipio.
     * @return true si el departamento y municipio son validos; false en caso contrario.
     */
    boolean esUbicacionValida(String departamento, String municipio);
}

