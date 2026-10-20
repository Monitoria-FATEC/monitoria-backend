package com.fatec.monitoria.modules.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

// Envio de e-mails do sistema.
// Com app.mail.enabled=false (ou sem configuração de SMTP) não envia: escreve o conteúdo
// no console, o que facilita testar sem depender do Gmail.
@Slf4j
@Service
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean habilitado;
    private final String remetente;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.enabled:false}") boolean habilitado,
            @Value("${spring.mail.username:}") String remetente) {
        this.mailSender = mailSender;
        this.habilitado = habilitado;
        this.remetente = remetente;
    }

    public void enviarCodigoRecuperacao(String destinatario, String nome, String codigo, int validadeMinutos) {
        String assunto = "Código para redefinir sua senha - Monitoria Fatec";
        String texto = "Olá, " + primeiroNome(nome) + "!\n\n"
                + "Seu código para redefinir a senha é: " + codigo + "\n\n"
                + "Ele vale por " + validadeMinutos + " minutos. "
                + "Se você não pediu a troca de senha, ignore este e-mail.\n\n"
                + "Monitoria Acadêmica - Fatec Zona Leste";

        enviar(destinatario, assunto, texto, codigo);
    }

    public void enviarCodigoVerificacao(String destinatario, String nome, String codigo, int validadeMinutos) {
        String assunto = "Confirme seu e-mail - Monitoria Fatec";
        String texto = "Olá, " + primeiroNome(nome) + "!\n\n"
                + "Seu código para confirmar o e-mail e concluir o cadastro é: " + codigo + "\n\n"
                + "Ele vale por " + validadeMinutos + " minutos. "
                + "Se você não fez esse cadastro, ignore este e-mail.\n\n"
                + "Monitoria Acadêmica - Fatec Zona Leste";

        enviar(destinatario, assunto, texto, codigo);
    }

    private void enviar(String destinatario, String assunto, String texto, String codigo) {
        JavaMailSender sender = mailSender.getIfAvailable();

        String motivo = null;
        if (!habilitado) {
            motivo = "app.mail.enabled está false ou não foi lido (confira o recuo de 'app:' no application.yml)";
        } else if (sender == null) {
            motivo = "spring.mail.host não foi lido, então o Spring não criou o JavaMailSender "
                    + "(confira o recuo de 'mail:' dentro de 'spring:')";
        } else if (remetente.isBlank()) {
            motivo = "spring.mail.username está vazio";
        }

        if (motivo != null) {
            log.warn("[E-MAIL SIMULADO] Envio real desligado: {}", motivo);
            log.info("[E-MAIL SIMULADO] Para: {} | Código: {}", destinatario, codigo);
            return;
        }

        try {
            MimeMessage mensagem = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, false, "UTF-8");
            helper.setFrom(remetente, "Monitoria Fatec");
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(texto, false);
            sender.send(mensagem);

            log.info("[E-MAIL] Mensagem \"{}\" enviada para {}", assunto, destinatario);
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            // quem pediu o código recebe a mesma resposta genérica; o motivo fica só no log
            log.error("[E-MAIL] Falha ao enviar para {}: {}", destinatario, e.getMessage());
        }
    }

    private String primeiroNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return "tudo bem";
        }
        return nome.trim().split("\\s+")[0];
    }
}