package com.fatec.monitoria.modules.monitor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MonitorCadastroRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "RA é obrigatório")
    private String ra;

    @NotBlank
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank
    private String curso;
}