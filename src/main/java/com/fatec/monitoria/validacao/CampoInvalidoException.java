package com.fatec.monitoria.validacao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

// 400 com a lista de campos inválidos, para o front marcar cada campo.
public class CampoInvalidoException extends ResponseStatusException {

    private final Map<String, String> campos;

    public CampoInvalidoException(Map<String, String> campos) {
        super(HttpStatus.BAD_REQUEST, campos.values().iterator().next());
        this.campos = new LinkedHashMap<>(campos);
    }

    public CampoInvalidoException(String campo, String mensagem) {
        this(Map.of(campo, mensagem));
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}