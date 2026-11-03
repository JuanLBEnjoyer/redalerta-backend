package redalerta_backend.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilidad para normalizar nombres geograficos (departamentos y municipios).
 * Elimina tildes diacriticas en vocales (conservando la letra eñe) y espacios redundantes
 * para garantizar que los datos provenientes de lecturas GPS (Geocoder) coincidan exactamente
 * con los datos seleccionados en formularios de registro y filtros de consulta.
 */
public final class TextoGeograficoUtil {

    private static final Pattern MARCAS_ACENTO_VOCALICO = Pattern.compile("[\\u0300-\\u0302\\u0304-\\u036f]+");
    private static final Pattern TODAS_LAS_MARCAS_DIACRITICAS = Pattern.compile("\\p{M}+");
    private static final Pattern ESPACIOS_MULTIPLES = Pattern.compile("\\s+");

    private TextoGeograficoUtil() {
    }

    /**
     * Normaliza un nombre geografico eliminando acentos en vocales (ej. "Quindío" -> "Quindio",
     * "Calarcá" -> "Calarca", "Bogotá, D.C." -> "Bogota, D.C.") y conservando la letra 'ñ'.
     *
     * @param valor Cadena de entrada con el nombre del departamento o municipio.
     * @return Cadena limpia y normalizada en formato canonico.
     */
    public static String normalizarNombreGeografico(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = ESPACIOS_MULTIPLES.matcher(valor.trim()).replaceAll(" ");
        String descompuesto = Normalizer.normalize(limpio, Normalizer.Form.NFD);
        String sinTildesVocalicas = MARCAS_ACENTO_VOCALICO.matcher(descompuesto).replaceAll("");
        return Normalizer.normalize(sinTildesVocalicas, Normalizer.Form.NFC);
    }

    /**
     * Genera una clave de comparacion insensible a mayusculas, minusculas y cualquier tilde o diacritico.
     *
     * @param valor Cadena a transformar en clave de busqueda.
     * @return Clave en minusculas y sin marcas diacriticas.
     */
    public static String claveComparacion(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = ESPACIOS_MULTIPLES.matcher(valor.trim()).replaceAll(" ");
        String descompuesto = Normalizer.normalize(limpio, Normalizer.Form.NFD);
        return TODAS_LAS_MARCAS_DIACRITICAS.matcher(descompuesto)
                .replaceAll("")
                .toLowerCase(Locale.ROOT);
    }
}

