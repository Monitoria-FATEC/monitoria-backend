package com.fatec.monitoria.modules.usuario.dto;

import com.fatec.monitoria.validacao.Regras;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Usado pelo front para avisar, antes de enviar o cadastro, se o e-mail ou o RA já estão em uso.
@Data
public class DisponibilidadeRequest {

    @NotBlank(message = "O e-mail é obrigatório")
    @Size(max = Regras.EMAIL_MAX, message = "O e-mail deve ter no máximo 254 caracteres")
    @Pattern(regexp = Regras.EMAIL, message = Regras.EMAIL_MSG)
    private String email;

    // opcional: quando enviado, também confere se o RA já foi usado
    @Pattern(regexp = "^(\\s*\\d{13}\\s*)?$", message = Regras.RA_MSG)
    private String ra;
}