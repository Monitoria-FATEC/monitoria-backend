package com.fatec.monitoria.modules.monitor.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "monitores")
public class Monitor {

    @Id
    private String id;

    private String nome;
    private String ra;
    private String email;
    private String curso;
    private StatusInscricao status;
    private LocalDateTime dataEnvio;
}
