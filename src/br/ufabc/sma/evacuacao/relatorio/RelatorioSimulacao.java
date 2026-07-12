package br.ufabc.sma.evacuacao.relatorio;

import br.ufabc.sma.evacuacao.grafo.MetricasGrafo;
import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;
import br.ufabc.sma.evacuacao.modelo.EstadoSimulacao;
import br.ufabc.sma.evacuacao.utils.Configuracao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RelatorioSimulacao {

    private final CenarioSimulacao cenario;
    private final EstadoSimulacao estadoFinal;
    private final String motivoEncerramento;
    private final LocalDateTime geradoEm;

    public RelatorioSimulacao(CenarioSimulacao cenario, EstadoSimulacao estadoFinal, String motivoEncerramento) {
        this.cenario = cenario;
        this.estadoFinal = estadoFinal;
        this.motivoEncerramento = motivoEncerramento;
        this.geradoEm = LocalDateTime.now();
    }

    public String nomeArquivo() {
        String timestamp = geradoEm.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "relatorio-" + cenario.name().toLowerCase() + "-" + timestamp + ".txt";
    }

    public String conteudo() {
        MetricasGrafo metricas = estadoFinal.metricasGrafo();

        StringBuilder builder = new StringBuilder();
        builder.append("Relatorio da Simulacao de Evacuacao\n");
        builder.append("====================================\n\n");
        builder.append("Gerado em: ").append(geradoEm).append('\n');
        builder.append("Execucao ID: ").append(Configuracao.identificadorExecucao()).append('\n');
        builder.append("Seed base: ").append(Configuracao.sementeBase()).append('\n');
        builder.append("Cenario: ").append(cenario.name()).append(" - ").append(cenario.descricao()).append('\n');
        builder.append("Motivo de encerramento: ").append(motivoEncerramento).append("\n\n");

        builder.append("Resultados\n");
        builder.append("- Ciclos executados: ").append(estadoFinal.cicloAtual()).append('\n');
        builder.append("- Total de pessoas: ").append(estadoFinal.totalPessoas()).append('\n');
        builder.append("- Pessoas evacuadas: ").append(estadoFinal.pessoasEvacuadas()).append('\n');
        builder.append("- Pessoas restantes no predio: ").append(estadoFinal.pessoasNoPredio()).append('\n');
        builder.append("- Movimentos aceitos: ").append(estadoFinal.movimentosAceitos()).append('\n');
        builder.append("- Movimentos rejeitados: ").append(estadoFinal.movimentosRejeitados()).append('\n');
        builder.append("- Movimentos em area de risco: ").append(estadoFinal.movimentosEmRisco()).append('\n');
        builder.append("- Panico medio final: ").append(estadoFinal.panicoMedio()).append('\n');
        builder.append("- Orientacoes do brigadista: ").append(estadoFinal.orientacoesBrigadista()).append('\n');
        builder.append("- Tempo medio de evacuacao: ").append(estadoFinal.tempoMedioEvacuacao()).append(" ciclos\n\n");

        builder.append("Metricas do Grafo\n");
        builder.append("- Total de nos: ").append(metricas.totalNos()).append('\n');
        builder.append("- Total de arestas: ").append(metricas.totalArestas()).append('\n');
        builder.append("- Distancia media ate a saida: ").append(metricas.distanciaMediaAteSaida()).append('\n');
        builder.append("- Gargalos iniciais: ").append(metricas.gargalos()).append('\n');

        return builder.toString();
    }
}
