package com.fatec.monitoria.modules.termo.dto;

import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TermoCompromissoResponse {
    private String id;
    private String nomeEstudante;
    private String ra;
    private String disciplina;
    private LocalDateTime dataEnvio;

    public static TermoCompromissoResponse fromEntity(TermoCompromisso t) {
        TermoCompromissoResponse dto = new TermoCompromissoResponse();
        dto.setId(t.getId());
        dto.setNomeEstudante(t.getNomeEstudante());
        dto.setRa(t.getRa());
        dto.setDisciplina(t.getDisciplina());
        dto.setDataEnvio(t.getDataEnvio());
        return dto;
    }
}
