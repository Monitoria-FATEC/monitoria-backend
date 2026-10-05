package com.fatec.monitoria.modules.inscricao;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.InscricaoRepository;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.inscricao.dto.InscricaoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscricaoService {

    private final InscricaoRepository repository;

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

    public Inscricao aprovar(String id) {
        Inscricao inscricao = buscarOuFalhar(id);
        inscricao.setStatus(StatusInscricao.AGUARDANDO_GESTAO);
        inscricao.setDataAtualizacao(LocalDateTime.now());
        return repository.save(inscricao);
    }

    public Inscricao devolver(String id, String justificativa) {
        Inscricao inscricao = buscarOuFalhar(id);
        inscricao.setStatus(StatusInscricao.DEVOLVIDA);
        inscricao.setJustificativaDevolucao(justificativa);
        inscricao.setDataAtualizacao(LocalDateTime.now());
        return repository.save(inscricao);
    }

    public Inscricao homologar(String id, String assinaturaGestao) {
        Inscricao inscricao = buscarOuFalhar(id);
        inscricao.setStatus(StatusInscricao.HOMOLOGADA);
        inscricao.setAssinaturaGestao(assinaturaGestao);
        inscricao.setDataAtualizacao(LocalDateTime.now());
        return repository.save(inscricao);
    }

    private Inscricao buscarOuFalhar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada: " + id));
    }
}