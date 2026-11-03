package com.fatec.monitoria.modules.atendimento.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RegistroAtendimentoRepository extends MongoRepository<RegistroAtendimento, String> {

    List<RegistroAtendimento> findByMonitorId(String monitorId);

    void deleteByMonitorId(String monitorId);
}