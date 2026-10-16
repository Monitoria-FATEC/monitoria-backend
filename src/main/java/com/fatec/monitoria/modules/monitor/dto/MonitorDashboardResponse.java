package com.fatec.monitoria.modules.monitor.dto;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
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

    // dados completos do termo (para o monitor corrigir quando devolvido)
    private String nomeEstudanteTermo;
    private String cpf;
    private String editalNumero;
    private String nomeProfessor;
    private String nomeCoordenador;
    private String unidade;
    private String cidade;
    private String dataAssinatura;
    private Integer numeroVias;
    private boolean temAssinatura;

    private StatusInscricao statusInscricao;
    private StatusDocumento statusTermo;
    private String justificativaTermo;
    // prazo que o supervisor deu para corrigir o termo (null se não deu)
    private LocalDate dataLimiteTermo;

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

    // inscricao é a mais recente do monitor (null se ele ainda não tem inscrição)
    public static MonitorDashboardResponse of(Monitor monitor, TermoCompromisso termo, Inscricao inscricao) {
        MonitorDashboardResponse response = new MonitorDashboardResponse();
        response.id = monitor.getId();
        response.nome = monitor.getNome();
        response.email = monitor.getEmail();
        response.curso = monitor.getCurso();
        response.ra = monitor.getRa();

        response.statusInscricao = inscricao == null ? null : inscricao.getStatus();
        response.dataLimiteTermo = inscricao == null ? null : inscricao.getDataLimiteCorrecao();

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

        if (termo != null) {
            response.nomeEstudanteTermo = termo.getNomeEstudante();
            response.cpf = termo.getCpf();
            response.editalNumero = termo.getEditalNumero();
            response.nomeProfessor = termo.getNomeProfessor();
            response.nomeCoordenador = termo.getNomeCoordenador();
            response.unidade = termo.getUnidade();
            response.cidade = termo.getCidade();
            response.dataAssinatura = termo.getDataAssinatura() == null ? null : termo.getDataAssinatura().toString();
            response.numeroVias = termo.getNumeroVias();
            response.temAssinatura = termo.getAssinaturaEstudante() != null && !termo.getAssinaturaEstudante().isBlank();
        }
        return response;
    }
}