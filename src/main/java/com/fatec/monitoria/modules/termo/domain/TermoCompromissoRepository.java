package com.fatec.monitoria.modules.termo.domain;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TermoCompromissoRepository extends MongoRepository<TermoCompromisso, String> {
    Optional<TermoCompromisso> findFirstByRaOrderByDataEnvioDesc(String ra);
}
