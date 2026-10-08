package com.fatec.monitoria.modules.monitor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContaAgenciaRequest {
    @NotBlank private String numeroConta;
    @NotBlank private String agencia;
}
