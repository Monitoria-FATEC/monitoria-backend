package com.fatec.monitoria.modules.termo;

import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import com.fatec.monitoria.modules.termo.dto.TermoCompromissoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TermoCompromissoService {

    private final TermoCompromissoRepository repository;

    public TermoCompromisso enviar(TermoCompromissoRequest request) {
        TermoCompromisso termo = new TermoCompromisso();
        BeanUtils.copyProperties(request, termo);
        termo.setDataEnvio(LocalDateTime.now());
        return repository.save(termo);
    }
}