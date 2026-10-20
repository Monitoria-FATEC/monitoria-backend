package com.fatec.monitoria.validacao;

import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.usuario.domain.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

// Confere se e-mail e RA ainda estão livres. Junta todos os conflitos
// para o aluno corrigir de uma vez, em vez de descobrir um por tentativa.
@Service
@RequiredArgsConstructor
public class DisponibilidadeCadastroService {

    private final UsuarioRepository usuarioRepository;
    private final MonitorRepository monitorRepository;

    // checarUsuarios: false quando a conta de acesso acabou de ser criada no mesmo cadastro
    public void exigirDisponiveis(String email, String ra, boolean checarUsuarios) {
        Map<String, String> conflitos = new LinkedHashMap<>();

        if (email != null && !email.isBlank()) {
            // conta ainda não confirmada por e-mail não ocupa o endereço: quem digitou errado pode tentar de novo
            boolean usuarioEmUso = checarUsuarios
                    && usuarioRepository.findByEmailIgnoreCase(email)
                    .map(u -> !u.isEmailPendenteVerificacao())
                    .orElse(false);

            if (monitorRepository.existsByEmailIgnoreCase(email) || usuarioEmUso) {
                conflitos.put("email", "Este e-mail já está cadastrado");
            }
        }

        if (ra != null && !ra.isBlank() && monitorRepository.existsByRa(ra)) {
            conflitos.put("ra", "Este RA já está cadastrado");
        }

        if (!conflitos.isEmpty()) {
            throw new CampoDuplicadoException(conflitos);
        }
    }
}