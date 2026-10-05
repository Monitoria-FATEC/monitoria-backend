package com.fatec.monitoria.modules.auth;

import com.fatec.monitoria.modules.usuario.domain.Role;
import com.fatec.monitoria.modules.usuario.domain.Usuario;
import com.fatec.monitoria.modules.usuario.domain.UsuarioRepository;
import com.fatec.monitoria.modules.usuario.dto.CadastroRequest;
import com.fatec.monitoria.modules.usuario.dto.LoginRequest;
import com.fatec.monitoria.modules.usuario.dto.LoginResponse;
import com.fatec.monitoria.modules.usuario.dto.UsuarioResponse;
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

    public Usuario cadastrar(CadastroRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setRole(Role.MONITOR);

        return usuarioRepository.save(usuario);
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

        String token = jwtService.gerarToken(usuario);
        return new LoginResponse(token, "Bearer", UsuarioResponse.fromEntity(usuario));
    }
}