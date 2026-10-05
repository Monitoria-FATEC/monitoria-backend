package com.fatec.monitoria.modules.inscricao.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "inscricoes")
public class Inscricao {

    @Id
    private String id;

    private String idMonitor;
    private String idTermoCompromisso;

    private StatusInscricao status;

    private String idSupervisor;
    private String idGestao;

    private String justificativaDevolucao;
    private String assinaturaGestao;

    private LocalDateTime dataSubmissao;
    private LocalDateTime dataAtualizacao;

    public Inscricao() {
        this.status = StatusInscricao.AGUARDANDO_SUPERVISOR;
        this.dataSubmissao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
}