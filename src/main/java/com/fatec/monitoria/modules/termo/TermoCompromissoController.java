package com.fatec.monitoria.modules.termo;

import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.dto.AtualizarTermoRequest;
import com.fatec.monitoria.modules.termo.dto.TermoCompromissoRequest;
import com.fatec.monitoria.modules.termo.dto.TermoCompromissoResponse;
import com.fatec.monitoria.validacao.DonoDoCadastro;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/termos-compromisso")
@RequiredArgsConstructor
public class TermoCompromissoController {

    private final TermoCompromissoService service;
    private final DonoDoCadastro donoDoCadastro;

    @PostMapping
    public ResponseEntity<TermoCompromissoResponse> enviar(
            Authentication authentication,
            @Valid @RequestBody TermoCompromissoRequest request) {
        // o termo tem de ser do RA do monitor logado
        donoDoCadastro.exigirRaDoMonitor(authentication.getName(), request.getRa());

        TermoCompromisso termo = service.enviar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TermoCompromissoResponse.fromEntity(termo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TermoCompromissoResponse> atualizar(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody AtualizarTermoRequest request) {
        TermoCompromisso termo = service.atualizar(authentication.getName(), id, request);
        return ResponseEntity.ok(TermoCompromissoResponse.fromEntity(termo));
    }
}