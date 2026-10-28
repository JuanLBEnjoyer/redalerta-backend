package redalerta_backend.modules.catalogo.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utilidad para normalizacion de texto y comparacion agnostica a tildes, diacriticos y mayusculas.
 */
public final class NormalizadorTexto {

    private NormalizadorTexto() {
    }

    /**
     * Remueve tildes, marcas diacriticas, espacios en los extremos y convierte a minusculas.
     * Ejemplo: "Quindío  " -> "quindio"
     *
     * @param texto Cadena original.
     * @return Cadena normalizada en formato canonico de comparacion o null si la entrada es nula.
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        String descompuesto = Normalizer.normalize(limpio, Normalizer.Form.NFD);
        return descompuesto.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
