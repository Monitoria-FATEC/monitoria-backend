package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.modules.monitor.dto.ContaAgenciaDevolucaoRequest;
import com.fatec.monitoria.modules.monitor.dto.ContaAgenciaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitores")
@RequiredArgsConstructor
public class ContaAgenciaSupervisorController {

    private final ContaAgenciaSupervisorService service;

    @GetMapping("/conta-agencia")
    public ResponseEntity<List<ContaAgenciaResponse>> listar(
            @RequestParam(required = false) StatusDocumento status) {
        return ResponseEntity.ok(service.listar(status));
    }

    @PatchMapping("/{id}/conta-agencia/aprovar")
    public ResponseEntity<ContaAgenciaResponse> aprovar(@PathVariable String id) {
        return ResponseEntity.ok(service.aprovar(id));
    }

    @PatchMapping("/{id}/conta-agencia/devolver")
    public ResponseEntity<ContaAgenciaResponse> devolver(
            @PathVariable String id,
            @Valid @RequestBody ContaAgenciaDevolucaoRequest request) {
        return ResponseEntity.ok(service.devolver(id, request.getJustificativa(), request.getDataLimite()));
    }
}