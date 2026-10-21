package com.fatec.monitoria.modules.auth;

import com.fatec.monitoria.modules.usuario.dto.TrocarSenhaRequest;
import com.fatec.monitoria.modules.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conta")
@RequiredArgsConstructor
public class ContaController {

    private final AuthService authService;

    // Usuário logado com senha temporária define a própria senha.
    @PutMapping("/senha")
    public ResponseEntity<UsuarioResponse> trocarSenhaTemporaria(
            Authentication authentication,
            @Valid @RequestBody TrocarSenhaRequest request) {
        return ResponseEntity.ok(UsuarioResponse.fromEntity(
                authService.trocarSenhaTemporaria(authentication.getName(), request.getNovaSenha())));
    }
}