package com.fatec.monitoria.modules.monitor.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContaAgenciaDevolucaoRequest {

    @NotBlank(message = "Informe o motivo da devolução")
    private String justificativa;

    // opcional
    @FutureOrPresent(message = "A data limite não pode estar no passado")
    private LocalDate dataLimite;
}