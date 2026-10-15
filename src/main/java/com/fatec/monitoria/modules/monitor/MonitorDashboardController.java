package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.monitor.dto.ContaAgenciaRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorDashboardResponse;
import com.fatec.monitoria.modules.monitor.dto.PerfilMonitorRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitores/me")
@RequiredArgsConstructor
public class MonitorDashboardController {
    private final MonitorDashboardService service;

    @GetMapping("/dashboard")
    public ResponseEntity<MonitorDashboardResponse> dashboard(Authentication authentication) {
        return ResponseEntity.ok(service.buscar(authentication.getName()));
    }

    @PutMapping("/conta-agencia")
    public ResponseEntity<MonitorDashboardResponse> contaAgencia(
            Authentication authentication,
            @Valid @RequestBody ContaAgenciaRequest request) {
        return ResponseEntity.ok(service.salvarContaAgencia(authentication.getName(), request));
    }

    @PutMapping("/perfil")
    public ResponseEntity<MonitorDashboardResponse> perfil(
            Authentication authentication,
            @RequestBody PerfilMonitorRequest request) {
        return ResponseEntity.ok(service.atualizarPerfil(authentication.getName(), request));
    }

    @PatchMapping("/desativar")
    public ResponseEntity<Void> desativar(Authentication authentication) {
        service.desativarPerfil(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
