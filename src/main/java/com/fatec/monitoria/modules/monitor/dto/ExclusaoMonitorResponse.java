package com.fatec.monitoria.modules.monitor.dto;

// Resumo do que será (ou foi) apagado junto com o monitor.
public record ExclusaoMonitorResponse(
        String nome,
        String ra,
        int inscricoes,
        int termos,
        boolean contaDeAcesso,
        boolean possuiTermoHomologado
) {
}