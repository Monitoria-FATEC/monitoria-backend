package com.fatec.monitoria.modules.termo.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;

@Data
@Document(collection = "termos_compromisso")
public class TermoCompromisso {

    @Id
    private String id;

    private String nomeEstudante;
    private String ra;
    private String cpf;
    private String curso;
    private String disciplina;
    private String oferta;
    private Integer cargaHoraria;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private String editalNumero;
    private String nomeProfessor;
    private String nomeCoordenador;
    private String unidade;
    private String cidade;
    private LocalDate dataAssinatura;
    private Integer numeroVias;
    private String assinaturaEstudante;

    private LocalDateTime dataEnvio;
    private StatusDocumento status = StatusDocumento.AGUARDANDO;
    private String justificativaDevolucao;
}
