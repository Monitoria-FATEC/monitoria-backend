package com.fatec.monitoria.modules.atendimento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AtendimentoRequest {

    @NotBlank(message = "Informe o nome do aluno atendido")
    @Size(min = 3, max = 120, message = "O nome do aluno deve ter de 3 a 120 caracteres")
    private String aluno;

    @NotBlank(message = "Descreva a atividade realizada")
    @Size(min = 10, max = 500, message = "A atividade deve ter de 10 a 500 caracteres")
    private String atividade;

    @NotNull(message = "Informe o dia do atendimento")
    @PastOrPresent(message = "O atendimento não pode estar no futuro")
    private LocalDate data;

    @NotBlank(message = "Informe o início")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Início inválido: use o formato HH:mm")
    private String inicio;

    @NotBlank(message = "Informe o término")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Término inválido: use o formato HH:mm")
    private String termino;
}