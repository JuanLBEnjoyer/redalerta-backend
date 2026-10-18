package redalerta_backend.modules.catalogo.domain;

import redalerta_backend.modules.catalogo.dto.DepartamentoResponseDto;
import redalerta_backend.modules.catalogo.util.NormalizadorTexto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catalogo geografico canonico en memoria para departamentos y municipios piloto.
 * Mantiene la correspondencia oficial de nombres y permite canonicalizar entradas
 * de texto independientemente de acentos ortograficos o variaciones de mayusculas.
 */
public final class CatalogoGeografico {

    private static final Map<String, List<String>> DEPARTAMENTOS_MUNICIPIOS;
    private static final Map<String, String> INDICE_NORMALIZADO_DEPARTAMENTOS;
    private static final Map<String, Map<String, String>> INDICE_NORMALIZADO_MUNICIPIOS;

    static {
        Map<String, List<String>> map = new LinkedHashMap<>();

        // Quindio (los 12 municipios oficiales del departamento)
        map.put("Quindío", List.of(
                "Armenia",
                "Buenavista",
                "Calarcá",
                "Circasia",
                "Córdoba",
                "Filandia",
                "Génova",
                "La Tebaida",
                "Montenegro",
                "Pijao",
                "Quimbaya",
                "Salento"
        ));

        // Risaralda
        map.put("Risaralda", List.of(
                "Pereira",
                "Dosquebradas",
                "Santa Rosa de Cabal",
                "La Virginia",
                "Belén de Umbría",
                "Marsella",
                "Santuario"
        ));

        // Caldas
        map.put("Caldas", List.of(
                "Manizales",
                "Villamaría",
                "Chinchiná",
                "Neira",
                "Palestina",
                "Riosucio",
                "La Dorada"
        ));

        // Antioquia
        map.put("Antioquia", List.of(
                "Medellín",
                "Bello",
                "Envigado",
                "Itagüí",
                "Sabaneta",
                "Rionegro",
                "Apartadó"
        ));

        // Valle del Cauca
        map.put("Valle del Cauca", List.of(
                "Cali",
                "Palmira",
                "Cartago",
                "Buga",
                "Tuluá",
                "Buenaventura",
                "Jamundí"
        ));

        // Cundinamarca
        map.put("Cundinamarca", List.of(
                "Bogotá D.C.",
                "Soacha",
                "Chía",
                "Zipaquirá",
                "Facatativá",
                "Fusagasugá",
                "Girardot"
        ));

        DEPARTAMENTOS_MUNICIPIOS = Collections.unmodifiableMap(map);

        // Construir indices normalizados para busqueda agnostica a acentos O(1)
        Map<String, String> idxDeptos = new LinkedHashMap<>();
        Map<String, Map<String, String>> idxMunis = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String canonicoDepto = entry.getKey();
            String normDepto = NormalizadorTexto.normalizar(canonicoDepto);
            idxDeptos.put(normDepto, canonicoDepto);

            Map<String, String> munisMap = new LinkedHashMap<>();
            for (String muni : entry.getValue()) {
                munisMap.put(NormalizadorTexto.normalizar(muni), muni);
            }
            idxMunis.put(canonicoDepto, munisMap);
        }

        INDICE_NORMALIZADO_DEPARTAMENTOS = Collections.unmodifiableMap(idxDeptos);
        INDICE_NORMALIZADO_MUNICIPIOS = Collections.unmodifiableMap(idxMunis);
    }

    private CatalogoGeografico() {
    }

    /**
     * Retorna la lista oficial de todos los departamentos disponibles.
     */
    public static List<String> obtenerDepartamentos() {
        return List.copyOf(DEPARTAMENTOS_MUNICIPIOS.keySet());
    }

    /**
     * Retorna la lista de municipios para un departamento dado (busqueda agnostica a tildes).
     */
    public static List<String> obtenerMunicipios(String departamento) {
        if (departamento == null || departamento.isBlank()) {
            return Collections.emptyList();
        }
        String canonico = canonicalizarDepartamento(departamento);
        return DEPARTAMENTOS_MUNICIPIOS.getOrDefault(canonico, Collections.emptyList());
    }

    /**
     * Retorna el catalogo completo con todos los departamentos y sus municipios.
     */
    public static List<DepartamentoResponseDto> obtenerCatalogo() {
        return DEPARTAMENTOS_MUNICIPIOS.entrySet().stream()
                .map(e -> new DepartamentoResponseDto(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * Normaliza un nombre de departamento a su representacion canonica oficial.
     * Ejemplo: "quindio" -> "Quindío", "QUINDÍO" -> "Quindío".
     * Si no se encuentra en el catalogo piloto, retorna el texto original limpio.
     */
    public static String canonicalizarDepartamento(String departamento) {
        if (departamento == null) {
            return null;
        }
        String limpio = departamento.trim();
        String norm = NormalizadorTexto.normalizar(limpio);
        String canonico = INDICE_NORMALIZADO_DEPARTAMENTOS.get(norm);
        return canonico != null ? canonico : limpio;
    }

    /**
     * Normaliza un nombre de municipio a su representacion canonica oficial para un departamento dado.
     * Ejemplo: depto: "Quindio", muni: "calarca" -> "Calarcá".
     * Si no se encuentra en el catalogo piloto, retorna el texto original limpio.
     */
    public static String canonicalizarMunicipio(String departamento, String municipio) {
        if (municipio == null) {
            return null;
        }
        String limpioMuni = municipio.trim();
        if (departamento == null || departamento.isBlank()) {
            return limpioMuni;
        }
        String canonicoDepto = canonicalizarDepartamento(departamento);
        Map<String, String> munisMap = INDICE_NORMALIZADO_MUNICIPIOS.get(canonicoDepto);
        if (munisMap != null) {
            String normMuni = NormalizadorTexto.normalizar(limpioMuni);
            String canonicoMuni = munisMap.get(normMuni);
            if (canonicoMuni != null) {
                return canonicoMuni;
            }
        }
        return limpioMuni;
    }

    /**
     * Verifica si un departamento pertenece al catalogo oficial.
     */
    public static boolean esDepartamentoValido(String departamento) {
        if (departamento == null || departamento.isBlank()) {
            return false;
        }
        return INDICE_NORMALIZADO_DEPARTAMENTOS.containsKey(NormalizadorTexto.normalizar(departamento));
    }

    /**
     * Verifica si un municipio pertenece al catalogo oficial dentro de un departamento.
     */
    public static boolean esMunicipioValido(String departamento, String municipio) {
        if (departamento == null || municipio == null) {
            return false;
        }
        String canonicoDepto = canonicalizarDepartamento(departamento);
        Map<String, String> munisMap = INDICE_NORMALIZADO_MUNICIPIOS.get(canonicoDepto);
        if (munisMap == null) {
            return false;
        }
        return munisMap.containsKey(NormalizadorTexto.normalizar(municipio));
    }
}
