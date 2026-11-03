package com.fatec.monitoria.modules.atendimento;

import com.fatec.monitoria.modules.atendimento.dto.AtendimentoRequest;
import com.fatec.monitoria.modules.atendimento.dto.AtendimentoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Rotas do monitor (cobertas por /api/monitores/me/** -> ROLE_MONITOR no SecurityConfig).
@RestController
@RequestMapping("/api/monitores/me/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController {

    private final AtendimentoService service;

    @GetMapping
    public ResponseEntity<List<AtendimentoResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(service.listar(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<AtendimentoResponse> criar(
            Authentication authentication,
            @Valid @RequestBody AtendimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtendimentoResponse> atualizar(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody AtendimentoRequest request) {
        return ResponseEntity.ok(service.atualizar(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(Authentication authentication, @PathVariable String id) {
        service.excluir(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}