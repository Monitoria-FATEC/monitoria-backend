package com.fatec.monitoria.modules.termo;

import com.fatec.monitoria.modules.inscricao.domain.Inscricao;
import com.fatec.monitoria.modules.inscricao.domain.InscricaoRepository;
import com.fatec.monitoria.modules.inscricao.domain.StatusInscricao;
import com.fatec.monitoria.modules.monitor.domain.Monitor;
import com.fatec.monitoria.modules.monitor.domain.MonitorRepository;
import com.fatec.monitoria.modules.monitor.domain.StatusDocumento;
import com.fatec.monitoria.modules.termo.domain.TermoCompromisso;
import com.fatec.monitoria.modules.termo.domain.TermoCompromissoRepository;
import com.fatec.monitoria.modules.termo.dto.AtualizarTermoRequest;
import com.fatec.monitoria.modules.termo.dto.TermoCompromissoRequest;
import com.fatec.monitoria.validacao.CampoDuplicadoException;
import com.fatec.monitoria.validacao.CampoInvalidoException;
import com.fatec.monitoria.validacao.CpfValidator;
import com.fatec.monitoria.validacao.Regras;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TermoCompromissoService {

    // mesmas regras do front (validacoes.js)
    private static final int DIAS_MINIMOS_PERIODO = 30;
    private static final int DIAS_MAXIMOS_SEMESTRAL = 210;
    private static final int DIAS_MAXIMOS_ANUAL = 400;
    private static final int DIAS_MAXIMOS_ASSINATURA_ANTIGA = 365;

    private final TermoCompromissoRepository repository;
    private final MonitorRepository monitorRepository;
    private final InscricaoRepository inscricaoRepository;

    public TermoCompromisso enviar(TermoCompromissoRequest request) {
        String oferta = request.getOferta().trim().toLowerCase();

        Map<String, String> erros = new LinkedHashMap<>();
        validarDatas(erros, request.getPeriodoInicio(), request.getPeriodoFim(),
                request.getDataAssinatura(), oferta);
        validarEdital(erros, request.getEditalNumero());
        validarNomesDistintos(erros, request.getNomeEstudante(),
                request.getNomeProfessor(), request.getNomeCoordenador());
        lancarSeHouverErros(erros);

        String ra = Regras.normalizarTexto(request.getRa());
        String cpf = CpfValidator.formatar(request.getCpf());
        exigirCpfLivre(cpf, ra);

        TermoCompromisso termo = new TermoCompromisso();
        BeanUtils.copyProperties(request, termo);

        // grava tudo já limpo, sem espaços sobrando
        termo.setNomeEstudante(Regras.normalizarNome(request.getNomeEstudante()));
        termo.setRa(ra);
        termo.setCpf(cpf);
        termo.setCurso(Regras.normalizarTexto(request.getCurso()));
        termo.setDisciplina(Regras.normalizarTexto(request.getDisciplina()));
        termo.setOferta(oferta);
        termo.setEditalNumero(limparEdital(request.getEditalNumero()));
        termo.setNomeProfessor(Regras.normalizarNome(request.getNomeProfessor()));
        termo.setNomeCoordenador(Regras.normalizarNome(request.getNomeCoordenador()));
        termo.setUnidade(Regras.normalizarTexto(request.getUnidade()));
        termo.setCidade(Regras.normalizarTexto(request.getCidade()));

        termo.setStatus(StatusDocumento.AGUARDANDO);
        termo.setDataEnvio(LocalDateTime.now());
        return repository.save(termo);
    }

    // Monitor corrige o termo devolvido e reenvia: a inscrição volta para o supervisor.
    public TermoCompromisso atualizar(String email, String id, AtualizarTermoRequest request) {
        Monitor monitor = monitorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor não encontrado"));
        TermoCompromisso termo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Termo não encontrado"));
        if (!monitor.getRa().equals(termo.getRa())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse termo não pertence ao monitor autenticado");
        }

        // inscrição mais recente ligada a esse termo
        Inscricao inscricao = inscricaoRepository.findByIdTermoCompromisso(id).stream()
                .max(Comparator.comparing(Inscricao::getDataSubmissao,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
        if (inscricao != null && inscricao.getStatus() != StatusInscricao.DEVOLVIDA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O termo só pode ser editado quando a inscrição foi devolvida (status atual: "
                            + inscricao.getStatus() + ")");
        }

        String oferta = request.getOferta().trim().toLowerCase();

        // datas (o RA nunca muda)
        Map<String, String> erros = new LinkedHashMap<>();
        if (naoVazio(request.getPeriodoInicio())) {
            termo.setPeriodoInicio(lerData(request.getPeriodoInicio(), "periodoInicio", "início do período", erros));
        }
        if (naoVazio(request.getPeriodoFim())) {
            termo.setPeriodoFim(lerData(request.getPeriodoFim(), "periodoFim", "fim do período", erros));
        }
        if (naoVazio(request.getDataAssinatura())) {
            termo.setDataAssinatura(lerData(request.getDataAssinatura(), "dataAssinatura", "data de assinatura", erros));
        }
        lancarSeHouverErros(erros);

        validarDatas(erros, termo.getPeriodoInicio(), termo.getPeriodoFim(), termo.getDataAssinatura(), oferta);
        if (naoVazio(request.getEditalNumero())) {
            validarEdital(erros, request.getEditalNumero());
        }
        validarNomesDistintos(erros,
                naoVazio(request.getNomeEstudante()) ? request.getNomeEstudante() : termo.getNomeEstudante(),
                naoVazio(request.getNomeProfessor()) ? request.getNomeProfessor() : termo.getNomeProfessor(),
                naoVazio(request.getNomeCoordenador()) ? request.getNomeCoordenador() : termo.getNomeCoordenador());
        lancarSeHouverErros(erros);

        // CPF novo não pode pertencer a outro aluno
        if (naoVazio(request.getCpf())) {
            String cpf = CpfValidator.formatar(request.getCpf());
            exigirCpfLivre(cpf, termo.getRa());
            termo.setCpf(cpf);
        }

        // obrigatórios
        termo.setDisciplina(request.getDisciplina().trim());
        termo.setOferta(oferta);
        termo.setCargaHoraria(request.getCargaHoraria());

        // opcionais: só troca quando vier preenchido
        if (naoVazio(request.getNomeEstudante())) termo.setNomeEstudante(Regras.normalizarNome(request.getNomeEstudante()));
        if (naoVazio(request.getCurso())) termo.setCurso(request.getCurso().trim());
        if (naoVazio(request.getEditalNumero())) termo.setEditalNumero(limparEdital(request.getEditalNumero()));
        if (naoVazio(request.getNomeProfessor())) termo.setNomeProfessor(Regras.normalizarNome(request.getNomeProfessor()));
        if (naoVazio(request.getNomeCoordenador())) termo.setNomeCoordenador(Regras.normalizarNome(request.getNomeCoordenador()));
        if (naoVazio(request.getUnidade())) termo.setUnidade(request.getUnidade().trim());
        if (naoVazio(request.getCidade())) termo.setCidade(request.getCidade().trim());
        if (request.getNumeroVias() != null) termo.setNumeroVias(request.getNumeroVias());
        if (naoVazio(request.getAssinaturaEstudante())) termo.setAssinaturaEstudante(request.getAssinaturaEstudante());

        termo.setStatus(StatusDocumento.AGUARDANDO);
        termo.setJustificativaDevolucao(null);
        termo.setDataEnvio(LocalDateTime.now());
        TermoCompromisso salvo = repository.save(termo);

        // devolve a inscrição para o supervisor conferir de novo
        if (inscricao != null) {
            inscricao.setStatus(StatusInscricao.AGUARDANDO_SUPERVISOR);
            inscricao.setJustificativaDevolucao(null);
            inscricao.setDataLimiteCorrecao(null);
            inscricao.setDataAtualizacao(LocalDateTime.now());
            inscricaoRepository.save(inscricao);
        }
        return salvo;
    }

    // O mesmo aluno pode reenviar com o mesmo CPF; outro RA com esse CPF não.
    private void exigirCpfLivre(String cpfFormatado, String ra) {
        List<String> grafias = List.of(cpfFormatado, CpfValidator.digitos(cpfFormatado));
        if (repository.existsByCpfInAndRaNot(grafias, ra)) {
            throw new CampoDuplicadoException("cpf", "Este CPF já está cadastrado para outro aluno");
        }
    }

    // Datas que nulas são ignoradas (no PUT elas podem não vir).
    private void validarDatas(Map<String, String> erros, LocalDate inicio, LocalDate fim,
                              LocalDate assinatura, String oferta) {
        LocalDate hoje = LocalDate.now();
        int anoMinimo = hoje.getYear() - 1;
        int anoMaximo = hoje.getYear() + 2;

        if (inicio != null && (inicio.getYear() < anoMinimo || inicio.getYear() > anoMaximo)) {
            erros.put("periodoInicio",
                    "O ano do início do período deve estar entre " + anoMinimo + " e " + anoMaximo);
        }
        if (fim != null && (fim.getYear() < anoMinimo || fim.getYear() > anoMaximo)) {
            erros.put("periodoFim",
                    "O ano do fim do período deve estar entre " + anoMinimo + " e " + anoMaximo);
        }

        if (inicio != null && fim != null && !erros.containsKey("periodoFim")) {
            long dias = ChronoUnit.DAYS.between(inicio, fim);
            if (dias <= 0) {
                erros.put("periodoFim", "A data final precisa ser posterior à data inicial");
            } else if (dias < DIAS_MINIMOS_PERIODO) {
                erros.put("periodoFim",
                        "O período letivo precisa ter pelo menos " + DIAS_MINIMOS_PERIODO + " dias");
            } else if ("semestral".equals(oferta) && dias > DIAS_MAXIMOS_SEMESTRAL) {
                erros.put("periodoFim",
                        "Para oferta semestral, o período não pode passar de cerca de 7 meses");
            } else if ("anual".equals(oferta) && dias > DIAS_MAXIMOS_ANUAL) {
                erros.put("periodoFim",
                        "Para oferta anual, o período não pode passar de cerca de 13 meses");
            }
        }

        if (assinatura != null) {
            if (assinatura.isAfter(hoje)) {
                erros.put("dataAssinatura", "A data de assinatura não pode ser futura");
            } else if (ChronoUnit.DAYS.between(assinatura, hoje) > DIAS_MAXIMOS_ASSINATURA_ANTIGA) {
                erros.put("dataAssinatura", "A data de assinatura está muito antiga. Confira o ano");
            } else if (fim != null && assinatura.isAfter(fim)) {
                erros.put("dataAssinatura",
                        "A data de assinatura não pode ser posterior ao fim do período letivo");
            }
        }
    }

    // O número já passou no padrão NN/AAAA; aqui confere se o ano faz sentido.
    private void validarEdital(Map<String, String> erros, String edital) {
        String[] partes = limparEdital(edital).split("/");
        int ano = Integer.parseInt(partes[1]);
        int anoAtual = LocalDate.now().getYear();
        if (ano < anoAtual - 2 || ano > anoAtual + 1) {
            erros.put("editalNumero", "O ano do edital (" + ano + ") não parece correto");
        }
    }

    // O estudante não pode ser o próprio professor ou coordenador.
    private void validarNomesDistintos(Map<String, String> erros, String estudante,
                                       String professor, String coordenador) {
        String nome = semAcento(estudante);
        if (!nome.isEmpty() && nome.equals(semAcento(professor))) {
            erros.put("nomeProfessor", "Este nome não pode ser igual ao do(a) estudante");
        }
        if (!nome.isEmpty() && nome.equals(semAcento(coordenador))) {
            erros.put("nomeCoordenador", "Este nome não pode ser igual ao do(a) estudante");
        }
    }

    private String semAcento(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(Regras.normalizarNome(texto), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private String limparEdital(String edital) {
        return edital.replaceAll("\\s+", "");
    }

    private void lancarSeHouverErros(Map<String, String> erros) {
        if (!erros.isEmpty()) {
            throw new CampoInvalidoException(erros);
        }
    }

    private boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }

    private LocalDate lerData(String valor, String campo, String rotulo, Map<String, String> erros) {
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            erros.put(campo, "Data inválida em " + rotulo + " (use AAAA-MM-DD)");
            return null;
        }
    }
}