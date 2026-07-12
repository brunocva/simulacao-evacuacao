package br.ufabc.sma.evacuacao.modelo;

public enum CenarioSimulacao {
    COM_BRIGADISTA(true, "Com brigadista"),
    SEM_BRIGADISTA(false, "Sem brigadista"),
    ALTO_CONGESTIONAMENTO(true, "Alto congestionamento"),
    SAIDA_BLOQUEADA(true, "Saida bloqueada"),
    RISCO_CRESCENTE(true, "Risco crescente");

    private final boolean usaBrigadista;
    private final String descricao;

    CenarioSimulacao(boolean usaBrigadista, String descricao) {
        this.usaBrigadista = usaBrigadista;
        this.descricao = descricao;
    }

    public boolean usaBrigadista() {
        return usaBrigadista;
    }

    public String descricao() {
        return descricao;
    }

    public static CenarioSimulacao deArgumentos(String[] args) {
        if (args == null || args.length == 0 || args[0] == null || args[0].isBlank()) {
            return COM_BRIGADISTA;
        }

        try {
            return CenarioSimulacao.valueOf(args[0].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("[Main] Cenario desconhecido: " + args[0] + ". Usando COM_BRIGADISTA.");
            return COM_BRIGADISTA;
        }
    }
}
