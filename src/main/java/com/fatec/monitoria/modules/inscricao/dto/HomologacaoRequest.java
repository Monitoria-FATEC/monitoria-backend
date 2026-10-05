package com.fatec.monitoria.modules.inscricao.dto;

import jakarta.validation.constraints.NotBlank;

public class HomologacaoRequest {

    @NotBlank
    private String assinaturaGestao;

    public String getAssinaturaGestao() { return assinaturaGestao; }
    public void setAssinaturaGestao(String assinaturaGestao) { this.assinaturaGestao = assinaturaGestao; }
}