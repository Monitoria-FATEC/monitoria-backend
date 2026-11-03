package com.fatec.monitoria.modules.inscricao;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.InscricaoRepository;
import com.fatec.monitoria.modules.inscricao.domain.RegistroHistorico;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.inscricao.dto.InscricaoDetalheResponse;
import com.fatec.monitoria.modules.inscricao.dto.InscricaoRequest;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import com.fatec.monitoria.modules.usuario.domain.Role;
import com.fatec.monitoria.modules.usuario.domain.Usuario;
import com.fatec.monitoria.modules.usuario.domain.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscricaoService {

    private final InscricaoRepository repository;
    private final MonitorRepository monitorRepository;
    private final TermoCompromissoRepository termoRepository;
    private final UsuarioRepository usuarioRepository;

    // Envio do termo pelo monitor. Confere se monitor e termo existem, se o termo é dele
    // e se ele já não tem uma inscrição em andamento.
    public Inscricao submeter(InscricaoRequest request, String emailAutor) {
        Usuario autor = buscarAutor(emailAutor);

        Monitor monitor = monitorRepository.findById(request.getIdMonitor())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado"));

        TermoCompromisso termo = termoRepository.findById(request.getIdTermoCompromisso())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Termo de compromisso não encontrado"));

        // o monitor só inscreve a si mesmo
        if (autor.getRole() == Role.MONITOR
                && (monitor.getEmail() == null || !monitor.getEmail().equalsIgnoreCase(autor.getEmail()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode enviar a sua própria inscrição");
        }

        if (monitor.getRa() == null || !monitor.getRa().equals(termo.getRa())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O termo não pertence a este monitor");
        }

        boolean jaTemInscricao = repository.findByIdMonitor(monitor.getId()).stream()
                .anyMatch(i -> i.getStatus() != StatusInscricao.RECUSADA);
        if (jaTemInscricao) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este monitor já tem uma inscrição em andamento");
        }

        Inscricao inscricao = new Inscricao();
        inscricao.setIdMonitor(monitor.getId());
        inscricao.setIdTermoCompromisso(termo.getId());
        registrar(inscricao, null, StatusInscricao.AGUARDANDO_SUPERVISOR, "ENVIO_DO_TERMO", autor, null);
        return repository.save(inscricao);
    }

    public List<Inscricao> listarPorStatus(StatusInscricao status) {
        if (status == null) {
            return repository.findAll();
        }
        return repository.findByStatus(status);
    }

    // Lista as inscrições já com os dados do monitor e do termo (para o painel).
    public List<InscricaoDetalheResponse> listarDetalhado(StatusInscricao status) {
        List<Inscricao> inscricoes = listarPorStatus(status);

        List<String> idsMonitor = inscricoes.stream()
                .map(Inscricao::getIdMonitor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> idsTermo = inscricoes.stream()
                .map(Inscricao::getIdTermoCompromisso)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, Monitor> monitores = monitorRepository.findAllById(idsMonitor).stream()
                .collect(Collectors.toMap(Monitor::getId, Function.identity()));

        Map<String, TermoCompromisso> termos = termoRepository.findAllById(idsTermo).stream()
                .collect(Collectors.toMap(TermoCompromisso::getId, Function.identity()));

        return inscricoes.stream()
                .sorted(Comparator.comparing(Inscricao::getDataSubmissao,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(i -> InscricaoDetalheResponse.of(
                        i,
                        monitores.get(i.getIdMonitor()),
                        termos.get(i.getIdTermoCompromisso())))
                .toList();
    }

    // Supervisor aprova a ficha e ela segue para a assinatura da Gestão.
    public Inscricao aprovar(String id, String emailAutor) {
        Usuario autor = buscarAutor(emailAutor);
        Inscricao inscricao = buscarOuFalhar(id);
        exigirStatus(inscricao, StatusInscricao.AGUARDANDO_SUPERVISOR);

        StatusInscricao anterior = inscricao.getStatus();
        inscricao.setStatus(StatusInscricao.AGUARDANDO_GESTAO);
        inscricao.setIdSupervisor(autor.getId());
        atualizarStatusTermo(inscricao, StatusDocumento.APROVADO, null);
        registrar(inscricao, anterior, inscricao.getStatus(), "APROVACAO", autor, null);
        return repository.save(inscricao);
    }

    // dataLimite é opcional: prazo que o supervisor dá para o aluno corrigir a ficha.
    public Inscricao devolver(String id, String justificativa, LocalDate dataLimite, String emailAutor) {
        Usuario autor = buscarAutor(emailAutor);
        Inscricao inscricao = buscarOuFalhar(id);
        exigirStatus(inscricao, StatusInscricao.AGUARDANDO_SUPERVISOR, StatusInscricao.AGUARDANDO_GESTAO);

        StatusInscricao anterior = inscricao.getStatus();
        inscricao.setStatus(StatusInscricao.DEVOLVIDA);
        inscricao.setJustificativaDevolucao(justificativa);
        inscricao.setDataLimiteCorrecao(dataLimite);
        // se já tinha sido marcada como enviada para assinatura, o marcador é zerado
        inscricao.setEnviadoParaAssinatura(false);
        inscricao.setDataEnvioAssinatura(null);
        atualizarStatusTermo(inscricao, StatusDocumento.DEVOLVIDO, justificativa);
        registrar(inscricao, anterior, inscricao.getStatus(), "DEVOLUCAO", autor, justificativa);
        return repository.save(inscricao);
    }

    // O supervisor avisa que baixou o PDF e enviou à Gestão para assinatura.
    // Para o aluno o status continua AGUARDANDO_GESTAO.
    public Inscricao enviarParaAssinatura(String id, String emailAutor) {
        Usuario autor = buscarAutor(emailAutor);
        Inscricao inscricao = buscarOuFalhar(id);
        exigirStatus(inscricao, StatusInscricao.AGUARDANDO_GESTAO);

        inscricao.setEnviadoParaAssinatura(true);
        inscricao.setDataEnvioAssinatura(LocalDateTime.now());
        registrar(inscricao, inscricao.getStatus(), inscricao.getStatus(), "ENVIO_PARA_ASSINATURA", autor, null);
        return repository.save(inscricao);
    }

    // O supervisor registra a homologação depois que o termo voltou assinado pela Gestão (fora do sistema).
    public Inscricao homologar(String id, String assinaturaGestao, String emailAutor) {
        Usuario autor = buscarAutor(emailAutor);
        Inscricao inscricao = buscarOuFalhar(id);
        exigirStatus(inscricao, StatusInscricao.AGUARDANDO_GESTAO);

        StatusInscricao anterior = inscricao.getStatus();
        inscricao.setStatus(StatusInscricao.HOMOLOGADA);
        inscricao.setAssinaturaGestao(assinaturaGestao);
        atualizarStatusTermo(inscricao, StatusDocumento.APROVADO, null);
        registrar(inscricao, anterior, inscricao.getStatus(), "HOMOLOGACAO", autor, null);
        return repository.save(inscricao);
    }

    // Acrescenta uma linha ao histórico e atualiza a data da última alteração.
    private void registrar(Inscricao inscricao, StatusInscricao anterior, StatusInscricao novo,
                           String acao, Usuario autor, String justificativa) {
        RegistroHistorico registro = new RegistroHistorico();
        registro.setAcao(acao);
        registro.setStatusAnterior(anterior);
        registro.setStatusNovo(novo);
        registro.setIdUsuario(autor.getId());
        registro.setNomeUsuario(autor.getNome());
        registro.setPerfil(autor.getRole().name());
        registro.setJustificativa(justificativa);
        registro.setData(LocalDateTime.now());
        inscricao.getHistorico().add(registro);
        inscricao.setDataAtualizacao(LocalDateTime.now());
    }

    private Usuario buscarAutor(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));
    }

    private Inscricao buscarOuFalhar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Inscrição não encontrada: " + id));
    }

    private void exigirStatus(Inscricao inscricao, StatusInscricao... permitidos) {
        for (StatusInscricao permitido : permitidos) {
            if (inscricao.getStatus() == permitido) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Operação inválida: a inscrição está em " + inscricao.getStatus()
                        + " e precisa estar em " + (permitidos.length == 1
                        ? permitidos[0] : Arrays.toString(permitidos)));
    }

    // Mantém o status do termo (mostrado no dashboard do monitor) em sincronia com a inscrição.
    private void atualizarStatusTermo(Inscricao inscricao, StatusDocumento status, String justificativa) {
        if (inscricao.getIdTermoCompromisso() == null) {
            return;
        }
        termoRepository.findById(inscricao.getIdTermoCompromisso()).ifPresent(termo -> {
            termo.setStatus(status);
            termo.setJustificativaDevolucao(justificativa);
            termoRepository.save(termo);
        });
    }
}