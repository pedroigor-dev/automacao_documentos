package com.automacao.automacaopt1.service;

import com.automacao.automacaopt1.model.ProcessamentoStatus;
import com.automacao.automacaopt1.model.TipoDocumento;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ArquivoService {
    
    private static final int MAX_TEXTO_LENGTH = 3000; 
    
    public ProcessamentoStatus processarArquivo(MultipartFile file) {
        ProcessamentoStatus status = new ProcessamentoStatus();
        status.setNomeOriginal(file.getOriginalFilename());
        status.setStatus("Recebido");

        try {
            String originalName = StringUtils.cleanPath(file.getOriginalFilename());
            Path tempDir = Files.createTempDirectory("upload_");
            Path tempFile = tempDir.resolve(originalName);
            file.transferTo(tempFile);

            Tika tika = new Tika();
            String textoExtraido = tika.parseToString(tempFile.toFile());

            TipoDocumento tipo = identificarTipoDocumento(textoExtraido);
            status.setTipoDocumento(tipo.name());

            String textoResumido = extrairResumoTexto(textoExtraido);
            status.setTextoExtraido(textoResumido);

            String novoNome = gerarNomePadrao(tipo, originalName);
            status.setNomeFinal(novoNome);

            String pastaDestino = "cloud/" + tipo.name();
            Path destino = java.nio.file.Paths.get(pastaDestino);
            Files.createDirectories(destino);
            Path arquivoFinal = destino.resolve(novoNome);
            Files.copy(tempFile, arquivoFinal);

            status.setStatus("Processado e salvo");

            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        } catch (IOException | TikaException e) {
            status.setStatus("Erro: " + e.getMessage());
        }
        return status;
    }


    private String extrairResumoTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }

        String textoLimpo = texto
            .replaceAll("\\s+", " ")
            .replaceAll("\\n{3,}", "\n\n")
            .trim();

        if (textoLimpo.length() > MAX_TEXTO_LENGTH) {
            int tercoParte = MAX_TEXTO_LENGTH / 3;
            
            String inicio = textoLimpo.substring(0, tercoParte);
            int meioStart = (textoLimpo.length() / 2) - (tercoParte / 2);
            String meio = textoLimpo.substring(meioStart, meioStart + tercoParte);
            String fim = textoLimpo.substring(textoLimpo.length() - tercoParte);
            
            return inicio + "\n...\n" + meio + "\n...\n" + fim;
        }

        return textoLimpo;
    }

    private TipoDocumento identificarTipoDocumento(String texto) {
        if (texto == null) return TipoDocumento.DESCONHECIDO;
        String lower = texto.toLowerCase();

        if (lower.contains("parecer técnico") || lower.contains("parecer tecnico")) {
            return TipoDocumento.PARECER_TECNICO;
        }
        if (lower.contains("manual de parecer") || lower.contains("modelo de parecer")) {
            return TipoDocumento.PARECER_TECNICO;
        }
        if (lower.contains("nota fiscal") || lower.contains("nf-e") || lower.contains("nfe")) {
            return TipoDocumento.NOTA_FISCAL;
        }

        String[] curriculo = {"currículo", "curriculo", "resume", "objetivo profissional", 
            "formação acadêmica", "formacao academica", "experiência profissional", 
            "experiencia profissional"};
        
        String[] parecerTecnico = {"parecer", "manual de boas práticas", "manual de boas praticas",
            "elaboração de parecer", "elaboracao de parecer", "grupo de objetos de contratação",
            "aprovador", "assinador digital", "análise técnica", "analise tecnica", 
            "recomendação técnica", "recomendacao tecnica"};
        
        String[] barema = {"barema", "critério de avaliação", "criterio de avaliacao",
            "tabela de pontuação", "tabela de pontuacao", "rubrica de avaliação"};
        
        String[] ingresso = {"ingresso", "evento", "meia entrada", "arena", "setor", "assento",
            "bilheteria", "código de barras", "codigo de barras", "qr code", "ticket",
            "wet eventos", "bora tickets"};
        
        String[] contrato = {"contrato", "cláusula", "clausula", "contratante", "contratada",
            "vigência", "vigencia", "rescisão", "rescisao"};
        
        String[] relatorio = {"relatório", "relatorio", "sumário executivo", "sumario executivo",
            "análise de dados", "analise de dados", "resultados obtidos", "indicadores de desempenho"};
        
        String[] ata = {"ata de reunião", "ata de reuniao", "pauta", "deliberação", "deliberacao",
            "participantes presentes"};
        
        String[] proposta = {"proposta comercial", "proposta técnica", "proposta tecnica",
            "condições comerciais", "condicoes comerciais", "proponente"};
        
        String[] notaFiscal = {"emitente", "destinatário", "destinatario", "código fiscal",
            "codigo fiscal", "cnpj", "inscrição estadual", "inscricao estadual"};
        
        String[] oficio = {"ofício", "oficio", "expediente n°", "expediente nº"};
        
        String[] memorando = {"memorando", "comunicado interno", "memo n°", "memo nº"};
        
        String[] laudo = {"laudo técnico", "laudo tecnico", "exame pericial", "conclusão pericial"};
        
        String[] certidao = {"certidão", "certidao", "cartório", "cartorio", "registro civil"};
        
        String[] procuracao = {"procuração", "procuracao", "outorgante", "outorgado", "poderes para"};
        
        String[] relatorioFinanceiro = {"relatório financeiro", "relatorio financeiro", "balanço",
            "balanco", "demonstrativo financeiro", "receita líquida", "despesa total"};
        
        String[] despacho = {"despacho", "decisão administrativa", "decisao administrativa",
            "determinação", "determinacao"};
        
        String[] planta = {"planta baixa", "desenho técnico", "desenho tecnico", "croqui",
            "especificação técnica", "especificacao tecnica", "projeto arquitetônico"};

        int scoreCurriculo = scorePalavras(lower, curriculo);
        int scoreParecer = scorePalavras(lower, parecerTecnico);
        int scoreBarema = scorePalavras(lower, barema);
        int scoreIngresso = scorePalavras(lower, ingresso);
        int scoreContrato = scorePalavras(lower, contrato);
        int scoreRelatorio = scorePalavras(lower, relatorio);
        int scoreAta = scorePalavras(lower, ata);
        int scoreProposta = scorePalavras(lower, proposta);
        int scoreNotaFiscal = scorePalavras(lower, notaFiscal);
        int scoreOficio = scorePalavras(lower, oficio);
        int scoreMemorando = scorePalavras(lower, memorando);
        int scoreLaudo = scorePalavras(lower, laudo);
        int scoreCertidao = scorePalavras(lower, certidao);
        int scoreProcuracao = scorePalavras(lower, procuracao);
        int scoreRelatorioFin = scorePalavras(lower, relatorioFinanceiro);
        int scoreDespacho = scorePalavras(lower, despacho);
        int scorePlanta = scorePalavras(lower, planta);

        int maxScore = 0;
        TipoDocumento tipo = TipoDocumento.DESCONHECIDO;

        if (scoreParecer > maxScore) { maxScore = scoreParecer; tipo = TipoDocumento.PARECER_TECNICO; }
        if (scoreCurriculo > maxScore) { maxScore = scoreCurriculo; tipo = TipoDocumento.CURRICULO; }
        if (scoreBarema > maxScore) { maxScore = scoreBarema; tipo = TipoDocumento.BAREMA; }
        if (scoreIngresso > maxScore) { maxScore = scoreIngresso; tipo = TipoDocumento.INGRESSO; }
        if (scoreContrato > maxScore) { maxScore = scoreContrato; tipo = TipoDocumento.CONTRATO; }
        if (scoreRelatorio > maxScore) { maxScore = scoreRelatorio; tipo = TipoDocumento.RELATORIO; }
        if (scoreAta > maxScore) { maxScore = scoreAta; tipo = TipoDocumento.ATA; }
        if (scoreProposta > maxScore) { maxScore = scoreProposta; tipo = TipoDocumento.PROPOSTA; }
        if (scoreNotaFiscal > maxScore) { maxScore = scoreNotaFiscal; tipo = TipoDocumento.NOTA_FISCAL; }
        if (scoreOficio > maxScore) { maxScore = scoreOficio; tipo = TipoDocumento.OFICIO; }
        if (scoreMemorando > maxScore) { maxScore = scoreMemorando; tipo = TipoDocumento.MEMORANDO; }
        if (scoreLaudo > maxScore) { maxScore = scoreLaudo; tipo = TipoDocumento.LAUDO; }
        if (scoreCertidao > maxScore) { maxScore = scoreCertidao; tipo = TipoDocumento.CERTIDAO; }
        if (scoreProcuracao > maxScore) { maxScore = scoreProcuracao; tipo = TipoDocumento.PROCURAÇÃO; }
        if (scoreRelatorioFin > maxScore) { maxScore = scoreRelatorioFin; tipo = TipoDocumento.RELATORIO_FINANCEIRO; }
        if (scoreDespacho > maxScore) { maxScore = scoreDespacho; tipo = TipoDocumento.DESPACHO; }
        if (scorePlanta > maxScore) { maxScore = scorePlanta; tipo = TipoDocumento.PLANTA; }

        return tipo;
    }

    private int scorePalavras(String texto, String[] palavras) {
        int score = 0;
        for (String palavra : palavras) {
            if (texto.contains(palavra)) score++;
        }
        return score;
    }

    private String gerarNomePadrao(TipoDocumento tipo, String originalName) {
        String extensao = "";
        int i = originalName.lastIndexOf('.');
        if (i > 0) extensao = originalName.substring(i);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return tipo.name() + "_" + timestamp + extensao;
    }
}
