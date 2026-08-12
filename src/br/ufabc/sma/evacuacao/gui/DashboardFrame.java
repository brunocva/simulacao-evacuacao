package br.ufabc.sma.evacuacao.gui;

import br.ufabc.sma.evacuacao.grafo.MetricasGrafo;
import br.ufabc.sma.evacuacao.grafo.GrafoPredio;
import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;
import br.ufabc.sma.evacuacao.modelo.Posicao;
import br.ufabc.sma.evacuacao.modelo.Predio;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private final PainelPredio painelPredio;
    private final JLabel cicloLabel;
    private final JLabel pessoasLabel;
    private final JLabel evacuadasLabel;
    private final JLabel rejeicoesLabel;
    private final JLabel riscoLabel;
    private final JLabel panicoLabel;
    private final JLabel tempoLabel;
    private final JLabel orientacoesLabel;
    private final JLabel grafoLabel;
    private final JLabel orientacaoLabel;
    private final JLabel cenarioLabel;
    private int cicloAtual = 0;

    public DashboardFrame(Predio predio, GrafoPredio grafoPredio, CenarioSimulacao cenario) {
        super("Simulacao de Evacuacao - SMA");
        this.painelPredio = new PainelPredio(predio, grafoPredio);
        this.cicloLabel = criarMetrica("Ciclo", "0");
        this.pessoasLabel = criarMetrica("No predio", "0");
        this.evacuadasLabel = criarMetrica("Evacuadas", "0");
        this.rejeicoesLabel = criarMetrica("Rejeicoes", "0");
        this.riscoLabel = criarMetrica("Em risco", "0");
        this.panicoLabel = criarMetrica("Panico medio", "0");
        this.tempoLabel = criarMetrica("Tempo medio", "0");
        this.orientacoesLabel = criarMetrica("Orientacoes", "0");
        this.grafoLabel = new JLabel("Nos: 0 | Arestas: 0 | Dist. media: 0");
        this.orientacaoLabel = new JLabel("Ultima orientacao: nenhuma");
        this.cenarioLabel = new JLabel("Cenario: " + cenario.name() + " - " + cenario.descricao());

        JCheckBox alternarGrafo = new JCheckBox("Grafo", true);
        alternarGrafo.addActionListener(event -> painelPredio.setMostrarGrafo(alternarGrafo.isSelected()));

        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
        topo.add(cenarioLabel, BorderLayout.WEST);
        topo.add(alternarGrafo, BorderLayout.EAST);

        JPanel painelMetricas = new JPanel(new GridLayout(2, 4, 8, 6));
        painelMetricas.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
        painelMetricas.add(cicloLabel);
        painelMetricas.add(pessoasLabel);
        painelMetricas.add(evacuadasLabel);
        painelMetricas.add(rejeicoesLabel);
        painelMetricas.add(riscoLabel);
        painelMetricas.add(panicoLabel);
        painelMetricas.add(tempoLabel);
        painelMetricas.add(orientacoesLabel);

        JPanel legenda = criarLegenda();

        JPanel rodape = new JPanel(new BorderLayout(8, 4));
        rodape.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
        rodape.add(grafoLabel, BorderLayout.WEST);
        rodape.add(orientacaoLabel, BorderLayout.CENTER);

        JPanel barraMetricas = new JPanel(new BorderLayout());
        barraMetricas.add(topo, BorderLayout.NORTH);
        barraMetricas.add(painelMetricas, BorderLayout.CENTER);
        barraMetricas.add(legenda, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(painelPredio, BorderLayout.CENTER);
        add(barraMetricas, BorderLayout.NORTH);
        add(rodape, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(820, 620);
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
            String pessoaOrientadaBrigadista,
            Map<String, Integer> panicos,
            MetricasGrafo metricasGrafo
    ) {
        this.cicloAtual = cicloAtual;
        SwingUtilities.invokeLater(() -> {
            painelPredio.atualizarPessoas(pessoas, panicos, pessoaOrientadaBrigadista);
            cicloLabel.setText("Ciclo: " + this.cicloAtual);
            pessoasLabel.setText("No predio: " + pessoas.size());
            evacuadasLabel.setText("Evacuadas: " + evacuadas);
            rejeicoesLabel.setText("Rejeicoes: " + movimentosRejeitados);
            riscoLabel.setText("Em risco: " + movimentosEmRisco);
            panicoLabel.setText("Panico medio: " + panicoMedio);
            tempoLabel.setText("Tempo medio: " + tempoMedioEvacuacao);
            orientacoesLabel.setText("Orientacoes: " + orientacoesBrigadista);
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

    private JLabel criarMetrica(String nome, String valorInicial) {
        JLabel label = new JLabel(nome + ": " + valorInicial);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 211)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        label.setOpaque(true);
        label.setBackground(new Color(248, 249, 247));
        return label;
    }

    private JPanel criarLegenda() {
        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        legenda.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 10));
        legenda.add(itemLegenda("Pessoa", new Color(47, 102, 170)));
        legenda.add(itemLegenda("Panico alto", new Color(190, 67, 54)));
        legenda.add(itemLegenda("Orientada", new Color(242, 201, 76)));
        legenda.add(itemLegenda("Saida", new Color(64, 150, 92)));
        legenda.add(itemLegenda("Risco", new Color(217, 132, 72)));
        legenda.add(itemLegenda("Obstaculo", new Color(83, 91, 97)));
        legenda.add(itemLegenda("Grafo", new Color(193, 203, 197)));
        return legenda;
    }

    private JComponent itemLegenda(String texto, Color cor) {
        JLabel item = new JLabel(texto);
        item.setIcon(new CorIcon(cor));
        return item;
    }

    private static class CorIcon implements Icon {
        private final Color cor;

        private CorIcon(Color cor) {
            this.cor = cor;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(cor);
            g.fillRect(x, y + 2, getIconWidth(), getIconHeight() - 4);
            g.setColor(new Color(120, 124, 121));
            g.drawRect(x, y + 2, getIconWidth(), getIconHeight() - 4);
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
    }
}
