package com.fatec.monitoria.modules.inscricao.dto;

import jakarta.validation.constraints.NotBlank;

public class InscricaoRequest {

    @NotBlank
    private String idMonitor;

    @NotBlank
    private String idTermoCompromisso;

    public String getIdMonitor() { return idMonitor; }
    public void setIdMonitor(String idMonitor) { this.idMonitor = idMonitor; }

    public String getIdTermoCompromisso() { return idTermoCompromisso; }
    public void setIdTermoCompromisso(String idTermoCompromisso) { this.idTermoCompromisso = idTermoCompromisso; }
}