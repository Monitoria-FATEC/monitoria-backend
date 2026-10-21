package com.fatec.monitoria.modules.monitor;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.InscricaoRepository;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.dto.ExclusaoMonitorResponse;
import com.fatec.monitoria.modules.monitor.dto.MonitorCadastroRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorCriadoResponse;
import com.fatec.monitoria.modules.monitor.dto.MonitorGestaoRequest;
import com.fatec.monitoria.modules.monitor.dto.MonitorGestaoResponse;
import com.fatec.monitoria.modules.monitor.dto.SenhaTemporariaResponse;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import com.fatec.monitoria.modules.usuario.domain.Role;
import com.fatec.monitoria.modules.usuario.domain.Usuario;
import com.fatec.monitoria.modules.usuario.domain.UsuarioRepository;
import com.fatec.monitoria.validacao.DisponibilidadeCadastroService;
import com.fatec.monitoria.validacao.Regras;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorGestaoService {

    // letras e números sem os que se confundem (0/O, 1/l/I)
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
    private static final String NUMEROS = "23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MonitorRepository monitorRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscricaoRepository inscricaoRepository;
    private final TermoCompromissoRepository termoRepository;
    private final MonitorService monitorService;
    private final DisponibilidadeCadastroService disponibilidade;
    private final PasswordEncoder passwordEncoder;

    // Lista todos os monitores (ativos e inativos) em ordem alfabética
    public List<MonitorGestaoResponse> listar() {
        return monitorRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        (Monitor m) -> m.getNome() == null ? "" : m.getNome(),
                        String.CASE_INSENSITIVE_ORDER))
                .map(MonitorGestaoResponse::fromEntity)
                .toList();
    }

    // Cadastro manual feito pelo supervisor: cria a conta de acesso (com senha temporária)
    // e o monitor, com as mesmas regras do pré-cadastro (RA e e-mail únicos, formatos válidos).
    public MonitorCriadoResponse criar(MonitorCadastroRequest request, String quemCriou) {
        String nome = Regras.normalizarNome(request.getNome());
        String email = Regras.normalizarEmail(request.getEmail());
        String ra = Regras.normalizarTexto(request.getRa());

        // junta todos os conflitos (e-mail e RA) para o supervisor corrigir de uma vez
        disponibilidade.exigirDisponiveis(email, ra, true);

        String senhaTemporaria = gerarSenhaTemporaria();

        // se sobrou uma conta abandonada (e-mail nunca confirmado) com este endereço, ela é refeita
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .filter(Usuario::isEmailPendenteVerificacao)
                .orElseGet(Usuario::new);

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senhaTemporaria));
        usuario.setSenhaTemporaria(true);
        usuario.setRole(Role.MONITOR);
        usuario.setAtivo(true);
        // quem cadastra é o supervisor, então não há código de e-mail para confirmar
        usuario.setEmailPendenteVerificacao(false);
        usuario.setCodigoVerificacaoHash(null);
        usuario.setExpiracaoCodigoVerificacao(null);
        usuario = usuarioRepository.save(usuario);

        Monitor monitor;
        try {
            monitor = monitorService.cadastrar(request);
        } catch (RuntimeException erro) {
            // não deixa uma conta sem monitor para trás
            usuarioRepository.delete(usuario);
            throw erro;
        }

        log.info("[CADASTRO MANUAL] {} cadastrou o monitor '{}' (RA {})", quemCriou, monitor.getNome(), monitor.getRa());

        return new MonitorCriadoResponse(MonitorGestaoResponse.fromEntity(monitor), senhaTemporaria);
    }

    // Gera outra senha temporária (monitor que perdeu a senha ou supervisor que não anotou a primeira).
    // A senha antiga deixa de valer e o monitor é obrigado a trocar no próximo acesso.
    public SenhaTemporariaResponse gerarNovaSenha(String id, String quemGerou) {
        Monitor monitor = buscar(id);

        Usuario usuario = monitor.getEmail() == null
                ? null
                : usuarioRepository.findByEmailIgnoreCase(monitor.getEmail())
                .filter(conta -> conta.getRole() == Role.MONITOR)
                .orElse(null);

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Este monitor não tem conta de acesso para receber uma senha");
        }

        String senhaTemporaria = gerarSenhaTemporaria();

        usuario.setSenha(passwordEncoder.encode(senhaTemporaria));
        usuario.setSenhaTemporaria(true);
        // o supervisor está entregando o acesso, então não precisa mais do código de e-mail
        usuario.setEmailPendenteVerificacao(false);
        usuarioRepository.save(usuario);

        log.warn("[SENHA TEMPORARIA] {} gerou uma nova senha temporária para o monitor '{}'", quemGerou, monitor.getNome());

        return new SenhaTemporariaResponse(usuario.getEmail(), senhaTemporaria);
    }

    // Edita nome, e-mail, curso, conta e agência. O RA não muda (o termo é ligado a ele).
    public MonitorGestaoResponse atualizar(String id, MonitorGestaoRequest request) {
        Monitor monitor = buscar(id);

        String emailAntigo = monitor.getEmail();
        String emailNovo = request.getEmail().trim().toLowerCase();
        boolean emailMudou = emailAntigo == null || !emailAntigo.equalsIgnoreCase(emailNovo);

        if (emailMudou) {
            exigirEmailLivre(emailNovo, monitor.getId());
        }

        // O login é ligado ao monitor pelo e-mail: mantém os dois em sincronia
        Usuario usuario = emailAntigo == null
                ? null
                : usuarioRepository.findByEmailIgnoreCase(emailAntigo).orElse(null);
        if (usuario != null) {
            usuario.setEmail(emailNovo);
            usuario.setNome(request.getNome().trim());
            usuarioRepository.save(usuario);
        }

        monitor.setNome(request.getNome().trim());
        monitor.setEmail(emailNovo);
        monitor.setCurso(request.getCurso().trim());
        monitor.setNumeroConta(vazioParaNulo(request.getNumeroConta()));
        monitor.setAgencia(vazioParaNulo(request.getAgencia()));

        return MonitorGestaoResponse.fromEntity(monitorRepository.save(monitor));
    }

    // Ativa ou desativa o perfil (não apaga nada)
    public MonitorGestaoResponse alterarAtivo(String id, boolean ativo) {
        Monitor monitor = buscar(id);
        monitor.setPerfilAtivo(ativo);
        return MonitorGestaoResponse.fromEntity(monitorRepository.save(monitor));
    }

    // Mostra o que a exclusão vai apagar, sem apagar nada (o front usa no aviso de confirmação).
    public ExclusaoMonitorResponse resumirExclusao(String id) {
        return planejarExclusao(buscar(id)).resumo();
    }

    // Exclusão definitiva: inscrições, termos, conta de login e, por último, o monitor.
    // O monitor sai por último para que, se algo falhar no meio, dê para tentar de novo.
    public ExclusaoMonitorResponse excluir(String id, String quemExcluiu) {
        PlanoDeExclusao plano = planejarExclusao(buscar(id));
        ExclusaoMonitorResponse resumo = plano.resumo();

        inscricaoRepository.deleteAll(plano.inscricoes());
        termoRepository.deleteAll(plano.termos());
        if (plano.conta() != null) {
            usuarioRepository.delete(plano.conta());
        }
        monitorRepository.delete(plano.monitor());

        log.warn("[EXCLUSAO] {} excluiu o monitor '{}' (RA {}): {} inscrição(ões), {} termo(s), conta de acesso: {}",
                quemExcluiu, resumo.nome(), resumo.ra(), resumo.inscricoes(), resumo.termos(),
                resumo.contaDeAcesso() ? "removida" : "não havia");

        return resumo;
    }

    private PlanoDeExclusao planejarExclusao(Monitor monitor) {
        List<Inscricao> inscricoes = inscricaoRepository.findByIdMonitor(monitor.getId());

        // termos ligados às inscrições do monitor
        Map<String, TermoCompromisso> termos = new LinkedHashMap<>();
        for (Inscricao inscricao : inscricoes) {
            if (inscricao.getIdTermoCompromisso() != null) {
                termoRepository.findById(inscricao.getIdTermoCompromisso())
                        .ifPresent(termo -> termos.put(termo.getId(), termo));
            }
        }

        // termos pelo RA, mas só quando o RA é só deste monitor (dados antigos podem ter RA repetido)
        if (monitor.getRa() != null && monitorRepository.countByRa(monitor.getRa()) <= 1) {
            termoRepository.findByRa(monitor.getRa()).forEach(termo -> termos.put(termo.getId(), termo));
        }

        // só apaga a conta de acesso se for de monitor (nunca a de um supervisor com o mesmo e-mail)
        Usuario conta = monitor.getEmail() == null
                ? null
                : usuarioRepository.findByEmailIgnoreCase(monitor.getEmail())
                .filter(usuario -> usuario.getRole() == Role.MONITOR)
                .orElse(null);

        return new PlanoDeExclusao(monitor, inscricoes, new ArrayList<>(termos.values()), conta);
    }

    private record PlanoDeExclusao(Monitor monitor, List<Inscricao> inscricoes,
                                   List<TermoCompromisso> termos, Usuario conta) {

        ExclusaoMonitorResponse resumo() {
            boolean homologado = inscricoes.stream()
                    .anyMatch(inscricao -> inscricao.getStatus() == StatusInscricao.HOMOLOGADA);

            return new ExclusaoMonitorResponse(
                    monitor.getNome(),
                    monitor.getRa(),
                    inscricoes.size(),
                    termos.size(),
                    conta != null,
                    homologado);
        }
    }

    private Monitor buscar(String id) {
        return monitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado"));
    }

    // 409 se o e-mail já pertence a outro monitor ou a outro login
    private void exigirEmailLivre(String email, String idDoMonitor) {
        monitorRepository.findByEmailIgnoreCase(email).ifPresent(outro -> {
            if (!outro.getId().equals(idDoMonitor)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um monitor com este e-mail");
            }
        });

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso por outra conta");
        }
    }

    // 10 caracteres: 7 letras e 3 números, embaralhados (atende à regra de senha do sistema)
    private String gerarSenhaTemporaria() {
        List<Character> caracteres = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            caracteres.add(LETRAS.charAt(RANDOM.nextInt(LETRAS.length())));
        }
        for (int i = 0; i < 3; i++) {
            caracteres.add(NUMEROS.charAt(RANDOM.nextInt(NUMEROS.length())));
        }
        Collections.shuffle(caracteres, RANDOM);

        StringBuilder senha = new StringBuilder();
        caracteres.forEach(senha::append);
        return senha.toString();
    }

    private String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}