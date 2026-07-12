package br.ufabc.sma.evacuacao.relatorio;

import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;
import br.ufabc.sma.evacuacao.modelo.EstadoSimulacao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ExportadorCsv {

    private static final Path DIRETORIO_OUTPUT = Path.of("output");
    private static final Path ARQUIVO_CSV = DIRETORIO_OUTPUT.resolve("resultados-cenarios.csv");

    public Path registrar(CenarioSimulacao cenario, EstadoSimulacao estadoFinal, String motivo) throws IOException {
        Files.createDirectories(DIRETORIO_OUTPUT);

        boolean criarCabecalho = Files.notExists(ARQUIVO_CSV);
        StringBuilder linha = new StringBuilder();

        if (criarCabecalho) {
            linha.append("cenario,motivo,ciclos,total_pessoas,pessoas_evacuadas,pessoas_no_predio,")
                    .append("movimentos_aceitos,movimentos_rejeitados,movimentos_em_risco,panico_medio,")
                    .append("orientacoes_brigadista,tempo_medio_evacuacao,total_nos,total_arestas,distancia_media_saida,")
                    .append("execucao_id,seed\n");
        }

        linha.append(cenario.name()).append(',')
                .append(motivo).append(',')
                .append(estadoFinal.cicloAtual()).append(',')
                .append(estadoFinal.totalPessoas()).append(',')
                .append(estadoFinal.pessoasEvacuadas()).append(',')
                .append(estadoFinal.pessoasNoPredio()).append(',')
                .append(estadoFinal.movimentosAceitos()).append(',')
                .append(estadoFinal.movimentosRejeitados()).append(',')
                .append(estadoFinal.movimentosEmRisco()).append(',')
                .append(estadoFinal.panicoMedio()).append(',')
                .append(estadoFinal.orientacoesBrigadista()).append(',')
                .append(estadoFinal.tempoMedioEvacuacao()).append(',')
                .append(estadoFinal.metricasGrafo().totalNos()).append(',')
                .append(estadoFinal.metricasGrafo().totalArestas()).append(',')
                .append(estadoFinal.metricasGrafo().distanciaMediaAteSaida()).append(',')
                .append(br.ufabc.sma.evacuacao.utils.Configuracao.identificadorExecucao()).append(',')
                .append(br.ufabc.sma.evacuacao.utils.Configuracao.sementeBase())
                .append('\n');

        Files.writeString(
                ARQUIVO_CSV,
                linha.toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

        return ARQUIVO_CSV;
    }
}
