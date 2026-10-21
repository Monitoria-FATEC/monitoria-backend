package com.fatec.monitoria.modules.monitor.dto;

// Resposta do cadastro manual: o monitor criado e a senha temporária (mostrada só nesta resposta).
public record MonitorCriadoResponse(
        MonitorGestaoResponse monitor,
        String senhaTemporaria
) {
}