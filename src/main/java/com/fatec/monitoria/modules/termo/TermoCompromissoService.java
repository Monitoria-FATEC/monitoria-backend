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
import java.time.format.DateTimeParseException;
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

        // obrigatórios
        termo.setDisciplina(request.getDisciplina().trim());
        termo.setOferta(request.getOferta().trim());
        termo.setCargaHoraria(request.getCargaHoraria());

        // datas (o RA nunca muda)
        if (naoVazio(request.getPeriodoInicio())) {
            termo.setPeriodoInicio(lerData(request.getPeriodoInicio(), "início do período"));
        }
        if (naoVazio(request.getPeriodoFim())) {
            termo.setPeriodoFim(lerData(request.getPeriodoFim(), "fim do período"));
        }
        if (termo.getPeriodoInicio() != null && termo.getPeriodoFim() != null
                && termo.getPeriodoFim().isBefore(termo.getPeriodoInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O fim do período não pode ser antes do início");
        }
        if (naoVazio(request.getDataAssinatura())) {
            termo.setDataAssinatura(lerData(request.getDataAssinatura(), "data de assinatura"));
        }

        // opcionais: só troca quando vier preenchido
        if (naoVazio(request.getNomeEstudante())) termo.setNomeEstudante(request.getNomeEstudante().trim());
        if (naoVazio(request.getCpf())) termo.setCpf(request.getCpf().trim());
        if (naoVazio(request.getCurso())) termo.setCurso(request.getCurso().trim());
        if (naoVazio(request.getEditalNumero())) termo.setEditalNumero(request.getEditalNumero().trim());
        if (naoVazio(request.getNomeProfessor())) termo.setNomeProfessor(request.getNomeProfessor().trim());
        if (naoVazio(request.getNomeCoordenador())) termo.setNomeCoordenador(request.getNomeCoordenador().trim());
        if (naoVazio(request.getUnidade())) termo.setUnidade(request.getUnidade().trim());
        if (naoVazio(request.getCidade())) termo.setCidade(request.getCidade().trim());
        if (request.getNumeroVias() != null) termo.setNumeroVias(request.getNumeroVias());
        if (naoVazio(request.getAssinaturaEstudante())) termo.setAssinaturaEstudante(request.getAssinaturaEstudante());

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

    private boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }

    private LocalDate lerData(String valor, String campo) {
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Data inválida em " + campo + " (use AAAA-MM-DD)");
        }
    }
}