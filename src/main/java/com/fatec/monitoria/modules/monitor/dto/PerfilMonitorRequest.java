package com.fatec.monitoria.modules.monitor.dto;

import com.fatec.monitoria.validacao.TelefoneBr;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PerfilMonitorRequest {

    @TelefoneBr(message = "Telefone inválido. Use DDD + número, como (11) 98765-4321")
    private String telefone;

    @Size(max = 500, message = "O link do WhatsApp deve ter no máximo 500 caracteres")
    @Pattern(regexp = "^\\s*$|^(?i)\\s*https://(wa\\.me|api\\.whatsapp\\.com|chat\\.whatsapp\\.com|web\\.whatsapp\\.com|whatsapp\\.com)/\\S*\\s*$",
            message = "Link do WhatsApp inválido. Use um link https://wa.me/55119... ou de grupo (chat.whatsapp.com)")
    private String linkWhatsapp;

    @Size(max = 500, message = "O link do Teams deve ter no máximo 500 caracteres")
    @Pattern(regexp = "^\\s*$|^(?i)\\s*https://(teams\\.microsoft\\.com|teams\\.live\\.com|teams\\.cloud\\.microsoft)/\\S*\\s*$",
            message = "Link do Teams inválido. Use o link da reunião ou do canal (https://teams.microsoft.com/...)")
    private String linkTeams;

    // Consentimento: o monitor autoriza mostrar e-mail e links de contato na página pública
    private Boolean compartilharContato;

    // Vigência (opcional): o monitor só aparece na página de monitorias dentro deste período
    private LocalDate vigenciaInicio;

    private LocalDate vigenciaFim;
}