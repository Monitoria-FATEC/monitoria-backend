package com.fatec.monitoria.modules.horario.dto;

// Horário de qualquer monitor, com os dados dele (visão do supervisor e do PDF).
// "vigente" diz se o período do monitor está valendo hoje (se não, ele não aparece na página pública).
public record HorarioMonitorResponse(
        String id,
        String monitorId,
        String nome,
        String curso,
        String disciplina,
        String professor,
        Integer diaSemana,
        String inicio,
        String fim,
        String local,
        String vigenciaInicio,
        String vigenciaFim,
        boolean vigente) {
}