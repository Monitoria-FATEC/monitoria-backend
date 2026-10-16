package com.fatec.monitoria.modules.termo;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.InscricaoRepository;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import com.fatec.monitoria.modules.termo.dto.AtualizarTermoRequest;
import com.fatec.monitoria.modules.termo.dto.TermoCompromissoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class TermoCompromissoService {

    private final TermoCompromissoRepository repository;
    private final MonitorRepository monitorRepository;
    private final InscricaoRepository inscricaoRepository;

    public TermoCompromisso enviar(TermoCompromissoRequest request) {
        TermoCompromisso termo = new TermoCompromisso();
        BeanUtils.copyProperties(request, termo);
        termo.setStatus(StatusDocumento.AGUARDANDO);
        termo.setDataEnvio(LocalDateTime.now());
        return repository.save(termo);
    }

    // Monitor corrige o termo devolvido e reenvia: a inscrição volta para o supervisor.
    public TermoCompromisso atualizar(String email, String id, AtualizarTermoRequest request) {
        Monitor monitor = monitorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado"));
        TermoCompromisso termo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Termo não encontrado"));
        if (!monitor.getRa().equals(termo.getRa())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse termo não pertence ao monitor autenticado");
        }

        // inscrição mais recente ligada a esse termo
        Inscricao inscricao = inscricaoRepository.findByIdTermoCompromisso(id).stream()
                .max(Comparator.comparing(Inscricao::getDataSubmissao,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
        if (inscricao != null && inscricao.getStatus() != StatusInscricao.DEVOLVIDA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O termo só pode ser editado quando a inscrição foi devolvida (status atual: "
                            + inscricao.getStatus() + ")");
        }

        termo.setDisciplina(request.getDisciplina());
        termo.setOferta(request.getOferta());
        termo.setCargaHoraria(request.getCargaHoraria());
        if (request.getPeriodoInicio() != null && !request.getPeriodoInicio().isBlank()) {
            termo.setPeriodoInicio(LocalDate.parse(request.getPeriodoInicio()));
        }
        if (request.getPeriodoFim() != null && !request.getPeriodoFim().isBlank()) {
            termo.setPeriodoFim(LocalDate.parse(request.getPeriodoFim()));
        }
        termo.setStatus(StatusDocumento.AGUARDANDO);
        termo.setJustificativaDevolucao(null);
        termo.setDataEnvio(LocalDateTime.now());
        TermoCompromisso salvo = repository.save(termo);

        // devolve a inscrição para o supervisor conferir de novo
        if (inscricao != null) {
            inscricao.setStatus(StatusInscricao.AGUARDANDO_SUPERVISOR);
            inscricao.setJustificativaDevolucao(null);
            inscricao.setDataLimiteCorrecao(null);
            inscricao.setDataAtualizacao(LocalDateTime.now());
            inscricaoRepository.save(inscricao);
        }
        return salvo;
    }
}