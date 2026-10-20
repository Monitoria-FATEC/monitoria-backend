package com.fatec.monitoria.modules.usuario.dto;

import com.fatec.monitoria.validacao.Regras;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CadastroRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = Regras.NOME_MAX, message = "O nome deve ter no máximo 100 caracteres")
    @Pattern(regexp = Regras.NOME, message = Regras.NOME_MSG)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Size(max = Regras.EMAIL_MAX, message = "O e-mail deve ter no máximo 254 caracteres")
    @Pattern(regexp = Regras.EMAIL, message = Regras.EMAIL_MSG)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Pattern(regexp = Regras.SENHA, message = Regras.SENHA_MSG)
    private String senha;
}