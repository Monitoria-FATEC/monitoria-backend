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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscricoes")
@RequiredArgsConstructor
public class InscricaoController {

    private final InscricaoService service;

    @PostMapping
    public ResponseEntity<InscricaoResponse> submeter(
            Authentication authentication, @Valid @RequestBody InscricaoRequest request) {
        Inscricao inscricao = service.submeter(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(InscricaoResponse.fromEntity(inscricao));
    }

    @GetMapping
    public ResponseEntity<List<InscricaoDetalheResponse>> listar(
            @RequestParam(required = false) StatusInscricao status) {
        return ResponseEntity.ok(service.listarDetalhado(status));
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<InscricaoResponse> aprovar(Authentication authentication, @PathVariable String id) {
        Inscricao inscricao = service.aprovar(id, authentication.getName());
        return ResponseEntity.ok(InscricaoResponse.fromEntity(inscricao));
    }

    @PatchMapping("/{id}/devolver")
    public ResponseEntity<InscricaoResponse> devolver(
            Authentication authentication,
            @PathVariable String id, @Valid @RequestBody DevolucaoRequest request) {
        Inscricao inscricao = service.devolver(id, request.getJustificativa(), request.getDataLimite(),
                authentication.getName());
        return ResponseEntity.ok(InscricaoResponse.fromEntity(inscricao));
    }

    @PatchMapping("/{id}/enviar-assinatura")
    public ResponseEntity<InscricaoResponse> enviarParaAssinatura(
            Authentication authentication, @PathVariable String id) {
        Inscricao inscricao = service.enviarParaAssinatura(id, authentication.getName());
        return ResponseEntity.ok(InscricaoResponse.fromEntity(inscricao));
    }

    @PatchMapping("/{id}/homologar")
    public ResponseEntity<InscricaoResponse> homologar(
            Authentication authentication,
            @PathVariable String id, @RequestBody(required = false) HomologacaoRequest request) {
        String assinatura = request != null ? request.getAssinaturaGestao() : null;
        Inscricao inscricao = service.homologar(id, assinatura, authentication.getName());
        return ResponseEntity.ok(InscricaoResponse.fromEntity(inscricao));
    }
}