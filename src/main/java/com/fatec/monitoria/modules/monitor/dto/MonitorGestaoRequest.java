package com.fatec.monitoria.modules.monitor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fatec.monitoria.validacao.TelefoneBr;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MonitorGestaoRequest {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "O curso é obrigatório")
    private String curso;

    // conta e agência podem ficar vazias (monitor que ainda não enviou)
    @Pattern(regexp = "^$|^\\d{4,12}(-[0-9Xx])?$",
            message = "Conta inválida: use de 4 a 12 dígitos, com dígito verificador opcional (ex.: 12345-6)")
    private String numeroConta;

    @Pattern(regexp = "^$|^\\d{3,5}(-[0-9Xx])?$",
            message = "Agência inválida: use de 3 a 5 dígitos, com dígito verificador opcional (ex.: 1234 ou 1234-5)")
    private String agencia;

    // Dados do perfil do monitor (o supervisor também pode editar)
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

    private Boolean compartilharContato;

    private LocalDate vigenciaInicio;

    private LocalDate vigenciaFim;
}