package com.fatec.monitoria.modules.auth;

import com.fatec.monitoria.modules.usuario.dto.CadastroRequest;
import com.fatec.monitoria.modules.usuario.dto.DisponibilidadeRequest;
import com.fatec.monitoria.modules.usuario.dto.EsqueciSenhaRequest;
import com.fatec.monitoria.modules.usuario.dto.LoginRequest;
import com.fatec.monitoria.modules.usuario.dto.LoginResponse;
import com.fatec.monitoria.modules.usuario.dto.MensagemResponse;
import com.fatec.monitoria.modules.usuario.dto.RedefinirSenhaRequest;
import com.fatec.monitoria.modules.usuario.dto.UsuarioResponse;
import com.fatec.monitoria.modules.usuario.dto.VerificarCodigoRequest;
import com.fatec.monitoria.validacao.DisponibilidadeCadastroService;
import com.fatec.monitoria.validacao.Regras;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;
    private final VerificacaoEmailService verificacaoEmailService;
    private final DisponibilidadeCadastroService disponibilidade;

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroRequest request) {
        UsuarioResponse resposta = UsuarioResponse.fromEntity(authService.cadastrar(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    // Confere o código enviado no cadastro. Se estiver certo, libera a conta e devolve o token.
    @PostMapping("/verificar-email")
    public ResponseEntity<LoginResponse> verificarEmail(@Valid @RequestBody VerificarCodigoRequest request) {
        return ResponseEntity.ok(verificacaoEmailService.confirmar(request.getEmail(), request.getCodigo()));
    }

    // Pede um código novo (no máximo um a cada 30 segundos).
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<MensagemResponse> reenviarCodigo(@Valid @RequestBody EsqueciSenhaRequest request) {
        return ResponseEntity.ok(verificacaoEmailService.reenviar(request.getEmail()));
    }

    // O front chama antes de enviar o cadastro: 200 se e-mail (e RA, se enviado) estão livres,
    // 409 com os campos em uso caso contrário.
    @PostMapping("/disponibilidade")
    public ResponseEntity<MensagemResponse> disponibilidade(@Valid @RequestBody DisponibilidadeRequest request) {
        disponibilidade.exigirDisponiveis(
                Regras.normalizarEmail(request.getEmail()),
                Regras.normalizarTexto(request.getRa()),
                true);
        return ResponseEntity.ok(new MensagemResponse("Disponível"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<MensagemResponse> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        return ResponseEntity.ok(recuperacaoSenhaService.solicitar(request.getEmail()));
    }

    @PostMapping("/esqueci-senha/verificar")
    public ResponseEntity<MensagemResponse> verificarCodigo(@Valid @RequestBody VerificarCodigoRequest request) {
        return ResponseEntity.ok(recuperacaoSenhaService.verificar(request.getEmail(), request.getCodigo()));
    }

    @PostMapping("/esqueci-senha/redefinir")
    public ResponseEntity<MensagemResponse> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        return ResponseEntity.ok(recuperacaoSenhaService.redefinir(
                request.getEmail(), request.getCodigo(), request.getNovaSenha()));
    }
}