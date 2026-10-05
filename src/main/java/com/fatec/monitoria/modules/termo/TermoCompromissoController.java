package com.fatec.monitoria.modules.termo;

import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.dto.TermoCompromissoRequest;
import com.fatec.monitoria.modules.termo.dto.TermoCompromissoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/termos-compromisso")
@RequiredArgsConstructor
public class TermoCompromissoController {

    private final TermoCompromissoService service;

    @PostMapping
    public ResponseEntity<TermoCompromissoResponse> enviar(@Valid @RequestBody TermoCompromissoRequest request) {
        TermoCompromisso termo = service.enviar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TermoCompromissoResponse.fromEntity(termo));
    }
}