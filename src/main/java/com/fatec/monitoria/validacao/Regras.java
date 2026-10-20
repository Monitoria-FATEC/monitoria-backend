package com.fatec.monitoria.validacao;

// Regras de validação compartilhadas pelos DTOs e pelos services.
// Os padrões aceitam espaços nas pontas porque os services fazem trim antes de gravar.
public final class Regras {

    private Regras() {
    }

    // pelo menos nome e sobrenome; só letras, espaço, apóstrofo, ponto e hífen
    public static final String NOME =
            "^\\s*\\p{L}[\\p{L}'’.-]*(\\s+\\p{L}[\\p{L}'’.-]*)+\\s*$";
    public static final String NOME_MSG = "Informe o nome completo (nome e sobrenome), apenas com letras";
    public static final int NOME_MAX = 100;

    // exige @, domínio com ponto e final com 2+ letras; sem '..' e sem ponto colado no @
    public static final String EMAIL =
            "^\\s*(?!\\.)(?!.*\\.\\.)[A-Za-z0-9._%+-]+(?<!\\.)@"
                    + "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
                    + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}\\s*$";
    public static final String EMAIL_MSG = "Informe um e-mail válido, como nome@dominio.com";
    public static final int EMAIL_MAX = 254;

    public static final String RA = "^\\s*\\d{13}\\s*$";
    public static final String RA_MSG = "O RA deve ter 13 dígitos numéricos";

    // 6 a 72 caracteres, sem espaços, com pelo menos uma letra e um número
    public static final String SENHA = "^(?=.*\\p{L})(?=.*\\d)\\S{6,72}$";
    public static final String SENHA_MSG =
            "A senha deve ter de 6 a 72 caracteres, sem espaços, com pelo menos uma letra e um número";

    public static String normalizarNome(String valor) {
        return valor == null ? null : valor.trim().replaceAll("\\s+", " ");
    }

    public static String normalizarEmail(String valor) {
        return valor == null ? null : valor.trim().toLowerCase();
    }

    public static String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }
}