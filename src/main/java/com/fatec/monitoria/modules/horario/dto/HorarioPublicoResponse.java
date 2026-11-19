package com.fatec.monitoria.modules.horario.dto;

// Visão pública (aluno, sem login): só o necessário para achar o atendimento.
// Os dados de contato (e-mail, WhatsApp e Teams) só vêm preenchidos quando o próprio monitor
// marcou, no perfil, que aceita compartilhá-los (contatoCompartilhado = true).
// De propósito, sem telefone nem ids.
public record HorarioPublicoResponse(
        String nome,
        String curso,
        String disciplina,
        String professor,
        Integer diaSemana,
        String inicio,
        String fim,
        String local,
        boolean contatoCompartilhado,
        String email,
        String linkWhatsapp,
        String linkTeams) {
}