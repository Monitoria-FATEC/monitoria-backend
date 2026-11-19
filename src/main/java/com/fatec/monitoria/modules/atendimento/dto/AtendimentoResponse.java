package com.fatec.monitoria.modules.atendimento.dto;

import com.fatec.monitoria.modules.atendimento.domain.RegistroAtendimento;

// Registro de atendimento do próprio monitor. A data sai como texto AAAA-MM-DD, o formato que o front usa.
public record AtendimentoResponse(
        String id,
        String aluno,
        String raAluno,
        String atividade,
        String data,
        String inicio,
        String termino) {

    public static AtendimentoResponse of(RegistroAtendimento r) {
        return new AtendimentoResponse(r.getId(), r.getAluno(), r.getRaAluno(), r.getAtividade(),
                r.getData() == null ? null : r.getData().toString(), r.getInicio(), r.getTermino());
    }
}