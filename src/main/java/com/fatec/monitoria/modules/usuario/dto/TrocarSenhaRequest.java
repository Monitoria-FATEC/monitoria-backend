package com.fatec.monitoria.modules.usuario.dto;

import com.fatec.monitoria.validacao.Regras;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TrocarSenhaRequest {

    @NotBlank(message = "A nova senha é obrigatória")
    @Pattern(regexp = Regras.SENHA, message = Regras.SENHA_MSG)
    private String novaSenha;

    // Código de 6 dígitos enviado por e-mail (POST /api/conta/senha/codigo).
    @NotBlank(message = "O código de confirmação é obrigatório")
    @Pattern(regexp = "\\d{6}", message = "O código tem 6 números")
    private String codigo;
}