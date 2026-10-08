package com.fatec.monitoria.modules.monitor.dto;

import lombok.Data;

@Data
public class PerfilMonitorRequest {
    private String telefone;
    private String localAtendimento;
    private String horariosAtendimento;
    private String linkWhatsapp;
    private String linkTeams;
}
