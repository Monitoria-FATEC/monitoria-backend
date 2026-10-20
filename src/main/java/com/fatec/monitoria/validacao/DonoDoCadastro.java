package com.fatec.monitoria.validacao;

import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

// Garante que quem está logado só cadastra e envia coisas em nome de si mesmo.
@Component
@RequiredArgsConstructor
public class DonoDoCadastro {

    private final MonitorRepository monitorRepository;

    // O cadastro de monitor tem de usar o e-mail da conta logada.
    public void exigirMesmoEmail(String emailLogado, String emailInformado) {
        if (emailLogado == null || emailInformado == null
                || !emailLogado.trim().equalsIgnoreCase(emailInformado.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "O cadastro deve ser feito com o e-mail da conta logada");
        }
    }

    // O termo tem de ser do RA do monitor logado.
    public void exigirRaDoMonitor(String emailLogado, String ra) {
        Monitor monitor = monitorRepository.findByEmailIgnoreCase(emailLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Faça o cadastro de monitor antes de enviar o termo"));

        if (ra == null || !ra.trim().equals(monitor.getRa())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "O RA do termo deve ser o do monitor logado");
        }
    }
}