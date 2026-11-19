package com.fatec.monitoria.modules.monitor.dto;

import java.util.List;

// Resultado da limpeza de dados órfãos: o que foi encontrado (e, se executado, apagado).
public record LimpezaDadosResponse(
        boolean executado,
        // inscrições cujo monitor não existe mais (apagadas na limpeza)
        List<String> inscricoesOrfas,
        // termos que não pertencem a nenhum monitor existente nem a inscrição válida (apagados na limpeza)
        List<String> termosOrfaos,
        // inscrições de monitores reais cujo termo não existe mais (só listadas, nunca apagadas)
        List<String> inscricoesSemTermo,
        // monitores sem nenhuma inscrição (só listados, nunca apagados)
        List<String> monitoresSemInscricao) {
}