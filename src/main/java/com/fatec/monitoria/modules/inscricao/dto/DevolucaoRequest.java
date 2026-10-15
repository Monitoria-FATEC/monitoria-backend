package com.fatec.monitoria.modules.inscricao.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class DevolucaoRequest {

    @NotBlank
    private String justificativa;

    @FutureOrPresent(message = "A data limite não pode estar no passado")
    private LocalDate dataLimite;

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }

    public LocalDate getDataLimite() { return dataLimite; }
    public void setDataLimite(LocalDate dataLimite) { this.dataLimite = dataLimite; }
}