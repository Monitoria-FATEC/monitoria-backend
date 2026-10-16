package com.fatec.monitoria.modules.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemResponse {

    private String mensagem;
    // validade do código em segundos (o front usa para o cronômetro); null quando não se aplica
    private Integer expiraEmSegundos;
}