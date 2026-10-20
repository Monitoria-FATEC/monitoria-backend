package com.fatec.monitoria.validacao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

// 409 que informa quais campos já estão em uso, para o front marcar o campo certo.
public class CampoDuplicadoException extends ResponseStatusException {

    private final Map<String, String> campos;

    public CampoDuplicadoException(Map<String, String> campos) {
        super(HttpStatus.CONFLICT, campos.values().iterator().next());
        this.campos = new LinkedHashMap<>(campos);
    }

    public CampoDuplicadoException(String campo, String mensagem) {
        this(Map.of(campo, mensagem));
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}