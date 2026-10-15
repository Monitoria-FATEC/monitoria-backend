package com.fatec.monitoria.modules.inscricao;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.InscricaoRepository;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.inscricao.dto.InscricaoDetalheResponse;
import com.fatec.monitoria.modules.inscricao.dto.InscricaoRequest;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscricaoService {

    private final InscricaoRepository repository;
    private final MonitorRepository monitorRepository;
    private final TermoCompromissoRepository termoRepository;

    public Inscricao submeter(InscricaoRequest request) {
        Inscricao inscricao = new Inscricao();
        inscricao.setIdMonitor(request.getIdMonitor());
        inscricao.setIdTermoCompromisso(request.getIdTermoCompromisso());
        return repository.save(inscricao);
    }

    public List<Inscricao> listarPorStatus(StatusInscricao status) {
        if (status == null) {
            return repository.findAll();
        }
        return repository.findByStatus(status);
    }

    // Lista as inscrições já com os dados do monitor e do termo (para o painel).
    public List<InscricaoDetalheResponse> listarDetalhado(StatusInscricao status) {
        List<Inscricao> inscricoes = listarPorStatus(status);

        List<String> idsMonitor = inscricoes.stream()
                .map(Inscricao::getIdMonitor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> idsTermo = inscricoes.stream()
                .map(Inscricao::getIdTermoCompromisso)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, Monitor> monitores = monitorRepository.findAllById(idsMonitor).stream()
                .collect(Collectors.toMap(Monitor::getId, Function.identity()));

        Map<String, TermoCompromisso> termos = termoRepository.findAllById(idsTermo).stream()
                .collect(Collectors.toMap(TermoCompromisso::getId, Function.identity()));

        return inscricoes.stream()
                .sorted(Comparator.comparing(Inscricao::getDataSubmissao,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(i -> InscricaoDetalheResponse.of(
                        i,
                        monitores.get(i.getIdMonitor()),
                        termos.get(i.getIdTermoCompromisso())))
                .toList();
    }

    public Inscricao aprovar(String id) {
        Inscricao inscricao = buscarOuFalhar(id);
        inscricao.setStatus(StatusInscricao.AGUARDANDO_GESTAO);
        inscricao.setDataAtualizacao(LocalDateTime.now());
        return repository.save(inscricao);
    }

    // dataLimite é opcional: prazo que o supervisor dá para o aluno corrigir a ficha.
    public Inscricao devolver(String id, String justificativa, LocalDate dataLimite) {
        Inscricao inscricao = buscarOuFalhar(id);
        inscricao.setStatus(StatusInscricao.DEVOLVIDA);
        inscricao.setJustificativaDevolucao(justificativa);
        inscricao.setDataLimiteCorrecao(dataLimite);
        // se já tinha sido marcada como enviada para assinatura, o marcador é zerado
        inscricao.setEnviadoParaAssinatura(false);
        inscricao.setDataEnvioAssinatura(null);
        inscricao.setDataAtualizacao(LocalDateTime.now());
        return repository.save(inscricao);
    }

    // Gestão avisa que baixou o PDF e enviou para assinatura.
    // Para o aluno o status continua AGUARDANDO_GESTAO.
    public Inscricao enviarParaAssinatura(String id) {
        Inscricao inscricao = buscarOuFalhar(id);
        exigirStatus(inscricao, StatusInscricao.AGUARDANDO_GESTAO);
        inscricao.setEnviadoParaAssinatura(true);
        inscricao.setDataEnvioAssinatura(LocalDateTime.now());
        inscricao.setDataAtualizacao(LocalDateTime.now());
        return repository.save(inscricao);
    }

    // Gestão clica em "homologado" depois que o termo foi assinado fora do sistema.
    public Inscricao homologar(String id, String assinaturaGestao) {
        Inscricao inscricao = buscarOuFalhar(id);
        exigirStatus(inscricao, StatusInscricao.AGUARDANDO_GESTAO);
        inscricao.setStatus(StatusInscricao.HOMOLOGADA);
        inscricao.setAssinaturaGestao(assinaturaGestao);
        inscricao.setDataAtualizacao(LocalDateTime.now());
        return repository.save(inscricao);
    }

    private Inscricao buscarOuFalhar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Inscrição não encontrada: " + id));
    }

    private void exigirStatus(Inscricao inscricao, StatusInscricao esperado) {
        if (inscricao.getStatus() != esperado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Operação inválida: a inscrição está em " + inscricao.getStatus()
                            + " e precisa estar em " + esperado);
        }
    }
}