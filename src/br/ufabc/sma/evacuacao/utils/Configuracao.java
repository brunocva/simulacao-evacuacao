package br.ufabc.sma.evacuacao.utils;

import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;

public final class Configuracao {

    public static final int TOTAL_PESSOAS = 8;
    public static final int TOTAL_PESSOAS_ALTO_CONGESTIONAMENTO = 12;
    public static final int INTERVALO_CICLO_MS = 1000;
    public static final int INTERVALO_PESSOA_MS = 1200;
    public static final int MAX_CICLOS = 80;
    public static final int INTERVALO_CICLO_RAPIDO_MS = 120;
    public static final int INTERVALO_PESSOA_RAPIDO_MS = 140;
    public static final int MAX_CICLOS_RAPIDO = 35;

    public static int totalPessoas(CenarioSimulacao cenario) {
        if (cenario == CenarioSimulacao.ALTO_CONGESTIONAMENTO) {
            return TOTAL_PESSOAS_ALTO_CONGESTIONAMENTO;
        }
        return TOTAL_PESSOAS;
    }

    public static int intervaloCicloMs() {
        return modoRapido() ? INTERVALO_CICLO_RAPIDO_MS : INTERVALO_CICLO_MS;
    }

    public static int intervaloPessoaMs() {
        return modoRapido() ? INTERVALO_PESSOA_RAPIDO_MS : INTERVALO_PESSOA_MS;
    }

    public static int maxCiclos() {
        return modoRapido() ? MAX_CICLOS_RAPIDO : MAX_CICLOS;
    }

    public static boolean modoRapido() {
        return Boolean.getBoolean("evacuacao.rapido");
    }

    public static boolean interfaceGraficaHabilitada() {
        return !Boolean.getBoolean("evacuacao.semGui");
    }

    public static long sementeBase() {
        String valor = System.getProperty("evacuacao.seed");
        if (valor != null && !valor.isBlank()) {
            try {
                return Long.parseLong(valor);
            } catch (NumberFormatException ignored) {
                // usa fallback abaixo
            }
        }
        return 0L;
    }

    public static String identificadorExecucao() {
        String valor = System.getProperty("evacuacao.execucaoId");
        return (valor == null || valor.isBlank()) ? "indefinido" : valor;
    }

    public static CenarioSimulacao cenarioBase() {
        return CenarioSimulacao.SEM_BRIGADISTA;
    }

    private Configuracao() {
    }
}
