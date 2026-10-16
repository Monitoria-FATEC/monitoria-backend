package com.fatec.monitoria.modules.monitor.dto;

import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import lombok.Data;

import java.time.LocalDate;

// Linha do painel do supervisor: dados do monitor + conta/agência enviadas.
@Data
public class ContaAgenciaResponse {

    private String idMonitor;
    private String nome;
    private String ra;
    private String email;
    private String curso;
    private String numeroConta;
    private String agencia;
    private StatusDocumento statusContaAgencia;
    private String justificativaContaAgencia;
    private LocalDate dataLimiteContaAgencia;

    public static ContaAgenciaResponse fromEntity(Monitor m) {
        ContaAgenciaResponse r = new ContaAgenciaResponse();
        r.idMonitor = m.getId();
        r.nome = m.getNome();
        r.ra = m.getRa();
        r.email = m.getEmail();
        r.curso = m.getCurso();
        r.numeroConta = m.getNumeroConta();
        r.agencia = m.getAgencia();
        r.statusContaAgencia = m.getStatusContaAgencia() == null
                ? StatusDocumento.AGUARDANDO : m.getStatusContaAgencia();
        r.justificativaContaAgencia = m.getJustificativaContaAgencia();
        r.dataLimiteContaAgencia = m.getDataLimiteContaAgencia();
        return r;
    }
}