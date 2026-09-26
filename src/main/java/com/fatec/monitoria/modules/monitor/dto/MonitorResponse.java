package com.fatec.monitoria.modules.monitor.dto;

import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.StatusInscricao;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MonitorResponse {

    private String id;
    private String nome;
    private String email;
    private String curso;
    private StatusInscricao status;
    private LocalDateTime dataEnvio;
    private String ra;

    public static MonitorResponse fromEntity(Monitor m) {
        MonitorResponse dto = new MonitorResponse();
        dto.setId(m.getId());
        dto.setNome(m.getNome());
        dto.setEmail(m.getEmail());
        dto.setCurso(m.getCurso());
        dto.setStatus(m.getStatus());
        dto.setDataEnvio(m.getDataEnvio());
        dto.setRa(m.getRa());
        return dto;
    }
}