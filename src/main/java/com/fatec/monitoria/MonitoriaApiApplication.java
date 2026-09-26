package com.fatec.monitoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonitoriaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoriaApiApplication.class, args);
    }

}
/*
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusInscricao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MonitoriaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoriaApiApplication.class, args);
    }

    @Bean
    CommandLineRunner testarConexao(MonitorRepository repo) {
        return args -> {
            Monitor m = new Monitor();
            m.setNome("Teste de Conexão");
            m.setStatus(StatusInscricao.PENDENTE);
            repo.save(m);
            System.out.println("✅ Salvo! Total de monitores: " + repo.count());
        };
    }

}*/
