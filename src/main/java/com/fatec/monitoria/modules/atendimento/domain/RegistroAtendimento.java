package com.fatec.monitoria.modules.atendimento.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Uma linha da ficha de atendimentos: um atendimento que o monitor fez para um aluno.
@Data
@Document(collection = "registros_atendimento")
public class RegistroAtendimento {

    @Id
    private String id;

    // dono do registro (o monitor que atendeu)
    @Indexed
    private String monitorId;

    private String aluno;
    private String atividade;
    private LocalDate data;

    // "HH:mm"
    private String inicio;
    private String termino;

    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataAtualizacao = LocalDateTime.now();
}