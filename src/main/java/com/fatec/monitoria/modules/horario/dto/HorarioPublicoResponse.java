package com.fatec.monitoria.modules.horario.dto;

// Visão pública (aluno, sem login): só o necessário para achar o atendimento.
// Contato só pelos links que o próprio monitor publicou no perfil (WhatsApp e Teams).
// De propósito, sem e-mail, telefone nem ids.
public record HorarioPublicoResponse(
        String nome,
        String curso,
        String disciplina,
        String professor,
        Integer diaSemana,
        String inicio,
        String fim,
        String local,
        String linkWhatsapp,
        String linkTeams) {
}