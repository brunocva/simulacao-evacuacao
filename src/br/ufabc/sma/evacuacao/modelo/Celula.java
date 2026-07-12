package br.ufabc.sma.evacuacao.modelo;

import java.io.Serializable;

public class Celula implements Serializable {

    private final Posicao posicao;
    private final TipoCelula tipo;

    public Celula(Posicao posicao, TipoCelula tipo) {
        this.posicao = posicao;
        this.tipo = tipo;
    }

    public Posicao posicao() {
        return posicao;
    }

    public TipoCelula tipo() {
        return tipo;
    }

    public boolean transitavel() {
        return tipo != TipoCelula.OBSTACULO;
    }
}
