package com.fatec.monitoria.modules.inscricao.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InscricaoRepository extends MongoRepository<Inscricao, String> {

    List<Inscricao> findByStatus(StatusInscricao status);

    List<Inscricao> findByIdMonitor(String idMonitor);
}