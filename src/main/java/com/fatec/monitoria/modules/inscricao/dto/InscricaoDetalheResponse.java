package com.fatec.monitoria.modules.inscricao.dto;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InscricaoDetalheResponse {

    private String id;
    private StatusInscricao status;
    private String justificativaDevolucao;
    private LocalDateTime dataSubmissao;
    private LocalDateTime dataAtualizacao;

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
        private String disciplina;
        private LocalDate periodoInicio;
        private LocalDate periodoFim;
        private LocalDate dataAssinatura;
        private LocalDateTime dataEnvio;
        private String assinaturaEstudante;
    }

    // monitor e termo podem ser null (ex.: inscrição de teste com id inválido)
    public static InscricaoDetalheResponse of(Inscricao i, Monitor m, TermoCompromisso t) {
        InscricaoDetalheResponse r = new InscricaoDetalheResponse();
        r.setId(i.getId());
        r.setStatus(i.getStatus());
        r.setJustificativaDevolucao(i.getJustificativaDevolucao());
        r.setDataSubmissao(i.getDataSubmissao());
        r.setDataAtualizacao(i.getDataAtualizacao());

        if (m != null) {
            MonitorInfo mi = new MonitorInfo();
            mi.setId(m.getId());
            mi.setNome(m.getNome());
            mi.setRa(m.getRa());
            mi.setEmail(m.getEmail());
            mi.setCurso(m.getCurso());
            r.setMonitor(mi);
        }

        if (t != null) {
            TermoInfo ti = new TermoInfo();
            ti.setId(t.getId());
            ti.setDisciplina(t.getDisciplina());
            ti.setPeriodoInicio(t.getPeriodoInicio());
            ti.setPeriodoFim(t.getPeriodoFim());
            ti.setDataAssinatura(t.getDataAssinatura());
            ti.setDataEnvio(t.getDataEnvio());
            ti.setAssinaturaEstudante(t.getAssinaturaEstudante());
            r.setTermo(ti);
        }

        return r;
    }
}