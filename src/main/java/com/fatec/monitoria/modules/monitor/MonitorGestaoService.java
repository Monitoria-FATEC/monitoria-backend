package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.dto.MonitorGestaoRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorGestaoResponse;
import com.fatec.monitoria.modules.usuario.domain.Usuario;
import com.fatec.monitoria.modules.usuario.domain.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorGestaoService {

    private final MonitorRepository monitorRepository;
    private final UsuarioRepository usuarioRepository;

    // Lista todos os monitores (ativos e inativos) em ordem alfabética
    public List<MonitorGestaoResponse> listar() {
        return monitorRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        (Monitor m) -> m.getNome() == null ? "" : m.getNome(),
                        String.CASE_INSENSITIVE_ORDER))
                .map(MonitorGestaoResponse::fromEntity)
                .toList();
    }

    // Edita nome, e-mail, curso, conta e agência. O RA não muda (o termo é ligado a ele).
    public MonitorGestaoResponse atualizar(String id, MonitorGestaoRequest request) {
        Monitor monitor = buscar(id);

        String emailAntigo = monitor.getEmail();
        String emailNovo = request.getEmail().trim().toLowerCase();
        boolean emailMudou = emailAntigo == null || !emailAntigo.equalsIgnoreCase(emailNovo);

        if (emailMudou) {
            exigirEmailLivre(emailNovo, monitor.getId());
        }

        // O login é ligado ao monitor pelo e-mail: mantém os dois em sincronia
        Usuario usuario = emailAntigo == null
                ? null
                : usuarioRepository.findByEmailIgnoreCase(emailAntigo).orElse(null);
        if (usuario != null) {
            usuario.setEmail(emailNovo);
            usuario.setNome(request.getNome().trim());
            usuarioRepository.save(usuario);
        }

        monitor.setNome(request.getNome().trim());
        monitor.setEmail(emailNovo);
        monitor.setCurso(request.getCurso().trim());
        monitor.setNumeroConta(vazioParaNulo(request.getNumeroConta()));
        monitor.setAgencia(vazioParaNulo(request.getAgencia()));

        return MonitorGestaoResponse.fromEntity(monitorRepository.save(monitor));
    }

    // Ativa ou desativa o perfil (não apaga nada)
    public MonitorGestaoResponse alterarAtivo(String id, boolean ativo) {
        Monitor monitor = buscar(id);
        monitor.setPerfilAtivo(ativo);
        return MonitorGestaoResponse.fromEntity(monitorRepository.save(monitor));
    }

    private Monitor buscar(String id) {
        return monitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado"));
    }

    // 409 se o e-mail já pertence a outro monitor ou a outro login
    private void exigirEmailLivre(String email, String idDoMonitor) {
        monitorRepository.findByEmailIgnoreCase(email).ifPresent(outro -> {
            if (!outro.getId().equals(idDoMonitor)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um monitor com este e-mail");
            }
        });

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso por outra conta");
        }
    }

    private String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}