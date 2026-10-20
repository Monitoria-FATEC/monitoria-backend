package com.fatec.monitoria.modules.termo.dto;

import com.fatec.monitoria.validacao.Cpf;
import com.fatec.monitoria.validacao.Regras;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AtualizarTermoRequest {

    @NotBlank(message = "A disciplina é obrigatória")
    @Size(max = 150, message = "A disciplina deve ter no máximo 150 caracteres")
    private String disciplina;

    @NotBlank(message = "A oferta é obrigatória")
    @Pattern(regexp = "(?i)^\\s*(anual|semestral)\\s*$",
            message = "A oferta deve ser anual ou semestral")
    private String oferta;

    @Min(value = 4, message = "A carga horária semanal deve ser de 4 a 8 horas")
    @Max(value = 8, message = "A carga horária semanal deve ser de 4 a 8 horas")
    private Integer cargaHoraria;

    private String periodoInicio;
    private String periodoFim;

    // Campos opcionais: se vierem vazios, o valor que já está no termo é mantido
    @Size(max = Regras.NOME_MAX, message = "O nome deve ter no máximo 100 caracteres")
    @Pattern(regexp = "^\\s*$|" + Regras.NOME, message = Regras.NOME_MSG)
    private String nomeEstudante;

    @Cpf(message = "CPF inválido")
    private String cpf;

    @Size(max = 100, message = "O curso deve ter no máximo 100 caracteres")
    private String curso;

    @Pattern(regexp = "^\\s*$|^\\s*(?!0+\\s*/)\\d{1,3}\\s*/\\s*\\d{4}\\s*$",
            message = "Use o formato número/ano, como 04/2026")
    private String editalNumero;

    @Size(max = 100, message = "O nome do professor deve ter no máximo 100 caracteres")
    private String nomeProfessor;

    @Size(max = 100, message = "O nome do coordenador deve ter no máximo 100 caracteres")
    private String nomeCoordenador;

    @Size(max = 100, message = "A unidade deve ter no máximo 100 caracteres")
    private String unidade;

    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
    private String cidade;

    private String dataAssinatura;

    @Min(value = 1, message = "O número de vias deve ficar entre 1 e 10")
    @Max(value = 10, message = "O número de vias deve ficar entre 1 e 10")
    private Integer numeroVias;

    // Nova assinatura (imagem em base64). Se vier, substitui a anterior.
    @Size(max = 1_500_000, message = "A assinatura é grande demais")
    private String assinaturaEstudante;
}