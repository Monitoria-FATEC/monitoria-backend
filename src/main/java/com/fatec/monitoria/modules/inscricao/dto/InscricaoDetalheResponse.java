package com.fatec.monitoria.modules.inscricao.dto;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.RegistroHistorico;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class InscricaoDetalheResponse {

    private String id;
    private StatusInscricao status;
    private String idSupervisor;
    private String justificativaDevolucao;
    private LocalDate dataLimiteCorrecao;
    private boolean enviadoParaAssinatura;
    private LocalDateTime dataEnvioAssinatura;
    private LocalDateTime dataSubmissao;
    private LocalDateTime dataAtualizacao;
    private List<RegistroHistorico> historico = new ArrayList<>();
    private MonitorInfo monitor;
    private TermoInfo termo;

    @Data
    public static class MonitorInfo {
        private String id;
        private String nome;
        private String ra;
        private String email;
        private String curso;
    }

    @Data
    public static class TermoInfo {
        private String id;
        private String nomeEstudante;
        private String ra;
        private String cpf;
        private String curso;
        private String disciplina;
        private String oferta;
        private Integer cargaHoraria;
        private LocalDate periodoInicio;
        private LocalDate periodoFim;
        private String editalNumero;
        private String nomeProfessor;
        private String nomeCoordenador;
        private String unidade;
        private String cidade;
        private LocalDate dataAssinatura;
        private Integer numeroVias;
        private LocalDateTime dataEnvio;
        private String assinaturaEstudante;
    }

    public static InscricaoDetalheResponse of(Inscricao i, Monitor m, TermoCompromisso t) {
        InscricaoDetalheResponse r = new InscricaoDetalheResponse();
        r.id = i.getId();
        r.status = i.getStatus();
        r.idSupervisor = i.getIdSupervisor();
        r.justificativaDevolucao = i.getJustificativaDevolucao();
        r.dataLimiteCorrecao = i.getDataLimiteCorrecao();
        r.enviadoParaAssinatura = i.isEnviadoParaAssinatura();
        r.dataEnvioAssinatura = i.getDataEnvioAssinatura();
        r.dataSubmissao = i.getDataSubmissao();
        r.dataAtualizacao = i.getDataAtualizacao();
        // inscrições antigas podem não ter histórico gravado
        r.historico = i.getHistorico() != null ? i.getHistorico() : new ArrayList<>();

        if (m != null) {
            MonitorInfo mi = new MonitorInfo();
            mi.id = m.getId();
            mi.nome = m.getNome();
            mi.ra = m.getRa();
            mi.email = m.getEmail();
            mi.curso = m.getCurso();
            r.monitor = mi;
        }

        if (t != null) {
            TermoInfo ti = new TermoInfo();
            ti.id = t.getId();
            ti.nomeEstudante = t.getNomeEstudante();
            ti.ra = t.getRa();
            ti.cpf = t.getCpf();
            ti.curso = t.getCurso();
            ti.disciplina = t.getDisciplina();
            ti.oferta = t.getOferta();
            ti.cargaHoraria = t.getCargaHoraria();
            ti.periodoInicio = t.getPeriodoInicio();
            ti.periodoFim = t.getPeriodoFim();
            ti.editalNumero = t.getEditalNumero();
            ti.nomeProfessor = t.getNomeProfessor();
            ti.nomeCoordenador = t.getNomeCoordenador();
            ti.unidade = t.getUnidade();
            ti.cidade = t.getCidade();
            ti.dataAssinatura = t.getDataAssinatura();
            ti.numeroVias = t.getNumeroVias();
            ti.dataEnvio = t.getDataEnvio();
            ti.assinaturaEstudante = t.getAssinaturaEstudante();
            r.termo = ti;
        }

        return r;
    }
}