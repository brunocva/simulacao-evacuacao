package br.ufabc.sma.evacuacao.modelo;

import java.io.Serializable;

public record Movimento(Posicao destino, int panicoAtual) implements Serializable {
}
