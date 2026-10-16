package com.fatec.monitoria.modules.monitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ContaAgenciaRequest {

    @NotBlank(message = "Informe o número da conta")
    @Pattern(regexp = "^\\d{4,12}(-[0-9Xx])?$",
            message = "Conta inválida: use de 4 a 12 dígitos, com dígito verificador opcional (ex.: 12345-6)")
    private String numeroConta;

    @NotBlank(message = "Informe a agência")
    @Pattern(regexp = "^\\d{3,5}(-[0-9Xx])?$",
            message = "Agência inválida: use de 3 a 5 dígitos, com dígito verificador opcional (ex.: 1234 ou 1234-5)")
    private String agencia;
}