# Validacao dos Cenarios

Este documento registra como validar a simulacao de evacuacao em modo rapido e sem interface grafica.

## Comando Manual

Compilar:

```bash
javac -Xlint:unchecked -cp ../simulacao-jade/lib/jade.jar -d /tmp/simulacao-evacuacao-build $(find src -name '*.java' | sort)
```

Executar um cenario:

```bash
java -cp /tmp/simulacao-evacuacao-build:../simulacao-jade/lib/jade.jar br.ufabc.sma.evacuacao.Main COM_BRIGADISTA SEM_GUI RAPIDO
```

## Script de Validacao

Executar todos os cenarios:

```bash
./scripts/validar_cenarios.sh
```

O script compila o projeto e executa:

- `COM_BRIGADISTA SEM_GUI RAPIDO`;
- `SEM_BRIGADISTA SEM_GUI RAPIDO`;
- `ALTO_CONGESTIONAMENTO SEM_GUI RAPIDO`;
- `SAIDA_BLOQUEADA SEM_GUI RAPIDO`;
- `RISCO_CRESCENTE SEM_GUI RAPIDO`.

O script tambem verifica se cada execucao registrou `EVACUACAO_CONCLUIDA`. Se algum cenario falhar, a validacao encerra com erro.

## Repeticoes Experimentais

Executar repeticoes controladas por cenario:

```bash
REPETICOES=5 ./scripts/executar_repeticoes.sh
```

Configuracoes opcionais:

```bash
SEED_BASE=20260712 REPETICOES=10 TIMEOUT_SECONDS=45 ./scripts/executar_repeticoes.sh
```

O baseline experimental e `SEM_BRIGADISTA`. As demais configuracoes sao comparadas contra ele nos arquivos `comparativo-cenarios.txt`, `comparativo-apresentacao.txt`, `comparativo-apresentacao.md` e `estatisticas-cenarios.csv`.

## Resultado Esperado

Cada cenario deve encerrar com:

```text
EVACUACAO_CONCLUIDA
```

Arquivos esperados em `output/`:

- `relatorio-<cenario>-<timestamp>.txt`;
- `resultados-cenarios.csv`;
- `metricas-ciclos.csv`;
- `comparativo-cenarios.txt`.
- `comparativo-apresentacao.txt`;
- `comparativo-apresentacao.md`.
- `estatisticas-cenarios.csv`.

## Validacoes Ja Realizadas

| Cenario | Resultado |
|---|---|
| `COM_BRIGADISTA` | `EVACUACAO_CONCLUIDA` |
| `SEM_BRIGADISTA` | `EVACUACAO_CONCLUIDA` |
| `ALTO_CONGESTIONAMENTO` | `EVACUACAO_CONCLUIDA` |
| `SAIDA_BLOQUEADA` | `EVACUACAO_CONCLUIDA` |
| `RISCO_CRESCENTE` | `EVACUACAO_CONCLUIDA` |

## Observacoes

- O modo `SEM_GUI` desabilita a interface Swing e a GUI do JADE.
- O modo `RAPIDO` reduz intervalos e limite de ciclos para smoke tests.
- Em modo `SEM_GUI`, o projeto escolhe uma porta local disponivel para reduzir conflito entre execucoes.
- O JADE precisa abrir socket local; se o ambiente bloquear sockets, execute pelo IntelliJ ou por terminal local.
