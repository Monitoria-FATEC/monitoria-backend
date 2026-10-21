package com.fatec.monitoria.modules.termo.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TermoCompromissoRepository extends MongoRepository<TermoCompromisso, String> {

    Optional<TermoCompromisso> findFirstByRaOrderByDataEnvioDesc(String ra);

    List<TermoCompromisso> findByRa(String ra);

    // CPF já usado em termo de outro aluno (recebe as duas grafias: com e sem pontuação)
    boolean existsByCpfInAndRaNot(Collection<String> cpfs, String ra);
}