package br.ufabc.sma.evacuacao.grafo;

import br.ufabc.sma.evacuacao.modelo.Posicao;

import java.io.Serializable;

public record NoPredio(Posicao posicao) implements Serializable {
}
