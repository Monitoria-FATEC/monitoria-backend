package com.fatec.monitoria.modules.horario.dto;

import com.fatec.monitoria.modules.horario.domain.HorarioAtendimento;

// Horário do próprio monitor.
public record HorarioResponse(String id, Integer diaSemana, String inicio, String fim, String local) {

    public static HorarioResponse of(HorarioAtendimento h) {
        return new HorarioResponse(h.getId(), h.getDiaSemana(), h.getInicio(), h.getFim(), h.getLocal());
    }
}