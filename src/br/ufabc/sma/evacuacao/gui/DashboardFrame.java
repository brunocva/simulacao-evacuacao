package br.ufabc.sma.evacuacao.gui;

import br.ufabc.sma.evacuacao.grafo.MetricasGrafo;
import br.ufabc.sma.evacuacao.grafo.GrafoPredio;
import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;
import br.ufabc.sma.evacuacao.modelo.Posicao;
import br.ufabc.sma.evacuacao.modelo.Predio;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private final PainelPredio painelPredio;
    private final JLabel resumoLabel;
    private final JLabel grafoLabel;
    private final JLabel movimentoLabel;
    private final JLabel orientacaoLabel;
    private final JLabel cenarioLabel;
    private int cicloAtual = 0;

    public DashboardFrame(Predio predio, GrafoPredio grafoPredio, CenarioSimulacao cenario) {
        super("Simulacao de Evacuacao - SMA");
        this.painelPredio = new PainelPredio(predio, grafoPredio);
        this.resumoLabel = new JLabel("Pessoas: 0 | Evacuadas: 0");
        this.grafoLabel = new JLabel("Grafo: aguardando metricas");
        this.movimentoLabel = new JLabel("Movimentos: aguardando");
        this.orientacaoLabel = new JLabel("Ultima orientacao: nenhuma");
        this.cenarioLabel = new JLabel("Cenario: " + cenario.name() + " - " + cenario.descricao());

        JPanel barraStatus = new JPanel(new BorderLayout());
        barraStatus.add(resumoLabel, BorderLayout.WEST);
        barraStatus.add(grafoLabel, BorderLayout.EAST);

        JPanel barraMetricas = new JPanel(new BorderLayout());
        barraMetricas.add(cenarioLabel, BorderLayout.NORTH);
        barraMetricas.add(barraStatus, BorderLayout.CENTER);

        JPanel barraInferior = new JPanel(new BorderLayout());
        barraInferior.add(movimentoLabel, BorderLayout.NORTH);
        barraInferior.add(orientacaoLabel, BorderLayout.SOUTH);
        barraMetricas.add(barraInferior, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(painelPredio, BorderLayout.CENTER);
        add(barraMetricas, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 560);
        setLocationRelativeTo(null);
    }

    public void atualizar(
            Map<String, Posicao> pessoas,
            int evacuadas,
            int cicloAtual,
            int movimentosAceitos,
            int movimentosRejeitados,
            int movimentosEmRisco,
            int panicoMedio,
            int orientacoesBrigadista,
            int tempoMedioEvacuacao,
            String ultimaOrientacaoBrigadista,
            MetricasGrafo metricasGrafo
    ) {
        this.cicloAtual = cicloAtual;
        SwingUtilities.invokeLater(() -> {
            painelPredio.atualizarPessoas(pessoas);
            resumoLabel.setText("Ciclo: " + this.cicloAtual
                    + " | Pessoas no predio: " + pessoas.size()
                    + " | Evacuadas: " + evacuadas);
            movimentoLabel.setText("Movimentos aceitos: " + movimentosAceitos
                    + " | Rejeitados: " + movimentosRejeitados
                    + " | Em risco: " + movimentosEmRisco
                    + " | Panico medio: " + panicoMedio
                    + " | Orientacoes: " + orientacoesBrigadista
                    + " | Tempo medio: " + tempoMedioEvacuacao);
            orientacaoLabel.setText("Ultima orientacao: " + ultimaOrientacaoBrigadista);
            if (metricasGrafo != null) {
                grafoLabel.setText("Nos: " + metricasGrafo.totalNos()
                        + " | Arestas: " + metricasGrafo.totalArestas()
                        + " | Dist. media: " + metricasGrafo.distanciaMediaAteSaida());
            }
        });
    }

    public void exibir() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
