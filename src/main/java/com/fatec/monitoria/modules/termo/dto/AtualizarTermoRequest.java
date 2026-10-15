package com.fatec.monitoria.modules.termo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AtualizarTermoRequest {
    @NotBlank private String disciplina;
    @NotBlank private String oferta;
    private Integer cargaHoraria;
    private String periodoInicio;
    private String periodoFim;
}
