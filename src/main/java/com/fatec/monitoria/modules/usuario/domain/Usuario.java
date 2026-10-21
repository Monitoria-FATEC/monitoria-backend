package com.fatec.monitoria.modules.usuario.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Document(collection = "usuarios")
public class Usuario implements UserDetails {

    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String email;

    private String senha;

    private Role role;

    private boolean ativo = true;

    // Senha gerada pelo supervisor (cadastro manual ou "nova senha"): o usuário é obrigado a
    // trocar no primeiro acesso. O padrão é false de propósito: contas antigas, que não têm
    // esse campo no banco, não são afetadas.
    private boolean senhaTemporaria = false;

    // Confirmação do e-mail no cadastro. O padrão é false de propósito: contas antigas,
    // que não têm esse campo no banco, continuam liberadas.
    private boolean emailPendenteVerificacao = false;
    private String codigoVerificacaoHash;
    private LocalDateTime expiracaoCodigoVerificacao;
    private int tentativasCodigoVerificacao;
    private LocalDateTime ultimoEnvioCodigoVerificacao;

    // Recuperação de senha por código enviado por e-mail (US-03).
    // O código nunca é guardado em texto: só o hash dele.
    private String codigoRecuperacaoHash;
    private LocalDateTime expiracaoCodigoRecuperacao;
    private int tentativasCodigoRecuperacao;
    private LocalDateTime ultimoEnvioCodigoRecuperacao;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    // ---- Métodos exigidos pelo UserDetails ----

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}