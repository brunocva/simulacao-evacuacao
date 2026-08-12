package br.ufabc.sma.evacuacao.gui;

import br.ufabc.sma.evacuacao.modelo.Posicao;
import br.ufabc.sma.evacuacao.modelo.Predio;
import br.ufabc.sma.evacuacao.grafo.ArestaPredio;
import br.ufabc.sma.evacuacao.grafo.GrafoPredio;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;

public class PainelPredio extends JPanel {

    private static final int MARGEM = 32;
    private final Predio predio;
    private final GrafoPredio grafoPredio;
    private Map<String, Posicao> pessoas = new HashMap<>();
    private Map<String, Integer> panicos = new HashMap<>();
    private String pessoaOrientada;
    private boolean mostrarGrafo = true;

    public PainelPredio(Predio predio, GrafoPredio grafoPredio) {
        this.predio = predio;
        this.grafoPredio = grafoPredio;
        setPreferredSize(new Dimension(720, 500));
        setBackground(new Color(250, 250, 248));
    }

    public void atualizarPessoas(Map<String, Posicao> pessoas, Map<String, Integer> panicos, String pessoaOrientada) {
        this.pessoas = new HashMap<>(pessoas);
        this.panicos = new HashMap<>(panicos);
        this.pessoaOrientada = pessoaOrientada;
        repaint();
    }

    public void setMostrarGrafo(boolean mostrarGrafo) {
        this.mostrarGrafo = mostrarGrafo;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int larguraCelula = (getWidth() - (MARGEM * 2)) / predio.largura();
        int alturaCelula = (getHeight() - (MARGEM * 2)) / predio.altura();
        int tamanho = Math.max(24, Math.min(larguraCelula, alturaCelula));

        desenharGrade(g2, tamanho);
        if (mostrarGrafo) {
            desenharArestas(g2, tamanho);
        }
        desenharPessoas(g2, tamanho);

        g2.dispose();
    }

    private void desenharArestas(Graphics2D g2, int tamanho) {
        if (grafoPredio == null) {
            return;
        }

        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(new Color(193, 203, 197, 140));

        for (ArestaPredio aresta : grafoPredio.arestas()) {
            int x1 = MARGEM + aresta.origem().x() * tamanho + (tamanho / 2);
            int y1 = MARGEM + aresta.origem().y() * tamanho + (tamanho / 2);
            int x2 = MARGEM + aresta.destino().x() * tamanho + (tamanho / 2);
            int y2 = MARGEM + aresta.destino().y() * tamanho + (tamanho / 2);
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void desenharGrade(Graphics2D g2, int tamanho) {
        for (int y = 0; y < predio.altura(); y++) {
            for (int x = 0; x < predio.largura(); x++) {
                Posicao posicao = new Posicao(x, y);
                int px = MARGEM + x * tamanho;
                int py = MARGEM + y * tamanho;

                if (predio.ehSaida(posicao)) {
                    g2.setColor(new Color(64, 150, 92));
                } else if (predio.ehObstaculo(posicao)) {
                    g2.setColor(new Color(83, 91, 97));
                } else if (predio.ehRisco(posicao)) {
                    g2.setColor(new Color(217, 132, 72));
                } else {
                    g2.setColor(new Color(236, 238, 236));
                }

                g2.fillRect(px, py, tamanho, tamanho);
                g2.setColor(new Color(180, 184, 181));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRect(px, py, tamanho, tamanho);

                if (predio.ehSaida(posicao)) {
                    desenharTextoCentralizado(g2, "S", px, py, tamanho, Color.WHITE);
                } else if (predio.ehRisco(posicao)) {
                    desenharTextoCentralizado(g2, "R", px, py, tamanho, Color.WHITE);
                }
            }
        }
    }

    private void desenharPessoas(Graphics2D g2, int tamanho) {
        int indice = 1;
        for (Map.Entry<String, Posicao> entry : pessoas.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> indicePessoa(entry.getKey())))
                .toList()) {
            Posicao posicao = entry.getValue();
            int px = MARGEM + posicao.x() * tamanho;
            int py = MARGEM + posicao.y() * tamanho;
            int diametro = Math.max(14, tamanho - 14);
            int deslocamento = (tamanho - diametro) / 2;
            int panico = panicos.getOrDefault(entry.getKey(), 0);
            boolean orientada = entry.getKey().equals(pessoaOrientada);

            g2.setColor(panico >= 60 ? new Color(190, 67, 54) : new Color(47, 102, 170));
            g2.fillOval(px + deslocamento, py + deslocamento, diametro, diametro);
            g2.setColor(panico >= 60 ? new Color(118, 38, 32) : new Color(20, 54, 96));
            g2.drawOval(px + deslocamento, py + deslocamento, diametro, diametro);

            if (orientada) {
                g2.setColor(new Color(242, 201, 76));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(px + deslocamento - 4, py + deslocamento - 4, diametro + 8, diametro + 8);
                g2.setStroke(new BasicStroke(1f));
            }

            desenharTextoCentralizado(g2, String.valueOf(indice), px, py, tamanho, Color.WHITE);
            indice++;
        }
    }

    private int indicePessoa(String nome) {
        int separador = nome.lastIndexOf('-');
        if (separador >= 0 && separador < nome.length() - 1) {
            try {
                return Integer.parseInt(nome.substring(separador + 1));
            } catch (NumberFormatException ignored) {
                // usa ordenacao alfabetica no fallback abaixo
            }
        }
        return Integer.MAX_VALUE;
    }

    private void desenharTextoCentralizado(Graphics2D g2, String texto, int x, int y, int tamanho, Color cor) {
        g2.setColor(cor);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(12, tamanho / 3)));
        FontMetrics metrics = g2.getFontMetrics();
        int tx = x + (tamanho - metrics.stringWidth(texto)) / 2;
        int ty = y + ((tamanho - metrics.getHeight()) / 2) + metrics.getAscent();
        g2.drawString(texto, tx, ty);
    }
}
