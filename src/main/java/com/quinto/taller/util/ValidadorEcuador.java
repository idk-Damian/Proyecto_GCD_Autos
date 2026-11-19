package com.quinto.taller.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validaciones propias de Ecuador: cédula (algoritmo módulo 10) y placas vehiculares.
 */
public final class ValidadorEcuador {

    private static final Pattern PLACA_AUTO = Pattern.compile("^[A-Z]{3}-\\d{3,4}$");
    private static final Pattern PLACA_MOTO = Pattern.compile("^[A-Z]{2}\\d{3}[A-Z]$");

    private ValidadorEcuador() {}

    public static boolean cedulaValida(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) return false;
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (!((provincia >= 1 && provincia <= 24) || provincia == 30)) return false;
        if (cedula.charAt(2) - '0' >= 6) return false;

        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int d = cedula.charAt(i) - '0';
            if (i % 2 == 0) {           // posiciones impares (1,3,5...) se multiplican por 2
                d *= 2;
                if (d > 9) d -= 9;
            }
            suma += d;
        }
        int verificador = (10 - (suma % 10)) % 10;
        return verificador == cedula.charAt(9) - '0';
    }

    /** Normaliza la placa: mayúsculas, sin espacios y con guion (ABC1234 -> ABC-1234). */
    public static String normalizarPlaca(String placa) {
        if (placa == null) return null;
        String p = placa.trim().toUpperCase(Locale.ROOT).replace(" ", "");
        if (p.matches("^[A-Z]{3}\\d{3,4}$")) {
            p = p.substring(0, 3) + "-" + p.substring(3);
        }
        return p;
    }

    public static boolean placaValida(String placaNormalizada) {
        return placaNormalizada != null
                && (PLACA_AUTO.matcher(placaNormalizada).matches() || PLACA_MOTO.matcher(placaNormalizada).matches());
    }
}
