package br.ufabc.sma.evacuacao.relatorio;

public record ResultadoCenario(
        String cenario,
        String motivo,
        int ciclos,
        int totalPessoas,
        int pessoasEvacuadas,
        int pessoasNoPredio,
        int movimentosAceitos,
        int movimentosRejeitados,
        int movimentosEmRisco,
        int panicoMedio,
        int orientacoesBrigadista,
        int tempoMedioEvacuacao,
        int totalNos,
        int totalArestas,
        int distanciaMediaSaida,
        String execucaoId,
        long seed
) {

    public static ResultadoCenario deLinhaCsv(String linha) {
        String[] campos = linha.split(",", -1);
        boolean possuiCamposNovos = campos.length >= 17;
        boolean possuiTempoMedio = campos.length >= 15;

        if (campos.length < 14) {
            throw new IllegalArgumentException("Linha CSV incompleta: " + linha);
        }

        int indiceTempoMedio = possuiTempoMedio ? 11 : -1;
        int indiceTotalNos = possuiTempoMedio ? 12 : 11;
        int indiceTotalArestas = possuiTempoMedio ? 13 : 12;
        int indiceDistancia = possuiTempoMedio ? 14 : 13;
        int indiceExecucaoId = possuiCamposNovos ? 15 : -1;
        int indiceSeed = possuiCamposNovos ? 16 : -1;

        return new ResultadoCenario(
                campos[0],
                campos[1],
                Integer.parseInt(campos[2]),
                Integer.parseInt(campos[3]),
                Integer.parseInt(campos[4]),
                Integer.parseInt(campos[5]),
                Integer.parseInt(campos[6]),
                Integer.parseInt(campos[7]),
                Integer.parseInt(campos[8]),
                Integer.parseInt(campos[9]),
                Integer.parseInt(campos[10]),
                possuiTempoMedio ? Integer.parseInt(campos[indiceTempoMedio]) : 0,
                Integer.parseInt(campos[indiceTotalNos]),
                Integer.parseInt(campos[indiceTotalArestas]),
                Integer.parseInt(campos[indiceDistancia]),
                possuiCamposNovos ? campos[indiceExecucaoId] : "indefinido",
                possuiCamposNovos ? parseLongOuPadrao(campos[indiceSeed], 0L) : 0L
        );
    }

    private static long parseLongOuPadrao(String valor, long padrao) {
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            return padrao;
        }
    }
}
