package br.ufabc.sma.evacuacao.grafo;

import br.ufabc.sma.evacuacao.modelo.Posicao;
import br.ufabc.sma.evacuacao.modelo.Predio;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class GrafoPredio {

    private final Predio predio;
    private final Map<Posicao, List<Posicao>> adjacencias = new HashMap<>();

    public GrafoPredio(Predio predio) {
        this.predio = predio;
        construir();
    }

    public static GrafoPredio criar(Predio predio) {
        return new GrafoPredio(predio);
    }

    private void construir() {
        for (Posicao posicao : predio.posicoesTransitiveis()) {
            adjacencias.put(posicao, vizinhosValidos(posicao));
        }
    }

    public int totalNos() {
        return adjacencias.size();
    }

    public int totalArestas() {
        int somaGraus = adjacencias.values().stream()
                .mapToInt(List::size)
                .sum();
        return somaGraus / 2;
    }

    public List<ArestaPredio> arestas() {
        List<ArestaPredio> arestas = new ArrayList<>();

        for (Map.Entry<Posicao, List<Posicao>> entrada : adjacencias.entrySet()) {
            Posicao origem = entrada.getKey();
            for (Posicao destino : entrada.getValue()) {
                if (compararPosicoes(origem, destino) < 0) {
                    arestas.add(new ArestaPredio(origem, destino, 1));
                }
            }
        }

        return arestas;
    }

    public int distanciaAteSaida(Posicao origem) {
        if (predio.ehSaida(origem)) {
            return 0;
        }

        Set<Posicao> visitadas = new HashSet<>();
        Queue<Posicao> fila = new ArrayDeque<>();
        Map<Posicao, Integer> distancias = new HashMap<>();

        fila.add(origem);
        visitadas.add(origem);
        distancias.put(origem, 0);

        while (!fila.isEmpty()) {
            Posicao atual = fila.poll();
            int distanciaAtual = distancias.get(atual);

            for (Posicao vizinho : adjacencias.getOrDefault(atual, List.of())) {
                if (visitadas.contains(vizinho)) {
                    continue;
                }

                if (predio.ehSaida(vizinho)) {
                    return distanciaAtual + 1;
                }

                visitadas.add(vizinho);
                distancias.put(vizinho, distanciaAtual + 1);
                fila.add(vizinho);
            }
        }

        return Integer.MAX_VALUE;
    }

    public MetricasGrafo calcularMetricas() {
        int somaDistancias = 0;
        int posicoesAlcancaveis = 0;

        for (Posicao posicao : adjacencias.keySet()) {
            int distancia = distanciaAteSaida(posicao);
            if (distancia != Integer.MAX_VALUE) {
                somaDistancias += distancia;
                posicoesAlcancaveis++;
            }
        }

        int distanciaMedia = posicoesAlcancaveis == 0 ? 0 : somaDistancias / posicoesAlcancaveis;

        return new MetricasGrafo(
                totalNos(),
                totalArestas(),
                distanciaMedia,
                identificarGargalos()
        );
    }

    public List<Posicao> caminhoMaisCurto(Posicao origem) {
        return caminhoMaisCurto(origem, Set.of());
    }

    public List<Posicao> caminhoMaisCurto(Posicao origem, Set<Posicao> bloqueadas) {
        if (predio.ehSaida(origem)) {
            return List.of(origem);
        }

        Set<Posicao> visitadas = new HashSet<>();
        Queue<Posicao> fila = new ArrayDeque<>();
        Map<Posicao, Posicao> anterior = new HashMap<>();

        fila.add(origem);
        visitadas.add(origem);

        while (!fila.isEmpty()) {
            Posicao atual = fila.poll();

            for (Posicao vizinho : adjacencias.getOrDefault(atual, List.of())) {
                if (visitadas.contains(vizinho) || posicaoBloqueada(origem, vizinho, bloqueadas)) {
                    continue;
                }

                anterior.put(vizinho, atual);
                if (predio.ehSaida(vizinho)) {
                    return reconstruirCaminho(origem, vizinho, anterior);
                }

                visitadas.add(vizinho);
                fila.add(vizinho);
            }
        }

        return List.of();
    }

    public List<Posicao> caminhoMaisCurtoPonderado(
            Posicao origem,
            Set<Posicao> bloqueadas,
            Set<Posicao> ocupadas,
            Set<Posicao> riscos
    ) {
        if (predio.ehSaida(origem)) {
            return List.of(origem);
        }

        Map<Posicao, Integer> custo = new HashMap<>();
        Map<Posicao, Posicao> anterior = new HashMap<>();
        PriorityQueue<PosicaoCusto> fila = new PriorityQueue<>(Comparator.comparingInt(PosicaoCusto::custo));
        Set<Posicao> visitadas = new HashSet<>();

        custo.put(origem, 0);
        fila.add(new PosicaoCusto(origem, 0));

        while (!fila.isEmpty()) {
            PosicaoCusto atual = fila.poll();
            if (!visitadas.add(atual.posicao())) {
                continue;
            }

            if (predio.ehSaida(atual.posicao())) {
                return reconstruirCaminho(origem, atual.posicao(), anterior);
            }

            for (Posicao vizinho : adjacencias.getOrDefault(atual.posicao(), List.of())) {
                if (posicaoBloqueada(origem, vizinho, bloqueadas) || visitadas.contains(vizinho)) {
                    continue;
                }

                int novoCusto = atual.custo() + custoMovimento(vizinho, ocupadas, riscos);
                if (novoCusto < custo.getOrDefault(vizinho, Integer.MAX_VALUE)) {
                    custo.put(vizinho, novoCusto);
                    anterior.put(vizinho, atual.posicao());
                    fila.add(new PosicaoCusto(vizinho, novoCusto));
                }
            }
        }

        return List.of();
    }

    private boolean posicaoBloqueada(Posicao origem, Posicao candidata, Set<Posicao> bloqueadas) {
        return !candidata.equals(origem) && !predio.ehSaida(candidata) && bloqueadas.contains(candidata);
    }

    private int custoMovimento(Posicao posicao, Set<Posicao> ocupadas, Set<Posicao> riscos) {
        int custo = 1;

        if (riscos.contains(posicao) || predio.ehRisco(posicao)) {
            custo += 6;
        }

        custo += congestionamentoAoRedor(posicao, ocupadas);
        return custo;
    }

    private int congestionamentoAoRedor(Posicao posicao, Set<Posicao> ocupadas) {
        int proximos = 0;
        for (Posicao vizinho : List.of(
                new Posicao(posicao.x() + 1, posicao.y()),
                new Posicao(posicao.x() - 1, posicao.y()),
                new Posicao(posicao.x(), posicao.y() + 1),
                new Posicao(posicao.x(), posicao.y() - 1)
        )) {
            if (ocupadas.contains(vizinho)) {
                proximos++;
            }
        }
        return proximos;
    }

    private List<Posicao> reconstruirCaminho(Posicao origem, Posicao destino, Map<Posicao, Posicao> anterior) {
        List<Posicao> caminho = new ArrayList<>();
        Posicao atual = destino;

        while (atual != null) {
            caminho.add(0, atual);
            if (atual.equals(origem)) {
                break;
            }
            atual = anterior.get(atual);
        }

        return caminho;
    }

    private record PosicaoCusto(Posicao posicao, int custo) {
    }

    private List<Posicao> identificarGargalos() {
        return adjacencias.entrySet()
                .stream()
                .filter(entry -> !predio.ehSaida(entry.getKey()))
                .sorted(Comparator.comparingInt(entry -> entry.getValue().size()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<Posicao> vizinhosValidos(Posicao posicao) {
        List<Posicao> candidatos = List.of(
                new Posicao(posicao.x() + 1, posicao.y()),
                new Posicao(posicao.x() - 1, posicao.y()),
                new Posicao(posicao.x(), posicao.y() + 1),
                new Posicao(posicao.x(), posicao.y() - 1)
        );

        return candidatos.stream()
                .filter(predio::posicaoValida)
                .toList();
    }

    private int compararPosicoes(Posicao a, Posicao b) {
        int comparacaoY = Integer.compare(a.y(), b.y());
        if (comparacaoY != 0) {
            return comparacaoY;
        }
        return Integer.compare(a.x(), b.x());
    }
}
