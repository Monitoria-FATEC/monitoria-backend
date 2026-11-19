package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.monitor.dto.LimpezaDadosResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/limpeza/orfaos")
@RequiredArgsConstructor
public class LimpezaDadosController {

    private final LimpezaDadosService service;

    // O que seria apagado (não apaga nada).
    @GetMapping
    public ResponseEntity<LimpezaDadosResponse> simular() {
        return ResponseEntity.ok(service.simular());
    }

    // Apaga as inscrições sem monitor e os termos sem dono.
    @DeleteMapping
    public ResponseEntity<LimpezaDadosResponse> executar(Authentication authentication) {
        return ResponseEntity.ok(service.executar(authentication.getName()));
    }
}