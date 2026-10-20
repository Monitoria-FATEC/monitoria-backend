package com.fatec.monitoria.modules.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RedefinirSenhaRequest {

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "O código é obrigatório")
    @Pattern(regexp = "\\d{6}", message = "O código deve ter 6 dígitos")
    private String codigo;

    // mesma regra da tela: 6 a 72 caracteres, sem espaços, com letra e número
    @NotBlank(message = "A nova senha é obrigatória")
    @Pattern(
            regexp = "^(?=.*\\p{L})(?=.*\\d)\\S{6,72}$",
            message = "A senha deve ter de 6 a 72 caracteres, sem espaços, com pelo menos uma letra e um número")
    private String novaSenha;
}