package com.fatec.monitoria.modules.horario.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// Um horário semanal em que o monitor atende (ex.: toda segunda, das 14:00 às 17:00).
@Data
@Document(collection = "horarios_atendimento")
public class HorarioAtendimento {

    @Id
    private String id;

    @Indexed
    private String monitorId;

    // 1 = segunda ... 6 = sábado
    private Integer diaSemana;

    // "HH:mm" (texto compara certo na ordem alfabética)
    private String inicio;
    private String fim;

    private String local;
}