package com.fatec.monitoria.modules.horario;

import com.fatec.monitoria.modules.horario.domain.HorarioAtendimento;
import com.fatec.monitoria.modules.horario.domain.HorarioAtendimentoRepository;
import com.fatec.monitoria.modules.horario.dto.HorarioMonitorResponse;
import com.fatec.monitoria.modules.horario.dto.HorarioPublicoResponse;
import com.fatec.monitoria.modules.horario.dto.HorarioRequest;
import com.fatec.monitoria.modules.horario.dto.HorarioResponse;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import com.fatec.monitoria.validacao.CampoInvalidoException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private static final Comparator<HorarioAtendimento> POR_DIA_E_HORA =
            Comparator.comparing(HorarioAtendimento::getDiaSemana).thenComparing(HorarioAtendimento::getInicio);

    private final HorarioAtendimentoRepository horarioRepository;
    private final MonitorRepository monitorRepository;
    private final TermoCompromissoRepository termoRepository;

    // ---------- monitor (só mexe nos próprios horários) ----------

    public List<HorarioResponse> listarMeus(String email) {
        Monitor monitor = buscarMonitor(email);
        return horarioRepository.findByMonitorId(monitor.getId()).stream()
                .sorted(POR_DIA_E_HORA)
                .map(HorarioResponse::of)
                .toList();
    }

    public HorarioResponse criar(String email, HorarioRequest request) {
        Monitor monitor = buscarMonitor(email);
        exigirPerfilLiberado(monitor);
        validarIntervalo(request);
        validarConflito(monitor.getId(), request, null);

        HorarioAtendimento horario = new HorarioAtendimento();
        horario.setMonitorId(monitor.getId());
        preencher(horario, request);
        return HorarioResponse.of(horarioRepository.save(horario));
    }

    public HorarioResponse atualizar(String email, String id, HorarioRequest request) {
        Monitor monitor = buscarMonitor(email);
        exigirPerfilLiberado(monitor);
        HorarioAtendimento horario = buscarDoMonitor(monitor, id);
        validarIntervalo(request);
        validarConflito(monitor.getId(), request, horario.getId());

        preencher(horario, request);
        return HorarioResponse.of(horarioRepository.save(horario));
    }

    public void remover(String email, String id) {
        Monitor monitor = buscarMonitor(email);
        horarioRepository.delete(buscarDoMonitor(monitor, id));
    }

    // ---------- supervisor (vê os horários de todos os monitores) ----------

    public List<HorarioMonitorResponse> listarDeTodos() {
        Map<String, Monitor> monitores = monitorRepository.findAll().stream()
                .filter(m -> !Boolean.FALSE.equals(m.getPerfilAtivo()))
                .collect(Collectors.toMap(Monitor::getId, Function.identity()));

        // disciplina, professor e período vêm do termo de compromisso mais recente de cada monitor
        Map<String, DadosDoTermo> termos = new HashMap<>();
        LocalDate hoje = LocalDate.now();

        return horarioRepository.findAll().stream()
                .filter(h -> monitores.containsKey(h.getMonitorId()))
                .sorted(POR_DIA_E_HORA)
                .map(h -> {
                    Monitor m = monitores.get(h.getMonitorId());
                    DadosDoTermo termo = termos.computeIfAbsent(m.getId(), id -> dadosDoTermo(m));
                    LocalDate inicioVig = inicioDaVigencia(m, termo);
                    LocalDate fimVig = fimDaVigencia(m, termo);
                    return new HorarioMonitorResponse(h.getId(), m.getId(), m.getNome(), m.getCurso(),
                            termo.disciplina(), termo.professor(), h.getDiaSemana(), h.getInicio(), h.getFim(), h.getLocal(),
                            inicioVig == null ? null : inicioVig.toString(),
                            fimVig == null ? null : fimVig.toString(),
                            vigente(m, termo, hoje));
                })
                .toList();
    }

    // ---------- público (aluno, sem login) ----------

    // Só monitores com conta e agência aprovadas, perfil ativo e vigência em dia.
    public List<HorarioPublicoResponse> listarPublicos() {
        Map<String, Monitor> liberados = monitorRepository.findAll().stream()
                .filter(m -> !Boolean.FALSE.equals(m.getPerfilAtivo()))
                .filter(m -> m.getStatusContaAgencia() == StatusDocumento.APROVADO)
                .collect(Collectors.toMap(Monitor::getId, Function.identity()));

        Map<String, DadosDoTermo> termos = new HashMap<>();
        LocalDate hoje = LocalDate.now();
        liberados.values().forEach(m -> termos.put(m.getId(), dadosDoTermo(m)));

        // só monitores dentro da vigência: semestres e anos anteriores não aparecem para o aluno
        return horarioRepository.findAll().stream()
                .filter(h -> liberados.containsKey(h.getMonitorId()))
                .filter(h -> vigente(liberados.get(h.getMonitorId()), termos.get(h.getMonitorId()), hoje))
                .sorted(POR_DIA_E_HORA)
                .map(h -> {
                    Monitor m = liberados.get(h.getMonitorId());
                    DadosDoTermo termo = termos.get(m.getId());
                    return new HorarioPublicoResponse(m.getNome(), m.getCurso(), termo.disciplina(), termo.professor(),
                            h.getDiaSemana(), h.getInicio(), h.getFim(), h.getLocal(),
                            vazioParaNulo(m.getLinkWhatsapp()), vazioParaNulo(m.getLinkTeams()));
                })
                .toList();
    }

    // ---------- apoio ----------

    private String vazioParaNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private record DadosDoTermo(String disciplina, String professor, LocalDate periodoInicio, LocalDate periodoFim) {
    }

    private DadosDoTermo dadosDoTermo(Monitor monitor) {
        return termoRepository.findFirstByRaOrderByDataEnvioDesc(monitor.getRa())
                .map(t -> new DadosDoTermo(
                        t.getDisciplina() != null ? t.getDisciplina() : "Não informada",
                        vazioParaNulo(t.getNomeProfessor()),
                        t.getPeriodoInicio(), t.getPeriodoFim()))
                .orElse(new DadosDoTermo("Não informada", null, null, null));
    }

    // Vigência do monitor: a que ele definiu no perfil; se não definiu, a do termo de compromisso.
    // Sem data de fim em nenhum dos dois, não dá para saber até quando atende: não é vigente.
    private LocalDate inicioDaVigencia(Monitor monitor, DadosDoTermo termo) {
        return monitor.getVigenciaInicio() != null ? monitor.getVigenciaInicio() : termo.periodoInicio();
    }

    private LocalDate fimDaVigencia(Monitor monitor, DadosDoTermo termo) {
        return monitor.getVigenciaFim() != null ? monitor.getVigenciaFim() : termo.periodoFim();
    }

    private boolean vigente(Monitor monitor, DadosDoTermo termo, LocalDate hoje) {
        LocalDate inicio = inicioDaVigencia(monitor, termo);
        LocalDate fim = fimDaVigencia(monitor, termo);
        return fim != null && !hoje.isAfter(fim) && (inicio == null || !hoje.isBefore(inicio));
    }

    private void preencher(HorarioAtendimento horario, HorarioRequest request) {
        horario.setDiaSemana(request.getDiaSemana());
        horario.setInicio(request.getInicio());
        horario.setFim(request.getFim());
        horario.setLocal(request.getLocal().trim());
    }

    private void validarIntervalo(HorarioRequest request) {
        if (request.getFim().compareTo(request.getInicio()) <= 0) {
            throw new CampoInvalidoException("fim", "O fim precisa ser depois do início");
        }
    }

    // dois horários do mesmo monitor no mesmo dia não podem se sobrepor
    private void validarConflito(String monitorId, HorarioRequest request, String ignorarId) {
        boolean conflita = horarioRepository.findByMonitorIdAndDiaSemana(monitorId, request.getDiaSemana()).stream()
                .filter(h -> !h.getId().equals(ignorarId))
                .anyMatch(h -> request.getInicio().compareTo(h.getFim()) < 0
                        && request.getFim().compareTo(h.getInicio()) > 0);

        if (conflita) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um horário seu neste intervalo");
        }
    }

    // mesma regra do perfil: só depois da aprovação de conta e agência
    private void exigirPerfilLiberado(Monitor monitor) {
        if (monitor.getStatusContaAgencia() != StatusDocumento.APROVADO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Os horários só podem ser publicados após a aprovação da conta e agência");
        }
    }

    // horário de outro monitor responde 404, como se não existisse
    private HorarioAtendimento buscarDoMonitor(Monitor monitor, String id) {
        return horarioRepository.findById(id)
                .filter(h -> monitor.getId().equals(h.getMonitorId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horário não encontrado"));
    }

    private Monitor buscarMonitor(String email) {
        return monitorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Monitor não encontrado para o usuário autenticado"));
    }
}