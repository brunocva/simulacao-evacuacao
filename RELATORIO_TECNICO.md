# Nota Tecnica - Simulacao de Evacuacao

Este arquivo foi reduzido para atuar como referencia tecnica curta. O texto academico principal do PIBIC esta em `texto_pibic.md`.

## Escopo

O projeto implementa uma simulacao de evacuacao em Java com JADE, agentes FIPA ACL e representacao do predio como grade e grafo.

## Componentes

- `AmbienteAgent`: mantem o estado global, valida movimentos, calcula metricas e responde consultas.
- `PessoaAgent`: representa uma pessoa, consulta percepcao/estado, decide movimento e reage a orientacoes.
- `BrigadistaAgent`: consulta o estado global e orienta uma pessoa distante da saida.
- `ObservadorAgent`: controla ciclos, registra metricas, encerra a simulacao e gera saidas.
- `Predio`: modela grade, obstaculos, saida e areas de risco.
- `GrafoPredio`: calcula adjacencias, caminhos, gargalos e sugestoes ponderadas.

## Cenarios

- `SEM_BRIGADISTA`: baseline experimental.
- `COM_BRIGADISTA`: avaliacao com coordenacao auxiliar.
- `ALTO_CONGESTIONAMENTO`: mais pessoas no predio.
- `SAIDA_BLOQUEADA`: obstaculo adicional proximo a saida.
- `RISCO_CRESCENTE`: areas de risco e impacto no panico.

## Saidas

- `output/relatorio-<cenario>-<timestamp>.txt`: relatorio textual por execucao.
- `output/resultados-cenarios.csv`: metricas finais por execucao.
- `output/metricas-ciclos.csv`: metricas por ciclo.
- `output/comparativo-cenarios.txt`: comparativo completo.
- `output/comparativo-apresentacao.txt`: resumo interpretativo para apresentacao.
- `output/comparativo-apresentacao.md`: resumo em Markdown.
- `output/estatisticas-cenarios.csv`: estatisticas consolidadas por cenario.

## Validacao

Smoke tests:

```bash
./scripts/validar_cenarios.sh
```

Repeticoes experimentais:

```bash
REPETICOES=5 ./scripts/executar_repeticoes.sh
```

## Limitacoes

- O modelo e abstrato e didatico, nao uma simulacao fisica real.
- O modelo de panico e simplificado.
- A validacao estatistica depende de uma bateria maior de repeticoes.
- Ainda nao ha geracao automatica de graficos.
