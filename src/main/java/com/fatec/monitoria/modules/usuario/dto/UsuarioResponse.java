package com.fatec.monitoria.modules.usuario.dto;

import com.fatec.monitoria.modules.usuario.domain.Role;
import com.fatec.monitoria.modules.usuario.domain.Usuario;
import lombok.Data;

@Data
public class UsuarioResponse {

    private String id;
    private String nome;
    private String email;
    private Role role;
    private boolean ativo;
    private boolean emailPendenteVerificacao;
    // true: o front leva o usuário para criar a própria senha antes de usar o sistema
    private boolean senhaTemporaria;

    public static UsuarioResponse fromEntity(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setNome(u.getNome());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole());
        r.setAtivo(u.isAtivo());
        r.setEmailPendenteVerificacao(u.isEmailPendenteVerificacao());
        r.setSenhaTemporaria(u.isSenhaTemporaria());
        return r;
    }
}