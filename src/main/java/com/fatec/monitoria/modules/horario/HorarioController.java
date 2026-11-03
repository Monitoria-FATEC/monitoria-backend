package com.fatec.monitoria.modules.horario;

import com.fatec.monitoria.modules.horario.dto.HorarioMonitorResponse;
import com.fatec.monitoria.modules.horario.dto.HorarioPublicoResponse;
import com.fatec.monitoria.modules.horario.dto.HorarioRequest;
import com.fatec.monitoria.modules.horario.dto.HorarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService service;

    // ----- monitor (coberto por /api/monitores/me/** -> ROLE_MONITOR) -----

    @GetMapping("/api/monitores/me/horarios")
    public ResponseEntity<List<HorarioResponse>> listarMeus(Authentication authentication) {
        return ResponseEntity.ok(service.listarMeus(authentication.getName()));
    }

    @PostMapping("/api/monitores/me/horarios")
    public ResponseEntity<HorarioResponse> criar(
            Authentication authentication,
            @Valid @RequestBody HorarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(authentication.getName(), request));
    }

    @PutMapping("/api/monitores/me/horarios/{id}")
    public ResponseEntity<HorarioResponse> atualizar(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody HorarioRequest request) {
        return ResponseEntity.ok(service.atualizar(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/monitores/me/horarios/{id}")
    public ResponseEntity<Void> remover(Authentication authentication, @PathVariable String id) {
        service.remover(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // ----- supervisor -----

    @GetMapping("/api/horarios")
    public ResponseEntity<List<HorarioMonitorResponse>> listarDeTodos() {
        return ResponseEntity.ok(service.listarDeTodos());
    }

    // ----- público: página "Encontrar monitoria" (sem login) -----

    @GetMapping("/api/horarios/publico")
    public ResponseEntity<List<HorarioPublicoResponse>> listarPublicos() {
        return ResponseEntity.ok(service.listarPublicos());
    }
}