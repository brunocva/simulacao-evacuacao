package br.ufabc.sma.evacuacao.modelo;

import br.ufabc.sma.evacuacao.grafo.MetricasGrafo;

import java.io.Serializable;
import java.util.Map;

public record EstadoSimulacao(
        int cicloAtual,
        int totalPessoas,
        int pessoasNoPredio,
        int pessoasEvacuadas,
        int movimentosAceitos,
        int movimentosRejeitados,
        int movimentosEmRisco,
        int panicoMedio,
        int orientacoesBrigadista,
        int tempoMedioEvacuacao,
        Posicao saidaPrincipal,
        MetricasGrafo metricasGrafo,
        Map<String, Posicao> posicoesPessoas,
        Map<String, Posicao> sugestoesProximaPosicao
) implements Serializable {

    public boolean evacuacaoConcluida() {
        return pessoasEvacuadas >= totalPessoas;
    }
}
