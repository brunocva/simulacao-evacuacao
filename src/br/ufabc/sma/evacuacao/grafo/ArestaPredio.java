package br.ufabc.sma.evacuacao.grafo;

import br.ufabc.sma.evacuacao.modelo.Posicao;

import java.io.Serializable;

public record ArestaPredio(Posicao origem, Posicao destino, int peso) implements Serializable {
}
