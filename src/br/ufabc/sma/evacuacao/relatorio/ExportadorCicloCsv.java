package br.ufabc.sma.evacuacao.relatorio;

import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;
import br.ufabc.sma.evacuacao.modelo.EstadoSimulacao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ExportadorCicloCsv {

    private static final Path DIRETORIO_OUTPUT = Path.of("output");
    private static final Path ARQUIVO_CSV = DIRETORIO_OUTPUT.resolve("metricas-ciclos.csv");

    public Path registrar(CenarioSimulacao cenario, EstadoSimulacao estado) throws IOException {
        Files.createDirectories(DIRETORIO_OUTPUT);

        boolean criarCabecalho = Files.notExists(ARQUIVO_CSV);
        StringBuilder linha = new StringBuilder();

        if (criarCabecalho) {
            linha.append("cenario,ciclo,total_pessoas,pessoas_evacuadas,pessoas_no_predio,")
                    .append("movimentos_aceitos,movimentos_rejeitados,movimentos_em_risco,")
                    .append("panico_medio,orientacoes_brigadista,tempo_medio_evacuacao,")
                    .append("total_nos,total_arestas,distancia_media_saida,execucao_id,seed\n");
        }

        linha.append(cenario.name()).append(',')
                .append(estado.cicloAtual()).append(',')
                .append(estado.totalPessoas()).append(',')
                .append(estado.pessoasEvacuadas()).append(',')
                .append(estado.pessoasNoPredio()).append(',')
                .append(estado.movimentosAceitos()).append(',')
                .append(estado.movimentosRejeitados()).append(',')
                .append(estado.movimentosEmRisco()).append(',')
                .append(estado.panicoMedio()).append(',')
                .append(estado.orientacoesBrigadista()).append(',')
                .append(estado.tempoMedioEvacuacao()).append(',')
                .append(estado.metricasGrafo().totalNos()).append(',')
                .append(estado.metricasGrafo().totalArestas()).append(',')
                .append(estado.metricasGrafo().distanciaMediaAteSaida()).append(',')
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
