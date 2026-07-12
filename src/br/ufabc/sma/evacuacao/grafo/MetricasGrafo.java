package br.ufabc.sma.evacuacao.grafo;

import br.ufabc.sma.evacuacao.modelo.Posicao;

import java.io.Serializable;
import java.util.List;

public record MetricasGrafo(
        int totalNos,
        int totalArestas,
        int distanciaMediaAteSaida,
        List<Posicao> gargalos
) implements Serializable {
}
