package com.fatec.monitoria.modules.auth;

import com.fatec.monitoria.modules.usuario.domain.Usuario;
import com.fatec.monitoria.modules.usuario.domain.UsuarioRepository;
import com.fatec.monitoria.modules.usuario.dto.MensagemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

// Recuperação de senha em 3 passos: pedir o código, conferir o código, trocar a senha.
// Os logs [RECUPERACAO] vão só para o console e explicam por que um pedido não funcionou;
// a resposta para o front continua genérica de propósito.
@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private static final int VALIDADE_MINUTOS = 2;
    private static final int MAX_TENTATIVAS = 5;
    private static final int INTERVALO_MINIMO_SEGUNDOS = 30;

    // Mesma resposta exista o e-mail ou não, para não revelar quem tem conta.
    private static final String RESPOSTA_SOLICITACAO =
            "Se o e-mail estiver cadastrado, enviaremos um código de verificação.";
    private static final String CODIGO_INVALIDO = "Código inválido ou expirado";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Passo 1: gera um código de 6 dígitos e "envia" por e-mail.
    public MensagemResponse solicitar(String emailInformado) {
        String email = normalizar(emailInformado);
        Optional<Usuario> encontrado = usuarioRepository.findByEmailIgnoreCase(email);

        if (encontrado.isEmpty()) {
            log.warn("[RECUPERACAO] Pedido ignorado: não existe USUÁRIO com o e-mail '{}' "
                    + "(confira a coleção 'usuarios', não a 'monitores')", email);
            return new MensagemResponse(RESPOSTA_SOLICITACAO);
        }

        if (!encontrado.get().isAtivo()) {
            log.warn("[RECUPERACAO] Pedido ignorado: o usuário '{}' está inativo", email);
            return new MensagemResponse(RESPOSTA_SOLICITACAO);
        }

        Usuario usuario = encontrado.get();
        LocalDateTime agora = LocalDateTime.now();

        // pedidos em sequência (duplo clique) não geram um código novo
        LocalDateTime ultimoEnvio = usuario.getUltimoEnvioCodigoRecuperacao();
        if (ultimoEnvio != null && ultimoEnvio.plusSeconds(INTERVALO_MINIMO_SEGUNDOS).isAfter(agora)) {
            log.warn("[RECUPERACAO] Pedido ignorado para '{}': novo código só a cada {}s. "
                    + "Use o código da última linha [E-MAIL SIMULADO].", email, INTERVALO_MINIMO_SEGUNDOS);
            return new MensagemResponse(RESPOSTA_SOLICITACAO);
        }

        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));

        usuario.setCodigoRecuperacaoHash(passwordEncoder.encode(codigo));
        usuario.setExpiracaoCodigoRecuperacao(agora.plusMinutes(VALIDADE_MINUTOS));
        usuario.setTentativasCodigoRecuperacao(0);
        usuario.setUltimoEnvioCodigoRecuperacao(agora);
        usuarioRepository.save(usuario);

        emailService.enviarCodigoRecuperacao(usuario.getEmail(), usuario.getNome(), codigo, VALIDADE_MINUTOS);

        return new MensagemResponse(RESPOSTA_SOLICITACAO);
    }

    // Passo 2: só confere. Não gasta o código, porque o passo 3 manda ele de novo.
    public MensagemResponse verificar(String emailInformado, String codigo) {
        conferirCodigo(emailInformado, codigo);
        return new MensagemResponse("Código válido");
    }

    // Passo 3: confere de novo, troca a senha e invalida o código.
    public MensagemResponse redefinir(String emailInformado, String codigo, String novaSenha) {
        Usuario usuario = conferirCodigo(emailInformado, codigo);

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        limparCodigo(usuario);
        usuarioRepository.save(usuario);

        return new MensagemResponse("Senha redefinida com sucesso");
    }

    private Usuario conferirCodigo(String emailInformado, String codigo) {
        String email = normalizar(emailInformado);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("[RECUPERACAO] Código recusado: não existe usuário com o e-mail '{}'", email);
                    return codigoInvalido();
                });

        if (usuario.getCodigoRecuperacaoHash() == null || usuario.getExpiracaoCodigoRecuperacao() == null) {
            log.warn("[RECUPERACAO] Código recusado para '{}': não há código ativo "
                    + "(nunca foi pedido, já foi usado ou foi descartado)", email);
            throw codigoInvalido();
        }

        // expirado ou com tentativas esgotadas: o código é descartado
        if (usuario.getExpiracaoCodigoRecuperacao().isBefore(LocalDateTime.now())) {
            log.warn("[RECUPERACAO] Código recusado para '{}': EXPIRADO (venceu às {}). Peça um novo.",
                    email, usuario.getExpiracaoCodigoRecuperacao());
            limparCodigo(usuario);
            usuarioRepository.save(usuario);
            throw codigoInvalido();
        }

        if (usuario.getTentativasCodigoRecuperacao() >= MAX_TENTATIVAS) {
            log.warn("[RECUPERACAO] Código recusado para '{}': {} tentativas erradas, código descartado. Peça um novo.",
                    email, MAX_TENTATIVAS);
            limparCodigo(usuario);
            usuarioRepository.save(usuario);
            throw codigoInvalido();
        }

        if (!passwordEncoder.matches(codigo, usuario.getCodigoRecuperacaoHash())) {
            int tentativas = usuario.getTentativasCodigoRecuperacao() + 1;
            usuario.setTentativasCodigoRecuperacao(tentativas);
            usuarioRepository.save(usuario);
            log.warn("[RECUPERACAO] Código recusado para '{}': dígitos não conferem (tentativa {} de {})",
                    email, tentativas, MAX_TENTATIVAS);
            throw codigoInvalido();
        }

        return usuario;
    }

    private void limparCodigo(Usuario usuario) {
        usuario.setCodigoRecuperacaoHash(null);
        usuario.setExpiracaoCodigoRecuperacao(null);
        usuario.setTentativasCodigoRecuperacao(0);
        usuario.setUltimoEnvioCodigoRecuperacao(null);
    }

    private ResponseStatusException codigoInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, CODIGO_INVALIDO);
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}