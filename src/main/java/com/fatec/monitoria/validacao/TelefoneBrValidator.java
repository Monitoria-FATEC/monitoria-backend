package com.fatec.monitoria.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelefoneBrValidator implements ConstraintValidator<TelefoneBr, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return valido(valor);
    }

    // Aceita "11987654321", "(11) 98765-4321", "+55 11 98765-4321", fixo "(11) 2345-6789".
    public static boolean valido(String valor) {
        if (valor == null || !valor.matches("^\\s*[\\d\\s()+\\-]+$")) {
            return false;
        }
        String n = nacional(valor);
        if (n.length() != 10 && n.length() != 11) {
            return false;
        }
        // DDD: dois dígitos, sem zero
        if (n.charAt(0) == '0' || n.charAt(1) == '0') {
            return false;
        }
        // celular começa com 9; fixo começa com 2 a 5
        char primeiro = n.charAt(2);
        if (n.length() == 11 && primeiro != '9') {
            return false;
        }
        if (n.length() == 10 && (primeiro < '2' || primeiro > '5')) {
            return false;
        }
        // rejeita números como 11111111 ou 99999999999
        return n.substring(2).chars().distinct().count() > 1;
    }

    // Forma gravada no banco: (11) 98765-4321 ou (11) 2345-6789
    public static String formatar(String valor) {
        String n = nacional(valor);
        int corte = n.length() == 11 ? 7 : 6;
        return "(" + n.substring(0, 2) + ") " + n.substring(2, corte) + "-" + n.substring(corte);
    }

    // Só os dígitos, sem o 55 do país
    public static String nacional(String valor) {
        String d = valor.replaceAll("\\D", "");
        if ((d.length() == 12 || d.length() == 13) && d.startsWith("55")) {
            d = d.substring(2);
        }
        return d;
    }
}