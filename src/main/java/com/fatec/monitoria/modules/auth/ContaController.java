package com.fatec.monitoria.modules.auth;

import com.fatec.monitoria.modules.usuario.dto.MensagemResponse;
import com.fatec.monitoria.modules.usuario.dto.TrocarSenhaRequest;
import com.fatec.monitoria.modules.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conta")
@RequiredArgsConstructor
public class ContaController {

    private final AuthService authService;

    // Passo 1 do primeiro acesso: envia o código de 6 dígitos para o e-mail do usuário logado.
    @PostMapping("/senha/codigo")
    public ResponseEntity<MensagemResponse> enviarCodigoTrocaSenha(Authentication authentication) {
        return ResponseEntity.ok(authService.enviarCodigoTrocaSenha(authentication.getName()));
    }

    // Passo 2: nova senha + código recebido por e-mail.
    @PutMapping("/senha")
    public ResponseEntity<UsuarioResponse> trocarSenhaTemporaria(
            Authentication authentication,
            @Valid @RequestBody TrocarSenhaRequest request) {
        return ResponseEntity.ok(UsuarioResponse.fromEntity(
                authService.trocarSenhaTemporaria(
                        authentication.getName(), request.getNovaSenha(), request.getCodigo())));
    }
}