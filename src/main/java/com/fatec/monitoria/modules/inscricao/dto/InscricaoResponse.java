package com.fatec.monitoria.modules.inscricao.dto;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;

import java.time.LocalDateTime;

public class InscricaoResponse {

    private String id;
    private String idMonitor;
    private String idTermoCompromisso;
    private StatusInscricao status;
    private String idSupervisor;
    private String idGestao;
    private String justificativaDevolucao;
    private LocalDateTime dataSubmissao;
    private LocalDateTime dataAtualizacao;

    public static InscricaoResponse fromEntity(Inscricao i) {
        InscricaoResponse r = new InscricaoResponse();
        r.id = i.getId();
        r.idMonitor = i.getIdMonitor();
        r.idTermoCompromisso = i.getIdTermoCompromisso();
        r.status = i.getStatus();
        r.idSupervisor = i.getIdSupervisor();
        r.idGestao = i.getIdGestao();
        r.justificativaDevolucao = i.getJustificativaDevolucao();
        r.dataSubmissao = i.getDataSubmissao();
        r.dataAtualizacao = i.getDataAtualizacao();
        return r;
    }

    public String getId() { return id; }
    public String getIdMonitor() { return idMonitor; }
    public String getIdTermoCompromisso() { return idTermoCompromisso; }
    public StatusInscricao getStatus() { return status; }
    public String getIdSupervisor() { return idSupervisor; }
    public String getIdGestao() { return idGestao; }
    public String getJustificativaDevolucao() { return justificativaDevolucao; }
    public LocalDateTime getDataSubmissao() { return dataSubmissao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}