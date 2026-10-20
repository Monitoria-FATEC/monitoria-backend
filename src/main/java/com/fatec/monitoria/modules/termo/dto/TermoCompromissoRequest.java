package com.fatec.monitoria.modules.termo.dto;

import com.fatec.monitoria.validacao.Cpf;
import com.fatec.monitoria.validacao.Regras;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TermoCompromissoRequest {

    @NotBlank(message = "O nome do estudante é obrigatório")
    @Size(max = Regras.NOME_MAX, message = "O nome deve ter no máximo 100 caracteres")
    @Pattern(regexp = Regras.NOME, message = Regras.NOME_MSG)
    private String nomeEstudante;

    @NotBlank(message = "O RA é obrigatório")
    @Pattern(regexp = Regras.RA, message = Regras.RA_MSG)
    private String ra;

    @NotBlank(message = "O CPF é obrigatório")
    @Cpf(message = "CPF inválido")
    private String cpf;

    @NotBlank(message = "O curso é obrigatório")
    @Size(max = 100, message = "O curso deve ter no máximo 100 caracteres")
    private String curso;

    @NotBlank(message = "A disciplina é obrigatória")
    @Size(max = 150, message = "A disciplina deve ter no máximo 150 caracteres")
    private String disciplina;

    @NotBlank(message = "A oferta é obrigatória")
    @Pattern(regexp = "(?i)^\\s*(anual|semestral)\\s*$",
            message = "A oferta deve ser anual ou semestral")
    private String oferta;

    @NotNull(message = "A carga horária é obrigatória")
    @Min(value = 4, message = "A carga horária semanal deve ser de 4 a 8 horas")
    @Max(value = 8, message = "A carga horária semanal deve ser de 4 a 8 horas")
    private Integer cargaHoraria;

    @NotNull(message = "A data de início do período é obrigatória")
    private LocalDate periodoInicio;

    @NotNull(message = "A data de fim do período é obrigatória")
    private LocalDate periodoFim;

    @NotBlank(message = "O número do edital é obrigatório")
    @Pattern(regexp = "^\\s*(?!0+\\s*/)\\d{1,3}\\s*/\\s*\\d{4}\\s*$",
            message = "Use o formato número/ano, como 04/2026")
    private String editalNumero;

    @NotBlank(message = "O nome do professor é obrigatório")
    @Size(min = 3, max = 100, message = "O nome do professor deve ter de 3 a 100 caracteres")
    private String nomeProfessor;

    @NotBlank(message = "O nome do coordenador é obrigatório")
    @Size(min = 3, max = 100, message = "O nome do coordenador deve ter de 3 a 100 caracteres")
    private String nomeCoordenador;

    @NotBlank(message = "A unidade é obrigatória")
    @Size(max = 100, message = "A unidade deve ter no máximo 100 caracteres")
    private String unidade;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @NotNull(message = "A data de assinatura é obrigatória")
    private LocalDate dataAssinatura;

    @NotNull(message = "O número de vias é obrigatório")
    @Min(value = 1, message = "O número de vias deve ficar entre 1 e 10")
    @Max(value = 10, message = "O número de vias deve ficar entre 1 e 10")
    private Integer numeroVias;

    @NotBlank(message = "A assinatura é obrigatória")
    @Size(max = 1_500_000, message = "A assinatura é grande demais")
    private String assinaturaEstudante;
}