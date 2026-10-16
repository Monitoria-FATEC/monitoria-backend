package com.fatec.monitoria.modules.termo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AtualizarTermoRequest {

    @NotBlank(message = "A disciplina é obrigatória")
    private String disciplina;

    @NotBlank(message = "A oferta é obrigatória")
    private String oferta;

    private Integer cargaHoraria;
    private String periodoInicio;
    private String periodoFim;

    // Campos opcionais: se vierem vazios, o valor que já está no termo é mantido
    private String nomeEstudante;
    private String cpf;
    private String curso;
    private String editalNumero;
    private String nomeProfessor;
    private String nomeCoordenador;
    private String unidade;
    private String cidade;
    private String dataAssinatura;
    private Integer numeroVias;

    // Nova assinatura (imagem em base64). Se vier, substitui a anterior.
    private String assinaturaEstudante;
}