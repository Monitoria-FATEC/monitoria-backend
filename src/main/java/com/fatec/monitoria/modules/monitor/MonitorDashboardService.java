package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.InscricaoRepository;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
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

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class MonitorDashboardService {
    private final MonitorRepository monitorRepository;
    private final TermoCompromissoRepository termoRepository;
    private final InscricaoRepository inscricaoRepository;

    public MonitorDashboardResponse buscar(String email) {
        Monitor monitor = buscarMonitor(email);
        return montarResposta(monitor);
    }

    // Só com o termo homologado. Primeiro envio, ou reenvio depois que o supervisor devolveu.
    public MonitorDashboardResponse salvarContaAgencia(String email, ContaAgenciaRequest request) {
        Monitor monitor = buscarMonitor(email);
        exigirTermoHomologado(monitor);

        boolean jaEnviou = monitor.getNumeroConta() != null && monitor.getAgencia() != null;
        if (jaEnviou && monitor.getStatusContaAgencia() != StatusDocumento.DEVOLVIDO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Conta e agência já enviadas (status: " + monitor.getStatusContaAgencia()
                            + "). Só é possível reenviar se o supervisor devolver.");
        }

        monitor.setNumeroConta(request.getNumeroConta());
        monitor.setAgencia(request.getAgencia());
        monitor.setStatusContaAgencia(StatusDocumento.AGUARDANDO);
        monitor.setJustificativaContaAgencia(null);
        monitor.setDataLimiteContaAgencia(null);
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
        return montarResposta(monitor);
    }

    private MonitorDashboardResponse montarResposta(Monitor monitor) {
        TermoCompromisso termo = termoRepository.findFirstByRaOrderByDataEnvioDesc(monitor.getRa()).orElse(null);
        return MonitorDashboardResponse.of(monitor, termo, statusDaInscricao(monitor));
    }

    // Status da inscrição mais recente do monitor (null se ele ainda não tem inscrição).
    private StatusInscricao statusDaInscricao(Monitor monitor) {
        return inscricaoRepository.findByIdMonitor(monitor.getId()).stream()
                .max(Comparator.comparing(Inscricao::getDataSubmissao,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(Inscricao::getStatus)
                .orElse(null);
    }

    // Regra do stakeholder: conta e agência só depois que o termo foi homologado.
    private void exigirTermoHomologado(Monitor monitor) {
        if (statusDaInscricao(monitor) != StatusInscricao.HOMOLOGADA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Conta e agência só são liberadas depois que o termo for homologado");
        }
    }

    private Monitor buscarMonitor(String email) {
        return monitorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado para o usuário autenticado"));
    }
}