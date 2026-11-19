package com.fatec.monitoria.modules.monitor.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private StatusDocumento statusContaAgencia = StatusDocumento.AGUARDANDO;
    private String numeroConta;
    private String agencia;
    private String telefone;
    private String linkWhatsapp;
    private String linkTeams;
    // Consentimento do monitor: só com true o e-mail e os links de contato aparecem na página pública.
    // Padrão false (não compartilha) para monitores que ainda não escolheram.
    private Boolean compartilharContato = false;
    private Boolean perfilAtivo = true;
    private String justificativaContaAgencia;
    // Prazo (opcional) que o supervisor dá para o monitor corrigir conta/agência devolvida
    private LocalDate dataLimiteContaAgencia;

    // Período em que o monitor atende (editado por ele no perfil). Fora dele, some da página pública.
    // Se vazio, vale o período do termo de compromisso.
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;

    // Aviso para o monitor quando o supervisor edita o perfil dele. Some quando o monitor clica em "Entendi".
    // avisoPerfilEm == null significa que não há aviso pendente.
    private LocalDateTime avisoPerfilEm;
    private String avisoPerfilPor;
    private List<String> avisoPerfilCampos;
}