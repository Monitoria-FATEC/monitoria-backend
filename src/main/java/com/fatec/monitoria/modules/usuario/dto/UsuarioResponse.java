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

    public static UsuarioResponse fromEntity(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setNome(u.getNome());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole());
        r.setAtivo(u.isAtivo());
        return r;
    }
}