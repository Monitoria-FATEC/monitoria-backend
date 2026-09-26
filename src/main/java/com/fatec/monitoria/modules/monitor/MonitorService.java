package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusInscricao;
import com.fatec.monitoria.modules.monitor.dto.AtualizarStatusRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorCadastroRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorService {

    private final MonitorRepository repository;

    public Monitor cadastrar(MonitorCadastroRequest request) {
        Monitor monitor = new Monitor();
        monitor.setNome(request.getNome());
        monitor.setRa(request.getRa());
        monitor.setEmail(request.getEmail());
        monitor.setCurso(request.getCurso());
        monitor.setStatus(StatusInscricao.AGUARDANDO_SUPERVISOR);
        monitor.setDataEnvio(LocalDateTime.now());

        return repository.save(monitor);
    }

    
    public List<Monitor> listarTodos() {

        return repository.findAll();
    }


    public Monitor atualizarStatus(String id, AtualizarStatusRequest request) {
        Monitor monitor = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado"));

        monitor.setStatus(request.getStatus());
        return repository.save(monitor);
    }
}