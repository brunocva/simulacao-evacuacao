package br.ufabc.sma.evacuacao.relatorio;

import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;
import br.ufabc.sma.evacuacao.utils.Configuracao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComparadorCenarios {

    private static final Path DIRETORIO_OUTPUT = Path.of("output");
    private static final Path ARQUIVO_RESULTADOS = DIRETORIO_OUTPUT.resolve("resultados-cenarios.csv");
    private static final Path ARQUIVO_COMPARATIVO = DIRETORIO_OUTPUT.resolve("comparativo-cenarios.txt");
    private static final Path ARQUIVO_RESUMO_APRESENTACAO = DIRETORIO_OUTPUT.resolve("comparativo-apresentacao.txt");
    private static final Path ARQUIVO_RESUMO_APRESENTACAO_MD = DIRETORIO_OUTPUT.resolve("comparativo-apresentacao.md");
    private static final Path ARQUIVO_ESTATISTICAS = DIRETORIO_OUTPUT.resolve("estatisticas-cenarios.csv");

    public Path gerarComparativo() throws IOException {
        if (Files.notExists(ARQUIVO_RESULTADOS)) {
            return ARQUIVO_COMPARATIVO;
        }

        List<ResultadoCenario> resultados = Files.readAllLines(ARQUIVO_RESULTADOS, StandardCharsets.UTF_8)
                .stream()
                .skip(1)
                .filter(linha -> !linha.isBlank())
                .filter(linha -> !linha.startsWith("cenario,"))
                .map(ResultadoCenario::deLinhaCsv)
                .toList();

        if (resultados.isEmpty()) {
            return ARQUIVO_COMPARATIVO;
        }

        Files.createDirectories(DIRETORIO_OUTPUT);
        Files.writeString(ARQUIVO_COMPARATIVO, montarConteudo(resultados), StandardCharsets.UTF_8);
        Files.writeString(ARQUIVO_RESUMO_APRESENTACAO, montarResumoApresentacao(resultados), StandardCharsets.UTF_8);
        Files.writeString(ARQUIVO_RESUMO_APRESENTACAO_MD, montarResumoApresentacaoMarkdown(resultados), StandardCharsets.UTF_8);
        Files.writeString(ARQUIVO_ESTATISTICAS, montarEstatisticasCsv(resultados), StandardCharsets.UTF_8);
        return ARQUIVO_COMPARATIVO;
    }

    private String montarResumoApresentacao(List<ResultadoCenario> resultados) {
        Map<String, ResumoCenario> consolidados = consolidarPorCenario(resultados);
        ResumoCenario baseline = consolidados.get(Configuracao.cenarioBase().name());

        ResumoCenario melhorDesempenho = consolidados.values().stream()
                .min(this::compararMelhorDesempenho)
                .orElseThrow();

        ResumoCenario menorPanico = consolidados.values().stream()
                .min(Comparator.comparingDouble(ResumoCenario::mediaPanico))
                .orElseThrow();

        ResumoCenario menosRejeicoes = consolidados.values().stream()
                .min(Comparator.comparingDouble(ResumoCenario::mediaRejeicoes))
                .orElseThrow();

        StringBuilder builder = new StringBuilder();
        builder.append("Resumo para apresentacao\n");
        builder.append("========================\n\n");
        builder.append("Baseline: ").append(Configuracao.cenarioBase().name()).append('\n');
        builder.append("Melhor desempenho geral: ").append(melhorDesempenho.cenario()).append('\n');
        builder.append("Menor panico medio: ").append(menorPanico.cenario()).append('\n');
        builder.append("Menos rejeicoes: ").append(menosRejeicoes.cenario()).append("\n\n");

        builder.append("Leitura interpretativa\n");
        builder.append(montarLeituraInterpretativa(consolidados, baseline, false)).append('\n');

        builder.append("Tabela compacta\n");
        builder.append("cenario | execucoes | tempoMedio(dp) | deltaTempo | panico(dp) | deltaPanico | rejeicoes(dp) | deltaRejeicoes\n");
        builder.append("-------- | -------- | -------------- | ---------- | ---------- | ----------- | ------------- | --------------\n");

        for (ResumoCenario resumo : consolidados.values()) {
            String deltaTempo = baseline == null || resumo.baseline()
                    ? "-"
                    : formatar(resumo.mediaTempoMedio() - baseline.mediaTempoMedio());
            String deltaPanico = baseline == null || resumo.baseline()
                    ? "-"
                    : formatar(resumo.mediaPanico() - baseline.mediaPanico());
            String deltaRejeicoes = baseline == null || resumo.baseline()
                    ? "-"
                    : formatar(resumo.mediaRejeicoes() - baseline.mediaRejeicoes());

            builder.append(resumo.cenario())
                    .append(resumo.baseline() ? " [BASE]" : "")
                    .append(" | ").append(resumo.execucoes())
                    .append(" | ").append(formatar(resumo.mediaTempoMedio()))
                    .append(" (").append(formatar(resumo.desvioTempoMedio())).append(')')
                    .append(" | ").append(deltaTempo)
                    .append(" | ").append(formatar(resumo.mediaPanico()))
                    .append(" (").append(formatar(resumo.desvioPanico())).append(')')
                    .append(" | ").append(deltaPanico)
                    .append(" | ").append(formatar(resumo.mediaRejeicoes()))
                    .append(" (").append(formatar(resumo.desvioRejeicoes())).append(')')
                    .append(" | ").append(deltaRejeicoes)
                    .append('\n');
        }

        return builder.toString();
    }

    private String montarResumoApresentacaoMarkdown(List<ResultadoCenario> resultados) {
        Map<String, ResumoCenario> consolidados = consolidarPorCenario(resultados);
        ResumoCenario baseline = consolidados.get(Configuracao.cenarioBase().name());

        ResumoCenario melhorDesempenho = consolidados.values().stream()
                .min(this::compararMelhorDesempenho)
                .orElseThrow();

        ResumoCenario menorPanico = consolidados.values().stream()
                .min(Comparator.comparingDouble(ResumoCenario::mediaPanico))
                .orElseThrow();

        ResumoCenario menosRejeicoes = consolidados.values().stream()
                .min(Comparator.comparingDouble(ResumoCenario::mediaRejeicoes))
                .orElseThrow();

        StringBuilder builder = new StringBuilder();
        builder.append("# Resumo para apresentacao\n\n");
        builder.append("- Baseline: ").append(Configuracao.cenarioBase().name()).append('\n');
        builder.append("- Melhor desempenho geral: ").append(melhorDesempenho.cenario()).append('\n');
        builder.append("- Menor panico medio: ").append(menorPanico.cenario()).append('\n');
        builder.append("- Menos rejeicoes: ").append(menosRejeicoes.cenario()).append("\n\n");

        builder.append("## Leitura interpretativa\n\n");
        builder.append(montarLeituraInterpretativa(consolidados, baseline, true)).append('\n');

        builder.append("| Cenario | Execucoes | Tempo medio (dp) | Delta tempo | Panico medio (dp) | Delta panico | Rejeicoes (dp) | Delta rejeicoes |\n");
        builder.append("| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |\n");

        for (ResumoCenario resumo : consolidados.values()) {
            String deltaTempo = baseline == null || resumo.baseline()
                    ? "-"
                    : formatar(resumo.mediaTempoMedio() - baseline.mediaTempoMedio());
            String deltaPanico = baseline == null || resumo.baseline()
                    ? "-"
                    : formatar(resumo.mediaPanico() - baseline.mediaPanico());
            String deltaRejeicoes = baseline == null || resumo.baseline()
                    ? "-"
                    : formatar(resumo.mediaRejeicoes() - baseline.mediaRejeicoes());

            builder.append("| ")
                    .append(resumo.cenario())
                    .append(resumo.baseline() ? " [BASE]" : "")
                    .append(" | ").append(resumo.execucoes())
                    .append(" | ").append(formatar(resumo.mediaTempoMedio()))
                    .append(" (").append(formatar(resumo.desvioTempoMedio())).append(')')
                    .append(" | ").append(deltaTempo)
                    .append(" | ").append(formatar(resumo.mediaPanico()))
                    .append(" (").append(formatar(resumo.desvioPanico())).append(')')
                    .append(" | ").append(deltaPanico)
                    .append(" | ").append(formatar(resumo.mediaRejeicoes()))
                    .append(" (").append(formatar(resumo.desvioRejeicoes())).append(')')
                    .append(" | ").append(deltaRejeicoes)
                    .append(" |\n");
        }

        return builder.toString();
    }

    private String montarConteudo(List<ResultadoCenario> resultados) {
        Map<String, ResumoCenario> consolidados = consolidarPorCenario(resultados);
        ResumoCenario baseline = consolidados.get(Configuracao.cenarioBase().name());

        ResumoCenario melhorDesempenho = consolidados.values().stream()
                .min(this::compararMelhorDesempenho)
                .orElseThrow();

        ResumoCenario menorPanico = consolidados.values().stream()
                .min(Comparator.comparingDouble(ResumoCenario::mediaPanico))
                .orElseThrow();

        ResumoCenario menosRejeicoes = consolidados.values().stream()
                .min(Comparator.comparingDouble(ResumoCenario::mediaRejeicoes))
                .orElseThrow();

        StringBuilder builder = new StringBuilder();
        builder.append("Comparativo de Cenarios\n");
        builder.append("=======================\n\n");
        builder.append("Gerado em: ").append(LocalDateTime.now()).append('\n');
        builder.append("Execucoes consideradas: ").append(resultados.size()).append('\n');
        builder.append("Baseline: ").append(Configuracao.cenarioBase().name()).append("\n\n");

        builder.append("Destaques\n");
        builder.append("- Melhor desempenho geral: ").append(descrever(melhorDesempenho)).append('\n');
        builder.append("- Menor panico medio: ").append(descrever(menorPanico)).append('\n');
        builder.append("- Menos movimentos rejeitados: ").append(descrever(menosRejeicoes)).append("\n\n");

        builder.append("Leitura interpretativa\n");
        builder.append(montarLeituraInterpretativa(consolidados, baseline, false)).append('\n');

        builder.append("Resumo por cenario\n");
        for (ResumoCenario resumo : consolidados.values()) {
            builder.append("- ")
                    .append(resumo.cenario())
                    .append(resumo.baseline() ? " [BASELINE]" : "")
                    .append(" | execucoes=").append(resumo.execucoes())
                    .append(" | ciclos=").append(formatar(resumo.mediaCiclos()))
                    .append(" (dp=").append(formatar(resumo.desvioCiclos())).append(')')
                    .append(" | evacuadas=").append(formatar(resumo.mediaEvacuadas()))
                    .append(" (dp=").append(formatar(resumo.desvioEvacuadas())).append(')')
                    .append(" | panico=").append(formatar(resumo.mediaPanico()))
                    .append(" (dp=").append(formatar(resumo.desvioPanico())).append(')')
                    .append(" | rejeicoes=").append(formatar(resumo.mediaRejeicoes()))
                    .append(" (dp=").append(formatar(resumo.desvioRejeicoes())).append(')')
                    .append(" | tempoMedio=").append(formatar(resumo.mediaTempoMedio()))
                    .append(" (dp=").append(formatar(resumo.desvioTempoMedio())).append(')')
                    .append(" | risco=").append(formatar(resumo.mediaRisco()))
                    .append(" | orientacoes=").append(formatar(resumo.mediaOrientacoes()));

            if (baseline != null && !resumo.baseline()) {
                builder.append(" | deltaTempo=").append(formatar(resumo.mediaTempoMedio() - baseline.mediaTempoMedio()))
                        .append(" | deltaPanico=").append(formatar(resumo.mediaPanico() - baseline.mediaPanico()))
                        .append(" | deltaRejeicoes=").append(formatar(resumo.mediaRejeicoes() - baseline.mediaRejeicoes()));
            }

            builder
                    .append('\n');
        }

        builder.append("\nResultados por execucao\n");
        for (ResultadoCenario resultado : resultados) {
            builder.append("- ")
                    .append(resultado.cenario())
                    .append(" | execucaoId=").append(resultado.execucaoId())
                    .append(" | seed=").append(resultado.seed())
                    .append(" | motivo=").append(resultado.motivo())
                    .append(" | ciclos=").append(resultado.ciclos())
                    .append(" | evacuadas=").append(resultado.pessoasEvacuadas()).append('/').append(resultado.totalPessoas())
                    .append(" | panico=").append(resultado.panicoMedio())
                    .append(" | rejeicoes=").append(resultado.movimentosRejeitados())
                    .append(" | tempoMedio=").append(resultado.tempoMedioEvacuacao())
                    .append(" | risco=").append(resultado.movimentosEmRisco())
                    .append(" | orientacoes=").append(resultado.orientacoesBrigadista())
                    .append('\n');
        }

        return builder.toString();
    }

    private String montarEstatisticasCsv(List<ResultadoCenario> resultados) {
        Map<String, ResumoCenario> consolidados = consolidarPorCenario(resultados);
        ResumoCenario baseline = consolidados.get(Configuracao.cenarioBase().name());

        StringBuilder builder = new StringBuilder();
        builder.append("cenario,baseline,execucoes,total_pessoas,taxa_evacuacao,media_ciclos,desvio_ciclos,")
                .append("media_evacuadas,desvio_evacuadas,media_tempo,desvio_tempo,delta_tempo,")
                .append("media_panico,desvio_panico,delta_panico,media_rejeicoes,desvio_rejeicoes,delta_rejeicoes,")
                .append("media_risco,media_orientacoes\n");

        for (ResumoCenario resumo : consolidados.values()) {
            builder.append(resumo.cenario()).append(',')
                    .append(resumo.baseline()).append(',')
                    .append(resumo.execucoes()).append(',')
                    .append(resumo.totalPessoas()).append(',')
                    .append(formatarCsv(resumo.taxaEvacuacao())).append(',')
                    .append(formatarCsv(resumo.mediaCiclos())).append(',')
                    .append(formatarCsv(resumo.desvioCiclos())).append(',')
                    .append(formatarCsv(resumo.mediaEvacuadas())).append(',')
                    .append(formatarCsv(resumo.desvioEvacuadas())).append(',')
                    .append(formatarCsv(resumo.mediaTempoMedio())).append(',')
                    .append(formatarCsv(resumo.desvioTempoMedio())).append(',')
                    .append(formatarCsv(deltaTempo(resumo, baseline))).append(',')
                    .append(formatarCsv(resumo.mediaPanico())).append(',')
                    .append(formatarCsv(resumo.desvioPanico())).append(',')
                    .append(formatarCsv(deltaPanico(resumo, baseline))).append(',')
                    .append(formatarCsv(resumo.mediaRejeicoes())).append(',')
                    .append(formatarCsv(resumo.desvioRejeicoes())).append(',')
                    .append(formatarCsv(deltaRejeicoes(resumo, baseline))).append(',')
                    .append(formatarCsv(resumo.mediaRisco())).append(',')
                    .append(formatarCsv(resumo.mediaOrientacoes()))
                    .append('\n');
        }

        return builder.toString();
    }

    private String montarLeituraInterpretativa(
            Map<String, ResumoCenario> consolidados,
            ResumoCenario baseline,
            boolean markdown
    ) {
        if (baseline == null) {
            return markdown
                    ? "- Baseline indisponivel; execute o cenario `" + Configuracao.cenarioBase().name() + "` para calcular deltas.\n"
                    : "- Baseline indisponivel; execute o cenario " + Configuracao.cenarioBase().name() + " para calcular deltas.\n";
        }

        StringBuilder builder = new StringBuilder();
        for (ResumoCenario resumo : consolidados.values()) {
            if (resumo.baseline()) {
                continue;
            }
            builder.append(markdown ? "- " : "- ")
                    .append(interpretarCenario(resumo, baseline))
                    .append('\n');
        }
        return builder.toString();
    }

    private String interpretarCenario(ResumoCenario resumo, ResumoCenario baseline) {
        double deltaTempo = deltaTempo(resumo, baseline);
        double deltaPanico = deltaPanico(resumo, baseline);
        double deltaRejeicoes = deltaRejeicoes(resumo, baseline);

        return resumo.cenario()
                + ": tempo medio " + descreverDelta(deltaTempo, "menor", "maior")
                + ", panico medio " + descreverDelta(deltaPanico, "menor", "maior")
                + " e rejeicoes " + descreverDelta(deltaRejeicoes, "menores", "maiores")
                + " em relacao ao baseline.";
    }

    private String descreverDelta(double delta, String palavraMenor, String palavraMaior) {
        if (Math.abs(delta) < 0.0001) {
            return "igual";
        }
        return formatar(Math.abs(delta)) + " " + (delta < 0 ? palavraMenor : palavraMaior);
    }

    private Map<String, ResumoCenario> consolidarPorCenario(List<ResultadoCenario> resultados) {
        Map<String, List<ResultadoCenario>> agrupados = resultados.stream()
                .collect(Collectors.groupingBy(ResultadoCenario::cenario, LinkedHashMap::new, Collectors.toList()));

        Map<String, ResumoCenario> consolidados = new LinkedHashMap<>();
        for (Map.Entry<String, List<ResultadoCenario>> entry : agrupados.entrySet()) {
            consolidados.put(
                    entry.getKey(),
                    ResumoCenario.de(
                            entry.getKey(),
                            entry.getValue(),
                            Configuracao.cenarioBase().name().equals(entry.getKey())
                    )
            );
        }
        return consolidados;
    }

    private String descrever(ResumoCenario resumo) {
        return resumo.cenario()
                + " (ciclos=" + formatar(resumo.mediaCiclos())
                + ", panico=" + formatar(resumo.mediaPanico())
                + ", rejeicoes=" + formatar(resumo.mediaRejeicoes())
                + ")";
    }

    private int compararMelhorDesempenho(ResumoCenario a, ResumoCenario b) {
        int comparacaoEvacuacao = Double.compare(b.taxaEvacuacao(), a.taxaEvacuacao());
        if (comparacaoEvacuacao != 0) {
            return comparacaoEvacuacao;
        }

        int comparacaoTempo = Double.compare(a.mediaTempoMedio(), b.mediaTempoMedio());
        if (comparacaoTempo != 0) {
            return comparacaoTempo;
        }

        return Double.compare(a.mediaRejeicoes(), b.mediaRejeicoes());
    }

    private double deltaTempo(ResumoCenario resumo, ResumoCenario baseline) {
        return baseline == null || resumo.baseline() ? 0.0 : resumo.mediaTempoMedio() - baseline.mediaTempoMedio();
    }

    private double deltaPanico(ResumoCenario resumo, ResumoCenario baseline) {
        return baseline == null || resumo.baseline() ? 0.0 : resumo.mediaPanico() - baseline.mediaPanico();
    }

    private double deltaRejeicoes(ResumoCenario resumo, ResumoCenario baseline) {
        return baseline == null || resumo.baseline() ? 0.0 : resumo.mediaRejeicoes() - baseline.mediaRejeicoes();
    }

    private String formatar(double valor) {
        if (Math.abs(valor - Math.rint(valor)) < 0.0001) {
            return String.valueOf((long) Math.rint(valor));
        }
        return String.format("%.2f", valor);
    }

    private String formatarCsv(double valor) {
        return String.format(java.util.Locale.US, "%.4f", valor);
    }

    private record ResumoCenario(
            String cenario,
            boolean baseline,
            int execucoes,
            int totalPessoas,
            double mediaCiclos,
            double desvioCiclos,
            double mediaEvacuadas,
            double desvioEvacuadas,
            double mediaPanico,
            double desvioPanico,
            double mediaRejeicoes,
            double desvioRejeicoes,
            double mediaTempoMedio,
            double desvioTempoMedio,
            double mediaRisco,
            double mediaOrientacoes
    ) {

        double taxaEvacuacao() {
            return totalPessoas == 0 ? 0.0 : mediaEvacuadas / totalPessoas;
        }

        static ResumoCenario de(String cenario, List<ResultadoCenario> resultados, boolean baseline) {
            int totalPessoas = resultados.isEmpty() ? 0 : resultados.get(0).totalPessoas();
            return new ResumoCenario(
                    cenario,
                    baseline,
                    resultados.size(),
                    totalPessoas,
                    media(resultados, ResultadoCenario::ciclos),
                    desvioPadrao(resultados, ResultadoCenario::ciclos),
                    media(resultados, ResultadoCenario::pessoasEvacuadas),
                    desvioPadrao(resultados, ResultadoCenario::pessoasEvacuadas),
                    media(resultados, ResultadoCenario::panicoMedio),
                    desvioPadrao(resultados, ResultadoCenario::panicoMedio),
                    media(resultados, ResultadoCenario::movimentosRejeitados),
                    desvioPadrao(resultados, ResultadoCenario::movimentosRejeitados),
                    media(resultados, ResultadoCenario::tempoMedioEvacuacao),
                    desvioPadrao(resultados, ResultadoCenario::tempoMedioEvacuacao),
                    media(resultados, ResultadoCenario::movimentosEmRisco),
                    media(resultados, ResultadoCenario::orientacoesBrigadista)
            );
        }

        private static double media(List<ResultadoCenario> resultados, java.util.function.ToIntFunction<ResultadoCenario> extrator) {
            return resultados.stream().mapToInt(extrator).average().orElse(0.0);
        }

        private static double desvioPadrao(List<ResultadoCenario> resultados, java.util.function.ToIntFunction<ResultadoCenario> extrator) {
            if (resultados.size() <= 1) {
                return 0.0;
            }

            double media = media(resultados, extrator);
            double somaQuadrados = resultados.stream()
                    .mapToDouble(resultado -> {
                        double diferenca = extrator.applyAsInt(resultado) - media;
                        return diferenca * diferenca;
                    })
                    .sum();

            return Math.sqrt(somaQuadrados / (resultados.size() - 1));
        }
    }
}
