package com.fatec.monitoria.modules.monitor.dto;

import com.fatec.monitoria.modules.monitor.domain.StatusInscricao;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarStatusRequest {

    @NotNull(message = "Status é obrigatório")
    private StatusInscricao status;
}