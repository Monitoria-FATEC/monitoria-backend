package com.fatec.monitoria.modules.auth;

import com.fatec.monitoria.modules.usuario.domain.Usuario;
import com.fatec.monitoria.modules.usuario.domain.UsuarioRepository;
import com.fatec.monitoria.modules.usuario.dto.LoginResponse;
import com.fatec.monitoria.modules.usuario.dto.MensagemResponse;
import com.fatec.monitoria.modules.usuario.dto.UsuarioResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

// Confirmação do e-mail no cadastro: envia o código, confere e libera a conta.
// Os logs [VERIFICACAO] vão só para o console e explicam por que um código foi recusado.
@Slf4j
@Service
public class VerificacaoEmailService {

    private static final int MAX_TENTATIVAS = 5;
    private static final int INTERVALO_MINIMO_SEGUNDOS = 30;
    private static final String CODIGO_INVALIDO = "Código inválido ou expirado";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final boolean exigida;
    private final int validadeMinutos;

    public VerificacaoEmailService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtService jwtService,
            @Value("${app.cadastro.verificar-email:false}") boolean exigida,
            @Value("${app.verificacao.validade-minutos:10}") int validadeMinutos) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.exigida = exigida;
        this.validadeMinutos = validadeMinutos;
    }

    // Liga/desliga pelo application.yml (app.cadastro.verificar-email)
    public boolean isExigida() {
        return exigida;
    }

    // Gera um código novo e envia. Pedidos em sequência (duplo clique) não geram código novo.
    public void enviarCodigo(Usuario usuario) {
        LocalDateTime agora = LocalDateTime.now();

        LocalDateTime ultimo = usuario.getUltimoEnvioCodigoVerificacao();
        if (ultimo != null && ultimo.plusSeconds(INTERVALO_MINIMO_SEGUNDOS).isAfter(agora)) {
            log.warn("[VERIFICACAO] Envio ignorado para '{}': novo código só a cada {}s. "
                    + "Use o último código enviado.", usuario.getEmail(), INTERVALO_MINIMO_SEGUNDOS);
            return;
        }

        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));

        usuario.setCodigoVerificacaoHash(passwordEncoder.encode(codigo));
        usuario.setExpiracaoCodigoVerificacao(agora.plusMinutes(validadeMinutos));
        usuario.setTentativasCodigoVerificacao(0);
        usuario.setUltimoEnvioCodigoVerificacao(agora);
        usuarioRepository.save(usuario);

        emailService.enviarCodigoVerificacao(usuario.getEmail(), usuario.getNome(), codigo, validadeMinutos);
    }

    // Confere o código, libera a conta e já devolve o token para o aluno seguir sem novo login.
    public LoginResponse confirmar(String emailInformado, String codigo) {
        String email = normalizar(emailInformado);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("[VERIFICACAO] Código recusado: não existe usuário com o e-mail '{}'", email);
                    return codigoInvalido();
                });

        if (!usuario.isEmailPendenteVerificacao()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este e-mail já foi confirmado. Faça login.");
        }

        if (usuario.getCodigoVerificacaoHash() == null || usuario.getExpiracaoCodigoVerificacao() == null) {
            log.warn("[VERIFICACAO] Código recusado para '{}': não há código ativo. Peça um novo.", email);
            throw codigoInvalido();
        }

        if (usuario.getExpiracaoCodigoVerificacao().isBefore(LocalDateTime.now())) {
            log.warn("[VERIFICACAO] Código recusado para '{}': EXPIRADO (venceu às {}). Peça um novo.",
                    email, usuario.getExpiracaoCodigoVerificacao());
            limparCodigo(usuario);
            usuarioRepository.save(usuario);
            throw codigoInvalido();
        }

        if (usuario.getTentativasCodigoVerificacao() >= MAX_TENTATIVAS) {
            log.warn("[VERIFICACAO] Código recusado para '{}': {} tentativas erradas, código descartado. Peça um novo.",
                    email, MAX_TENTATIVAS);
            limparCodigo(usuario);
            usuarioRepository.save(usuario);
            throw codigoInvalido();
        }

        if (!passwordEncoder.matches(codigo, usuario.getCodigoVerificacaoHash())) {
            int tentativas = usuario.getTentativasCodigoVerificacao() + 1;
            usuario.setTentativasCodigoVerificacao(tentativas);
            usuarioRepository.save(usuario);
            log.warn("[VERIFICACAO] Código recusado para '{}': dígitos não conferem (tentativa {} de {})",
                    email, tentativas, MAX_TENTATIVAS);
            throw codigoInvalido();
        }

        usuario.setEmailPendenteVerificacao(false);
        limparCodigo(usuario);
        usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario);
        return new LoginResponse(token, "Bearer", UsuarioResponse.fromEntity(usuario));
    }

    // Reenvio: a resposta é sempre a mesma, exista ou não um cadastro pendente.
    public MensagemResponse reenviar(String emailInformado) {
        String email = normalizar(emailInformado);
        Optional<Usuario> encontrado = usuarioRepository.findByEmailIgnoreCase(email);

        if (encontrado.isPresent() && encontrado.get().isEmailPendenteVerificacao() && encontrado.get().isAtivo()) {
            enviarCodigo(encontrado.get());
        } else {
            log.warn("[VERIFICACAO] Reenvio ignorado: não há cadastro pendente para '{}'", email);
        }

        return new MensagemResponse(
                "Se houver um cadastro aguardando confirmação para este e-mail, enviaremos um novo código.");
    }

    private void limparCodigo(Usuario usuario) {
        usuario.setCodigoVerificacaoHash(null);
        usuario.setExpiracaoCodigoVerificacao(null);
        usuario.setTentativasCodigoVerificacao(0);
        usuario.setUltimoEnvioCodigoVerificacao(null);
    }

    private ResponseStatusException codigoInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, CODIGO_INVALIDO);
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}