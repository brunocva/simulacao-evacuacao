package br.ufabc.sma.evacuacao.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Predio implements Serializable {

    private final int largura;
    private final int altura;
    private final Posicao saidaPrincipal;
    private final Set<Posicao> obstaculos;
    private final Set<Posicao> riscos;

    public Predio(int largura, int altura, Posicao saidaPrincipal, Set<Posicao> obstaculos) {
        this(largura, altura, saidaPrincipal, obstaculos, Set.of());
    }

    public Predio(int largura, int altura, Posicao saidaPrincipal, Set<Posicao> obstaculos, Set<Posicao> riscos) {
        this.largura = largura;
        this.altura = altura;
        this.saidaPrincipal = saidaPrincipal;
        this.obstaculos = new HashSet<>(obstaculos);
        this.riscos = new HashSet<>(riscos);
    }

    public static Predio criarCenarioBase() {
        Set<Posicao> obstaculos = new HashSet<>();
        obstaculos.add(new Posicao(3, 2));
        obstaculos.add(new Posicao(3, 3));
        obstaculos.add(new Posicao(3, 4));
        return new Predio(8, 6, new Posicao(7, 2), obstaculos);
    }

    public static Predio criarParaCenario(CenarioSimulacao cenario) {
        if (cenario == CenarioSimulacao.SAIDA_BLOQUEADA) {
            Set<Posicao> obstaculos = new HashSet<>();
            obstaculos.add(new Posicao(3, 2));
            obstaculos.add(new Posicao(3, 3));
            obstaculos.add(new Posicao(3, 4));
            obstaculos.add(new Posicao(6, 2));
            return new Predio(8, 6, new Posicao(7, 2), obstaculos);
        }
        if (cenario == CenarioSimulacao.RISCO_CRESCENTE) {
            Set<Posicao> obstaculos = new HashSet<>();
            obstaculos.add(new Posicao(3, 2));
            obstaculos.add(new Posicao(3, 3));
            obstaculos.add(new Posicao(3, 4));

            Set<Posicao> riscos = new HashSet<>();
            riscos.add(new Posicao(4, 1));
            riscos.add(new Posicao(4, 2));
            riscos.add(new Posicao(5, 2));
            riscos.add(new Posicao(5, 3));
            return new Predio(8, 6, new Posicao(7, 2), obstaculos, riscos);
        }
        return criarCenarioBase();
    }

    public boolean posicaoValida(Posicao posicao) {
        return posicao.x() >= 0
                && posicao.x() < largura
                && posicao.y() >= 0
                && posicao.y() < altura
                && !obstaculos.contains(posicao);
    }

    public boolean ehObstaculo(Posicao posicao) {
        return obstaculos.contains(posicao);
    }

    public boolean ehRisco(Posicao posicao) {
        return riscos.contains(posicao);
    }

    public Set<Posicao> posicoesRisco() {
        return Set.copyOf(riscos);
    }

    public List<Posicao> posicoesTransitiveis() {
        List<Posicao> posicoes = new ArrayList<>();
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                Posicao posicao = new Posicao(x, y);
                if (posicaoValida(posicao)) {
                    posicoes.add(posicao);
                }
            }
        }
        return posicoes;
    }

    public boolean ehSaida(Posicao posicao) {
        return saidaPrincipal.equals(posicao);
    }

    public Posicao saidaPrincipal() {
        return saidaPrincipal;
    }

    public int largura() {
        return largura;
    }

    public int altura() {
        return altura;
    }
}
