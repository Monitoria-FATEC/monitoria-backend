package com.fatec.monitoria.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<Cpf, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return valido(valor);
    }

    // Aceita "52998224725" ou "529.982.247-25"; qualquer outro caractere invalida.
    public static boolean valido(String valor) {
        if (valor == null || !valor.matches("^\\s*[\\d.\\-\\s]+$")) {
            return false;
        }
        String d = digitos(valor);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(d, 9) == d.charAt(9) - '0'
                && digitoVerificador(d, 10) == d.charAt(10) - '0';
    }

    // Forma gravada no banco: 000.000.000-00
    public static String formatar(String valor) {
        String d = digitos(valor);
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    public static String digitos(String valor) {
        return valor.replaceAll("\\D", "");
    }

    private static int digitoVerificador(String d, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (d.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}