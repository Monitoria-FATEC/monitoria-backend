package com.fatec.monitoria.modules.inscricao;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.inscricao.dto.DevolucaoRequest;
import com.fatec.monitoria.modules.inscricao.dto.HomologacaoRequest;
import com.fatec.monitoria.modules.inscricao.dto.InscricaoDetalheResponse;
import com.fatec.monitoria.modules.inscricao.dto.InscricaoRequest;
import com.fatec.monitoria.modules.inscricao.dto.InscricaoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscricoes")
@RequiredArgsConstructor
public class InscricaoController {

    private final InscricaoService service;

    @PostMapping
    public ResponseEntity<InscricaoResponse> submeter(@Valid @RequestBody InscricaoRequest request) {
        Inscricao inscricao = service.submeter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(InscricaoResponse.fromEntity(inscricao));
    }

    @GetMapping
    public ResponseEntity<List<InscricaoDetalheResponse>> listar(
            @RequestParam(required = false) StatusInscricao status) {
        return ResponseEntity.ok(service.listarDetalhado(status));
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<InscricaoResponse> aprovar(@PathVariable String id) {
        Inscricao inscricao = service.aprovar(id);
        return ResponseEntity.ok(InscricaoResponse.fromEntity(inscricao));
    }

    @PatchMapping("/{id}/devolver")
    public ResponseEntity<InscricaoResponse> devolver(
            @PathVariable String id, @Valid @RequestBody DevolucaoRequest request) {
        Inscricao inscricao = service.devolver(id, request.getJustificativa());
        return ResponseEntity.ok(InscricaoResponse.fromEntity(inscricao));
    }

    @PatchMapping("/{id}/homologar")
    public ResponseEntity<InscricaoResponse> homologar(
            @PathVariable String id, @Valid @RequestBody HomologacaoRequest request) {
        Inscricao inscricao = service.homologar(id, request.getAssinaturaGestao());
        return ResponseEntity.ok(InscricaoResponse.fromEntity(inscricao));
    }
}