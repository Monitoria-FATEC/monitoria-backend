package com.fatec.monitoria.modules.atendimento;

import com.fatec.monitoria.modules.atendimento.domain.RegistroAtendimento;
import com.fatec.monitoria.modules.atendimento.domain.RegistroAtendimentoRepository;
import com.fatec.monitoria.modules.atendimento.dto.AtendimentoRequest;
import com.fatec.monitoria.modules.atendimento.dto.AtendimentoResponse;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.validacao.CampoInvalidoException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

// Ficha de atendimentos: cada monitor só enxerga e mexe nos próprios registros.
@Service
@RequiredArgsConstructor
public class AtendimentoService {

    // mais recentes primeiro (data e depois início)
    private static final Comparator<RegistroAtendimento> MAIS_RECENTES_PRIMEIRO =
            Comparator.comparing(RegistroAtendimento::getData)
                    .thenComparing(RegistroAtendimento::getInicio)
                    .reversed();

    private final RegistroAtendimentoRepository registroRepository;
    private final MonitorRepository monitorRepository;

    public List<AtendimentoResponse> listar(String email) {
        Monitor monitor = buscarMonitor(email);
        return registroRepository.findByMonitorId(monitor.getId()).stream()
                .sorted(MAIS_RECENTES_PRIMEIRO)
                .map(AtendimentoResponse::of)
                .toList();
    }

    public AtendimentoResponse criar(String email, AtendimentoRequest request) {
        Monitor monitor = buscarMonitor(email);
        exigirPerfilLiberado(monitor);
        validarIntervalo(request);

        RegistroAtendimento registro = new RegistroAtendimento();
        registro.setMonitorId(monitor.getId());
        preencher(registro, request);
        return AtendimentoResponse.of(registroRepository.save(registro));
    }

    public AtendimentoResponse atualizar(String email, String id, AtendimentoRequest request) {
        Monitor monitor = buscarMonitor(email);
        exigirPerfilLiberado(monitor);
        RegistroAtendimento registro = buscarDoMonitor(monitor, id);
        validarIntervalo(request);

        preencher(registro, request);
        registro.setDataAtualizacao(LocalDateTime.now());
        return AtendimentoResponse.of(registroRepository.save(registro));
    }

    public void excluir(String email, String id) {
        Monitor monitor = buscarMonitor(email);
        registroRepository.delete(buscarDoMonitor(monitor, id));
    }

    // ---------- apoio ----------

    private void preencher(RegistroAtendimento registro, AtendimentoRequest request) {
        registro.setAluno(request.getAluno().trim());
        registro.setAtividade(request.getAtividade().trim());
        registro.setData(request.getData());
        registro.setInicio(request.getInicio());
        registro.setTermino(request.getTermino());
    }

    private void validarIntervalo(AtendimentoRequest request) {
        if (request.getTermino().compareTo(request.getInicio()) <= 0) {
            throw new CampoInvalidoException("termino", "O término precisa ser depois do início");
        }
    }

    // mesma regra do perfil: só depois da aprovação de conta e agência
    private void exigirPerfilLiberado(Monitor monitor) {
        if (monitor.getStatusContaAgencia() != StatusDocumento.APROVADO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "A ficha de atendimentos só é liberada após a aprovação da conta e agência");
        }
    }

    // registro de outro monitor responde 404, como se não existisse
    private RegistroAtendimento buscarDoMonitor(Monitor monitor, String id) {
        return registroRepository.findById(id)
                .filter(r -> monitor.getId().equals(r.getMonitorId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado"));
    }

    private Monitor buscarMonitor(String email) {
        return monitorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Monitor não encontrado para o usuário autenticado"));
    }
}