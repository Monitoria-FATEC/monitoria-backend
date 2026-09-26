package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.dto.AtualizarStatusRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorCadastroRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitores")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService service;

    @PostMapping
    public ResponseEntity<MonitorResponse> cadastrar(@Valid @RequestBody MonitorCadastroRequest request) {
        Monitor monitor = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MonitorResponse.fromEntity(monitor));
    }

    @GetMapping
    public ResponseEntity<List<MonitorResponse>> listar() {
        List<MonitorResponse> lista = service.listarTodos().stream()
                .map(MonitorResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MonitorResponse> atualizarStatus(
            @PathVariable String id,
            @Valid @RequestBody AtualizarStatusRequest request) {
        Monitor monitor = service.atualizarStatus(id, request);
        return ResponseEntity.ok(MonitorResponse.fromEntity(monitor));
    }
}