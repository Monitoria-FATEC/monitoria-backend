package com.fatec.monitoria.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404, 403, 409... lançados nos services com ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> tratarStatus(ResponseStatusException ex) {
        String mensagem = ex.getReason() != null ? ex.getReason() : "Não foi possível concluir a operação";
        return resposta(HttpStatus.valueOf(ex.getStatusCode().value()), mensagem, null);
    }

    // Falhas de @Valid: devolve a primeira mensagem e a lista de campos com erro
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        String mensagem = campos.values().stream().findFirst().orElse("Dados inválidos");
        return resposta(HttpStatus.BAD_REQUEST, mensagem, campos);
    }

    // JSON malformado ou data em formato errado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Dados enviados em formato inválido", null);
    }

    // Login com e-mail ou senha errados
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> tratarCredenciais(BadCredentialsException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos", null);
    }

    // Usuário com perfil desativado
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> tratarDesativado(DisabledException ex) {
        return resposta(HttpStatus.FORBIDDEN, "Perfil desativado. Procure a supervisão.", null);
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem, Map<String, String> campos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", status.value());
        corpo.put("message", mensagem);
        if (campos != null) {
            corpo.put("campos", campos);
        }
        return ResponseEntity.status(status).body(corpo);
    }
}