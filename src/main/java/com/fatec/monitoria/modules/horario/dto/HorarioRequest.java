package com.fatec.monitoria.modules.horario.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HorarioRequest {

    @NotNull(message = "Escolha o dia da semana")
    @Min(value = 1, message = "Dia da semana inválido")
    @Max(value = 6, message = "Dia da semana inválido")
    private Integer diaSemana;

    @NotBlank(message = "Informe o início")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Início inválido: use o formato HH:mm")
    private String inicio;

    @NotBlank(message = "Informe o fim")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Fim inválido: use o formato HH:mm")
    private String fim;

    @NotBlank(message = "Informe o local do atendimento")
    @Size(max = 120, message = "O local pode ter no máximo 120 caracteres")
    private String local;
}