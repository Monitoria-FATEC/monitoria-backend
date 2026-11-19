package com.fatec.monitoria.modules.monitor.dto;

import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MonitorGestaoResponse {

    private String id;
    private String nome;
    private String ra;
    private String email;
    private String curso;
    private boolean ativo;
    private String numeroConta;
    private String agencia;
    private StatusDocumento statusContaAgencia;
    private LocalDateTime dataCadastro;
    private String telefone;
    private String linkWhatsapp;
    private String linkTeams;
    private boolean compartilharContato;
    private String vigenciaInicio;
    private String vigenciaFim;

    public static MonitorGestaoResponse fromEntity(Monitor m) {
        MonitorGestaoResponse dto = new MonitorGestaoResponse();
        dto.setId(m.getId());
        dto.setNome(m.getNome());
        dto.setRa(m.getRa());
        dto.setEmail(m.getEmail());
        dto.setCurso(m.getCurso());
        // monitor antigo sem o campo é tratado como ativo
        dto.setAtivo(m.getPerfilAtivo() == null || m.getPerfilAtivo());
        dto.setNumeroConta(m.getNumeroConta());
        dto.setAgencia(m.getAgencia());
        dto.setStatusContaAgencia(m.getStatusContaAgencia() == null ? StatusDocumento.AGUARDANDO : m.getStatusContaAgencia());
        dto.setDataCadastro(m.getDataEnvio());
        dto.setTelefone(m.getTelefone());
        dto.setLinkWhatsapp(m.getLinkWhatsapp());
        dto.setLinkTeams(m.getLinkTeams());
        dto.setCompartilharContato(Boolean.TRUE.equals(m.getCompartilharContato()));
        dto.setVigenciaInicio(m.getVigenciaInicio() == null ? null : m.getVigenciaInicio().toString());
        dto.setVigenciaFim(m.getVigenciaFim() == null ? null : m.getVigenciaFim().toString());
        return dto;
    }
}