package com.fatec.monitoria.modules.auth;

import com.fatec.monitoria.modules.usuario.domain.Role;
import com.fatec.monitoria.modules.usuario.domain.Usuario;
import com.fatec.monitoria.modules.usuario.domain.UsuarioRepository;
import com.fatec.monitoria.modules.usuario.dto.CadastroRequest;
import com.fatec.monitoria.modules.usuario.dto.LoginRequest;
import com.fatec.monitoria.modules.usuario.dto.LoginResponse;
import com.fatec.monitoria.modules.usuario.dto.MensagemResponse;
import com.fatec.monitoria.modules.usuario.dto.UsuarioResponse;
import com.fatec.monitoria.validacao.DisponibilidadeCadastroService;
import com.fatec.monitoria.validacao.Regras;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DisponibilidadeCadastroService disponibilidade;
    private final VerificacaoEmailService verificacaoEmail;
    private final RecuperacaoSenhaService recuperacaoSenha;

    public Usuario cadastrar(CadastroRequest request) {
        String email = Regras.normalizarEmail(request.getEmail());

        // e-mail não pode existir como usuário confirmado nem como monitor
        disponibilidade.exigirDisponiveis(email, null, true);

        // se já existe uma conta ainda não confirmada com esse e-mail, ela é refeita com os dados novos
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .filter(Usuario::isEmailPendenteVerificacao)
                .orElseGet(Usuario::new);

        boolean exigirConfirmacao = verificacaoEmail.isExigida();

        usuario.setNome(Regras.normalizarNome(request.getNome()));
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setRole(Role.MONITOR);
        usuario.setEmailPendenteVerificacao(exigirConfirmacao);

        usuario = usuarioRepository.save(usuario);

        if (exigirConfirmacao) {
            verificacaoEmail.enviarCodigo(usuario);
        }
        return usuario;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getSenha()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));

        // senha certa, mas o e-mail ainda não foi confirmado
        if (usuario.isEmailPendenteVerificacao()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Confirme seu e-mail para entrar. Enviamos um código de verificação para o seu e-mail.");
        }

        String token = jwtService.gerarToken(usuario);
        return new LoginResponse(token, "Bearer", UsuarioResponse.fromEntity(usuario));
    }

    // Primeiro acesso, passo 1: confere que a senha ainda é temporária e envia o código por e-mail.
    public MensagemResponse enviarCodigoTrocaSenha(String email) {
        exigirSenhaTemporaria(buscarPorEmail(email));
        recuperacaoSenha.solicitar(email);
        return new MensagemResponse("Enviamos um código de 6 dígitos para o seu e-mail.");
    }

    // Primeiro acesso, passo 2: com o código certo, o usuário cria a própria senha.
    public Usuario trocarSenhaTemporaria(String email, String novaSenha, String codigo) {
        Usuario usuario = buscarPorEmail(email);
        exigirSenhaTemporaria(usuario);

        if (passwordEncoder.matches(novaSenha, usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A nova senha precisa ser diferente da senha temporária.");
        }

        // confere o código, grava a nova senha e tira a marca de senha temporária
        recuperacaoSenha.redefinir(email, codigo, novaSenha);
        return buscarPorEmail(email);
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    private void exigirSenhaTemporaria(Usuario usuario) {
        if (!usuario.isSenhaTemporaria()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Sua senha não é temporária. Para trocá-la, use \"Esqueci minha senha\" na tela de entrar.");
        }
    }
}