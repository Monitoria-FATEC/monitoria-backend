package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.monitor.dto.AlterarAtivoRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorGestaoRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorGestaoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitores")
@RequiredArgsConstructor
public class MonitorGestaoController {

    private final MonitorGestaoService service;

    @GetMapping("/gestao")
    public ResponseEntity<List<MonitorGestaoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PutMapping("/{id}/gestao")
    public ResponseEntity<MonitorGestaoResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody MonitorGestaoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<MonitorGestaoResponse> alterarAtivo(
            @PathVariable String id,
            @Valid @RequestBody AlterarAtivoRequest request) {
        return ResponseEntity.ok(service.alterarAtivo(id, request.getAtivo()));
    }
}