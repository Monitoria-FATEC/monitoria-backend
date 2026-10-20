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
import com.fatec.monitoria.validacao.CampoDuplicadoException;
import com.fatec.monitoria.validacao.Regras;
import com.fatec.monitoria.validacao.TelefoneBrValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

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

        // telefone gravado sempre no mesmo formato, e não pode ser de outro monitor
        String telefone = null;
        if (request.getTelefone() != null && !request.getTelefone().isBlank()) {
            telefone = TelefoneBrValidator.formatar(request.getTelefone());
            List<String> grafias = List.of(telefone, TelefoneBrValidator.nacional(telefone));
            if (monitorRepository.existsByTelefoneInAndIdNot(grafias, monitor.getId())) {
                throw new CampoDuplicadoException("telefone", "Este telefone já está cadastrado para outro monitor");
            }
        }

        monitor.setTelefone(telefone);
        monitor.setLocalAtendimento(vazioParaNulo(request.getLocalAtendimento()));
        monitor.setHorariosAtendimento(vazioParaNulo(request.getHorariosAtendimento()));
        monitor.setLinkWhatsapp(vazioParaNulo(request.getLinkWhatsapp()));
        monitor.setLinkTeams(vazioParaNulo(request.getLinkTeams()));
        return salvarERetornar(monitor);
    }

    public void desativarPerfil(String email) {
        Monitor monitor = buscarMonitor(email);
        monitor.setPerfilAtivo(false);
        monitorRepository.save(monitor);
    }

    private String vazioParaNulo(String valor) {
        String limpo = Regras.normalizarTexto(valor);
        return limpo == null || limpo.isEmpty() ? null : limpo;
    }

    private MonitorDashboardResponse salvarERetornar(Monitor monitor) {
        monitorRepository.save(monitor);
        return montarResposta(monitor);
    }

    private MonitorDashboardResponse montarResposta(Monitor monitor) {
        TermoCompromisso termo = termoRepository.findFirstByRaOrderByDataEnvioDesc(monitor.getRa()).orElse(null);
        return MonitorDashboardResponse.of(monitor, termo, inscricaoMaisRecente(monitor));
    }

    // Inscrição mais recente do monitor (null se ele ainda não tem inscrição).
    private Inscricao inscricaoMaisRecente(Monitor monitor) {
        return inscricaoRepository.findByIdMonitor(monitor.getId()).stream()
                .max(Comparator.comparing(Inscricao::getDataSubmissao,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
    }

    // Regra do stakeholder: conta e agência só depois que o termo foi homologado.
    private void exigirTermoHomologado(Monitor monitor) {
        Inscricao inscricao = inscricaoMaisRecente(monitor);
        if (inscricao == null || inscricao.getStatus() != StatusInscricao.HOMOLOGADA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Conta e agência só são liberadas depois que o termo for homologado");
        }
    }

    private Monitor buscarMonitor(String email) {
        return monitorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado para o usuário autenticado"));
    }
}