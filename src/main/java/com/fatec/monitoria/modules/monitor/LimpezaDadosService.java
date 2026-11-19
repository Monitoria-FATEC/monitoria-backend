package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.InscricaoRepository;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.dto.LimpezaDadosResponse;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Acha (e, se pedido, apaga) dados que ficaram para trás quando um monitor foi removido:
// inscrições sem monitor e termos que não pertencem a ninguém.
// Nunca apaga dados de monitor que existe: inscrição sem termo e monitor sem inscrição só são listados.
@Slf4j
@Service
@RequiredArgsConstructor
public class LimpezaDadosService {

    private final MonitorRepository monitorRepository;
    private final InscricaoRepository inscricaoRepository;
    private final TermoCompromissoRepository termoRepository;

    // Só mostra o que seria apagado.
    public LimpezaDadosResponse simular() {
        return analisar(false, "ninguém");
    }

    // Apaga as inscrições sem monitor e os termos sem dono.
    public LimpezaDadosResponse executar(String quemExecutou) {
        return analisar(true, quemExecutou);
    }

    private LimpezaDadosResponse analisar(boolean apagar, String quem) {
        List<Monitor> monitores = monitorRepository.findAll();
        Set<String> idsMonitores = new HashSet<>();
        Set<String> rasMonitores = new HashSet<>();
        for (Monitor monitor : monitores) {
            idsMonitores.add(monitor.getId());
            if (monitor.getRa() != null) {
                rasMonitores.add(monitor.getRa());
            }
        }

        List<Inscricao> inscricoes = inscricaoRepository.findAll();
        List<TermoCompromisso> termos = termoRepository.findAll();
        Set<String> idsTermos = new HashSet<>();
        termos.forEach(termo -> idsTermos.add(termo.getId()));

        // 1) inscrições cujo monitor não existe
        List<Inscricao> inscricoesOrfas = inscricoes.stream()
                .filter(inscricao -> inscricao.getIdMonitor() == null || !idsMonitores.contains(inscricao.getIdMonitor()))
                .toList();

        // 2) inscrições que continuam valendo
        List<Inscricao> inscricoesValidas = inscricoes.stream()
                .filter(inscricao -> inscricao.getIdMonitor() != null && idsMonitores.contains(inscricao.getIdMonitor()))
                .toList();
        Set<String> termosEmUso = new HashSet<>();
        inscricoesValidas.forEach(inscricao -> {
            if (inscricao.getIdTermoCompromisso() != null) {
                termosEmUso.add(inscricao.getIdTermoCompromisso());
            }
        });

        // 3) termos sem dono: nenhuma inscrição válida os usa e nenhum monitor existente tem o RA deles
        List<TermoCompromisso> termosOrfaos = termos.stream()
                .filter(termo -> !termosEmUso.contains(termo.getId()))
                .filter(termo -> termo.getRa() == null || !rasMonitores.contains(termo.getRa()))
                .toList();

        // só listados
        List<String> semTermo = inscricoesValidas.stream()
                .filter(inscricao -> inscricao.getIdTermoCompromisso() == null
                        || !idsTermos.contains(inscricao.getIdTermoCompromisso()))
                .map(inscricao -> nomeDoMonitor(monitores, inscricao.getIdMonitor()) + " (" + inscricao.getStatus() + ")")
                .toList();

        Set<String> monitoresComInscricao = new HashSet<>();
        inscricoesValidas.forEach(inscricao -> monitoresComInscricao.add(inscricao.getIdMonitor()));
        List<String> semInscricao = monitores.stream()
                .filter(monitor -> !monitoresComInscricao.contains(monitor.getId()))
                .map(monitor -> monitor.getNome() + " <" + monitor.getEmail() + ">")
                .toList();

        List<String> resumoInscricoes = inscricoesOrfas.stream()
                .map(inscricao -> inscricao.getId() + " (" + inscricao.getStatus() + ")")
                .toList();
        List<String> resumoTermos = termosOrfaos.stream()
                .map(termo -> termo.getId() + " (" + termo.getNomeEstudante() + ", RA " + termo.getRa() + ")")
                .toList();

        if (apagar) {
            inscricaoRepository.deleteAll(inscricoesOrfas);
            termoRepository.deleteAll(termosOrfaos);
            log.warn("[LIMPEZA] {} apagou {} inscrição(ões) sem monitor e {} termo(s) sem dono",
                    quem, inscricoesOrfas.size(), termosOrfaos.size());
        }

        return new LimpezaDadosResponse(apagar, resumoInscricoes, resumoTermos, semTermo, semInscricao);
    }

    private String nomeDoMonitor(List<Monitor> monitores, String id) {
        return monitores.stream()
                .filter(monitor -> monitor.getId().equals(id))
                .map(Monitor::getNome)
                .findFirst()
                .orElse("?");
    }
}