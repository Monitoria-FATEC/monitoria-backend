package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.modules.monitor.dto.ContaAgenciaRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorDashboardResponse;
import com.fatec.monitoria.modules.monitor.dto.PerfilMonitorRequest;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MonitorDashboardService {
    private final MonitorRepository monitorRepository;
    private final TermoCompromissoRepository termoRepository;

    public MonitorDashboardResponse buscar(String email) {
        Monitor monitor = buscarMonitor(email);
        TermoCompromisso termo = termoRepository.findFirstByRaOrderByDataEnvioDesc(monitor.getRa()).orElse(null);
        return MonitorDashboardResponse.of(monitor, termo);
    }

    public MonitorDashboardResponse salvarContaAgencia(String email, ContaAgenciaRequest request) {
        Monitor monitor = buscarMonitor(email);
        monitor.setNumeroConta(request.getNumeroConta());
        monitor.setAgencia(request.getAgencia());
        monitor.setStatusContaAgencia(StatusDocumento.AGUARDANDO);
        monitor.setJustificativaContaAgencia(null);
        return salvarERetornar(monitor);
    }

    public MonitorDashboardResponse atualizarPerfil(String email, PerfilMonitorRequest request) {
        Monitor monitor = buscarMonitor(email);
        if (monitor.getStatusContaAgencia() != StatusDocumento.APROVADO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O perfil só pode ser editado após a aprovação da conta e agência");
        }
        monitor.setTelefone(request.getTelefone());
        monitor.setLocalAtendimento(request.getLocalAtendimento());
        monitor.setHorariosAtendimento(request.getHorariosAtendimento());
        monitor.setLinkWhatsapp(request.getLinkWhatsapp());
        monitor.setLinkTeams(request.getLinkTeams());
        return salvarERetornar(monitor);
    }

    public void desativarPerfil(String email) {
        Monitor monitor = buscarMonitor(email);
        monitor.setPerfilAtivo(false);
        monitorRepository.save(monitor);
    }

    private MonitorDashboardResponse salvarERetornar(Monitor monitor) {
        monitorRepository.save(monitor);
        TermoCompromisso termo = termoRepository.findFirstByRaOrderByDataEnvioDesc(monitor.getRa()).orElse(null);
        return MonitorDashboardResponse.of(monitor, termo);
    }

    private Monitor buscarMonitor(String email) {
        return monitorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado para o usuário autenticado"));
    }
}
