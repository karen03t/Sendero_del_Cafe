package co.edu.autonoma.turismo.domain;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Cobertura geografica del proyecto: unicamente municipios del Eje Cafetero
 * (Quindio, Risaralda y Caldas). Evita que se publiquen lugares fuera del
 * alcance y unifica el nombre canonico (mayusculas y tildes correctas).
 */
public final class CoberturaEjeCafetero {

    private static final Map<String, String> MUNICIPIOS = new LinkedHashMap<>();

    static {
        registrar("Quindío",
                "Armenia", "Buenavista", "Calarcá", "Circasia", "Córdoba",
                "Filandia", "Génova", "La Tebaida", "Montenegro", "Pijao",
                "Quimbaya", "Salento");

        registrar("Risaralda",
                "Pereira", "Apía", "Balboa", "Belén de Umbría", "Dosquebradas",
                "Guática", "La Celia", "La Virginia", "Marsella", "Mistrató",
                "Pueblo Rico", "Quinchía", "Santa Rosa de Cabal", "Santuario");

        registrar("Caldas",
                "Manizales", "Aguadas", "Anserma", "Aranzazu", "Belalcázar",
                "Chinchiná", "Filadelfia", "La Dorada", "La Merced", "Manzanares",
                "Marmato", "Marquetalia", "Neira", "Norcasia", "Pácora",
                "Palestina", "Pensilvania", "Riosucio", "Risaralda", "Salamina",
                "Samaná", "San José", "Supía", "Victoria", "Villamaría", "Viterbo");
    }

    private CoberturaEjeCafetero() {
    }

    private static void registrar(String departamento, String... municipios) {
        for (String municipio : municipios) {
            MUNICIPIOS.put(clave(municipio), municipio);
        }
    }

    /** Devuelve el nombre canonico si el municipio pertenece al Eje Cafetero. */
    public static Optional<String> canonico(String municipio) {

        if (municipio == null || municipio.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(MUNICIPIOS.get(clave(municipio)));
    }

    public static boolean cubre(String municipio) {
        return canonico(municipio).isPresent();
    }

    public static int totalMunicipios() {
        return MUNICIPIOS.size();
    }

    private static String clave(String valor) {
        String sinAcentos = Normalizer
                .normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return sinAcentos.toLowerCase(Locale.ROOT);
    }
}
