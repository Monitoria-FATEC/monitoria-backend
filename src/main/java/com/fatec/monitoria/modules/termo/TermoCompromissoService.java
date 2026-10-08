package com.fatec.monitoria.modules.termo;

import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.modules.termo.dto.AtualizarTermoRequest;
import com.fatec.monitoria.modules.termo.dto.TermoCompromissoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TermoCompromissoService {

    private final TermoCompromissoRepository repository;
    private final MonitorRepository monitorRepository;

    public TermoCompromisso enviar(TermoCompromissoRequest request) {
        TermoCompromisso termo = new TermoCompromisso();
        BeanUtils.copyProperties(request, termo);
        termo.setStatus(StatusDocumento.AGUARDANDO);
        termo.setDataEnvio(LocalDateTime.now());
        return repository.save(termo);
    }

    public TermoCompromisso atualizar(String email, String id, AtualizarTermoRequest request) {
        Monitor monitor = monitorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado"));
        TermoCompromisso termo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Termo não encontrado"));
        if (!monitor.getRa().equals(termo.getRa())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse termo não pertence ao monitor autenticado");
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
        return repository.save(termo);
    }
}
