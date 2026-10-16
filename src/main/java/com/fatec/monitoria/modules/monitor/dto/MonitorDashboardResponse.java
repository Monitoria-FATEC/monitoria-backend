package com.fatec.monitoria.modules.monitor.dto;

import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorDashboardResponse {
    private String id;
    private String nome;
    private String email;
    private String curso;
    private String ra;
    private String termoId;
    private String disciplina;
    private String oferta;
    private Integer cargaHoraria;
    private String periodoInicio;
    private String periodoFim;
    private String dataEnvioTermo;
    private StatusInscricao statusInscricao;
    private StatusDocumento statusTermo;
    private String justificativaTermo;
    private StatusDocumento statusContaAgencia;
    private String justificativaContaAgencia;
    private LocalDate dataLimiteContaAgencia;
    private String numeroConta;
    private String agencia;
    private String telefone;
    private String localAtendimento;
    private String horariosAtendimento;
    private String linkWhatsapp;
    private String linkTeams;
    private boolean perfilAtivo;

    // statusInscricao vem da inscrição mais recente do monitor (null se ainda não existe)
    public static MonitorDashboardResponse of(Monitor monitor, TermoCompromisso termo, StatusInscricao statusInscricao) {
        MonitorDashboardResponse response = new MonitorDashboardResponse();
        response.id = monitor.getId();
        response.nome = monitor.getNome();
        response.email = monitor.getEmail();
        response.curso = monitor.getCurso();
        response.ra = monitor.getRa();
        response.statusInscricao = statusInscricao;
        response.statusContaAgencia = monitor.getStatusContaAgencia() == null ? StatusDocumento.AGUARDANDO : monitor.getStatusContaAgencia();
        response.justificativaContaAgencia = monitor.getJustificativaContaAgencia();
        response.dataLimiteContaAgencia = monitor.getDataLimiteContaAgencia();
        response.numeroConta = monitor.getNumeroConta();
        response.agencia = monitor.getAgencia();
        response.telefone = monitor.getTelefone();
        response.localAtendimento = monitor.getLocalAtendimento();
        response.horariosAtendimento = monitor.getHorariosAtendimento();
        response.linkWhatsapp = monitor.getLinkWhatsapp();
        response.linkTeams = monitor.getLinkTeams();
        response.perfilAtivo = monitor.getPerfilAtivo() == null || monitor.getPerfilAtivo();
        response.statusTermo = termo == null || termo.getStatus() == null ? StatusDocumento.AGUARDANDO : termo.getStatus();
        response.justificativaTermo = termo == null ? null : termo.getJustificativaDevolucao();
        response.termoId = termo == null ? null : termo.getId();
        response.disciplina = termo == null ? null : termo.getDisciplina();
        response.oferta = termo == null ? null : termo.getOferta();
        response.cargaHoraria = termo == null ? null : termo.getCargaHoraria();
        response.periodoInicio = termo == null || termo.getPeriodoInicio() == null ? null : termo.getPeriodoInicio().toString();
        response.periodoFim = termo == null || termo.getPeriodoFim() == null ? null : termo.getPeriodoFim().toString();
        response.dataEnvioTermo = termo == null || termo.getDataEnvio() == null ? null : termo.getDataEnvio().toString();
        return response;
    }
}