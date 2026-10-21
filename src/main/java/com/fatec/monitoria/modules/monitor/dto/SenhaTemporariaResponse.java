package com.fatec.monitoria.modules.monitor.dto;

// A senha temporária aparece só nesta resposta: no banco fica apenas o hash dela.
public record SenhaTemporariaResponse(
        String email,
        String senhaTemporaria
) {
}