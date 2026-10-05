package com.fatec.monitoria.modules.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tipo;
    private UsuarioResponse usuario;
}