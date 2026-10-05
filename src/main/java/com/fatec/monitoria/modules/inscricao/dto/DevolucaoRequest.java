package com.fatec.monitoria.modules.inscricao.dto;

import jakarta.validation.constraints.NotBlank;

public class DevolucaoRequest {

    @NotBlank
    private String justificativa;

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
}