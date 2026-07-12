package br.ufabc.sma.evacuacao.relatorio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportadorRelatorio {

    private static final Path DIRETORIO_OUTPUT = Path.of("output");

    public Path salvar(RelatorioSimulacao relatorio) throws IOException {
        Files.createDirectories(DIRETORIO_OUTPUT);
        Path arquivo = DIRETORIO_OUTPUT.resolve(relatorio.nomeArquivo());
        Files.writeString(arquivo, relatorio.conteudo(), StandardCharsets.UTF_8);
        return arquivo;
    }
}
