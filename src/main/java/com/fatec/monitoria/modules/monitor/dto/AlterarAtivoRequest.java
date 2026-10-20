package com.fatec.monitoria.modules.monitor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlterarAtivoRequest {

    @NotNull(message = "Informe se o perfil fica ativo ou inativo")
    private Boolean ativo;
}