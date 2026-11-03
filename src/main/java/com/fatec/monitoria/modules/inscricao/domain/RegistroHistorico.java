package com.fatec.monitoria.modules.inscricao.domain;

import java.time.LocalDateTime;

// Uma linha do histórico da inscrição: quem mudou o quê, quando e por quê.
// Fica guardada dentro da própria inscrição e nunca é apagada.
public class RegistroHistorico {

    private String acao;
    private StatusInscricao statusAnterior;
    private StatusInscricao statusNovo;
    private String idUsuario;
    private String nomeUsuario;
    private String perfil;
    private String justificativa;
    private LocalDateTime data;

    public RegistroHistorico() {
    }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public StatusInscricao getStatusAnterior() { return statusAnterior; }
    public void setStatusAnterior(StatusInscricao statusAnterior) { this.statusAnterior = statusAnterior; }

    public StatusInscricao getStatusNovo() { return statusNovo; }
    public void setStatusNovo(StatusInscricao statusNovo) { this.statusNovo = statusNovo; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
}