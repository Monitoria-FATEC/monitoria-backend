package com.fatec.monitoria.modules.horario.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HorarioAtendimentoRepository extends MongoRepository<HorarioAtendimento, String> {

    List<HorarioAtendimento> findByMonitorId(String monitorId);

    List<HorarioAtendimento> findByMonitorIdAndDiaSemana(String monitorId, Integer diaSemana);

    void deleteByMonitorId(String monitorId);
}