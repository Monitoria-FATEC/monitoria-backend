package com.fatec.monitoria.modules.monitor.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface MonitorRepository extends MongoRepository<Monitor, String> {

    Optional<Monitor> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRa(String ra);

    // telefone já usado por outro monitor (recebe as duas grafias: com e sem máscara)
    boolean existsByTelefoneInAndIdNot(Collection<String> telefones, String id);
}