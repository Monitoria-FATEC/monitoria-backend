package com.fatec.monitoria.modules.termo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TermoCompromissoRequest {

    @NotBlank private String nomeEstudante;
    @NotBlank private String ra;
    @NotBlank private String cpf;
    @NotBlank private String curso;
    @NotBlank private String disciplina;
    @NotBlank private String oferta;

    private Integer cargaHoraria;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    @NotBlank private String editalNumero;
    @NotBlank private String nomeProfessor;
    @NotBlank private String nomeCoordenador;
    @NotBlank private String unidade;
    @NotBlank private String cidade;

    private LocalDate dataAssinatura;
    private Integer numeroVias;

    @NotBlank private String assinaturaEstudante;
}