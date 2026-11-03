package com.fatec.monitoria.modules.inscricao.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "inscricoes")
public class Inscricao {

    @Id
    private String id;

    private String idMonitor;
    private String idTermoCompromisso;

    private StatusInscricao status;

    // Supervisor que aprovou a ficha (o histórico guarda também quem devolveu e quem homologou)
    private String idSupervisor;
    // A Gestão assina fora do sistema, então este campo só é usado se um dia ela passar a ter acesso
    private String idGestao;

    private String justificativaDevolucao;
    // Prazo (opcional) que o supervisor dá para o aluno corrigir uma ficha devolvida
    private LocalDate dataLimiteCorrecao;
    private String assinaturaGestao;

    // O supervisor marca que enviou o termo à Gestão para assinatura; o status continua AGUARDANDO_GESTAO
    private boolean enviadoParaAssinatura;
    private LocalDateTime dataEnvioAssinatura;

    private LocalDateTime dataSubmissao;
    private LocalDateTime dataAtualizacao;

    // Registro de cada mudança de status, com autor e data (só cresce, nunca é apagado)
    private List<RegistroHistorico> historico = new ArrayList<>();

    public Inscricao() {
        this.status = StatusInscricao.AGUARDANDO_SUPERVISOR;
        this.dataSubmissao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
}