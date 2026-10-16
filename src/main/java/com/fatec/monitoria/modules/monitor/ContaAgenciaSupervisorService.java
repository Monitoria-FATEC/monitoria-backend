package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.modules.monitor.dto.ContaAgenciaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// Supervisor confere a conta e a agência que o monitor enviou.
@Service
@RequiredArgsConstructor
public class ContaAgenciaSupervisorService {

    private final MonitorRepository monitorRepository;

    // Lista só quem já enviou conta/agência; status é opcional (AGUARDANDO, APROVADO, DEVOLVIDO).
    public List<ContaAgenciaResponse> listar(StatusDocumento status) {
        return monitorRepository.findAll().stream()
                .filter(m -> m.getNumeroConta() != null && m.getAgencia() != null)
                .filter(m -> status == null || statusDe(m) == status)
                .sorted(Comparator.comparing(Monitor::getNome,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(ContaAgenciaResponse::fromEntity)
                .toList();
    }

    public ContaAgenciaResponse aprovar(String idMonitor) {
        Monitor monitor = buscarComContaEnviada(idMonitor);
        monitor.setStatusContaAgencia(StatusDocumento.APROVADO);
        monitor.setJustificativaContaAgencia(null);
        monitor.setDataLimiteContaAgencia(null);
        return ContaAgenciaResponse.fromEntity(monitorRepository.save(monitor));
    }

    // dataLimite é opcional: prazo para o monitor corrigir.
    public ContaAgenciaResponse devolver(String idMonitor, String justificativa, LocalDate dataLimite) {
        Monitor monitor = buscarComContaEnviada(idMonitor);
        monitor.setStatusContaAgencia(StatusDocumento.DEVOLVIDO);
        monitor.setJustificativaContaAgencia(justificativa);
        monitor.setDataLimiteContaAgencia(dataLimite);
        return ContaAgenciaResponse.fromEntity(monitorRepository.save(monitor));
    }

    private Monitor buscarComContaEnviada(String idMonitor) {
        Monitor monitor = monitorRepository.findById(idMonitor)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Monitor não encontrado: " + idMonitor));
        if (Objects.isNull(monitor.getNumeroConta()) || Objects.isNull(monitor.getAgencia())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O monitor ainda não enviou conta e agência");
        }
        return monitor;
    }

    private StatusDocumento statusDe(Monitor m) {
        return m.getStatusContaAgencia() == null ? StatusDocumento.AGUARDANDO : m.getStatusContaAgencia();
    }
}