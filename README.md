# Simulacao de Evacuacao com SMA

Projeto inicial de simulacao de evacuacao em predio usando Java, JADE e comunicacao FIPA ACL.

## Objetivo

Modelar pessoas evacuando um ambiente discreto, com agentes autonomos que percebem o ambiente, decidem uma acao e propoem movimentos para um agente gerenciador.

## Agentes Iniciais

- `AmbienteAgent`: representa o predio, valida movimentos e responde percepcoes.
- `PessoaAgent`: representa uma pessoa no predio, solicita percepcao e propoe movimentos rumo a saida.
- `ObservadorAgent`: acompanha ciclos, consulta metricas e encerra a simulacao.
- `BrigadistaAgent`: consulta o ambiente e orienta pessoas com base nas rotas do grafo.

## Grafo

O predio possui uma representacao inicial em grafo:

- nos: posicoes transitaveis;
- arestas: movimentos ortogonais validos, desenhadas no painel;
- metricas: total de nos, total de arestas, distancia media ate saida e gargalos;
- uso atual: sugestao de proxima posicao por caminho mais curto, considerando ocupacao e reservas.

## Saidas Geradas

Ao final da simulacao, o `ObservadorAgent` gera arquivos em:

```text
output/
```

Arquivos:

- `relatorio-<cenario>-<timestamp>.txt`: resumo textual da execucao.
- `resultados-cenarios.csv`: linha consolidada por execucao para comparacao entre cenarios.
- `metricas-ciclos.csv`: metricas registradas a cada ciclo da simulacao.
- `comparativo-cenarios.txt`: resumo automatico comparando execucoes registradas.
- `comparativo-apresentacao.txt` e `comparativo-apresentacao.md`: resumo enxuto para apresentacao.
- `estatisticas-cenarios.csv`: estatisticas consolidadas por cenario.

As metricas incluem evacuados, movimentos aceitos/rejeitados, movimentos em risco, panico medio, orientacoes do brigadista e tempo medio de evacuacao.

## Documentacao

- `ARQUITETURA_SMA.md`: agentes, tabela FIPA, cenarios e saidas.
- `PLANO_DESENVOLVIMENTO.md`: plano vivo e resumo do status atual.
- `texto_pibic.md`: texto academico principal do projeto.
- `RELATORIO_TECNICO.md`: nota tecnica complementar.
- `VALIDACAO_CENARIOS.md`: comandos e resultados esperados para smoke tests.

## Risco e Panico

O cenario `RISCO_CRESCENTE` adiciona celulas de risco ao predio. Pessoas que passam por areas de risco aumentam seu panico; movimentos rejeitados tambem elevam panico. Quando o panico fica alto, a decisao pode se tornar imperfeita.

## Brigadista

O `BrigadistaAgent` consulta o estado da simulacao, identifica uma pessoa distante da saida e envia orientacao por FIPA ACL. O ambiente registra a quantidade de orientacoes enviadas para comparar cenarios com e sem coordenacao auxiliar.

O dashboard tambem exibe a ultima orientacao enviada, indicando qual pessoa foi orientada e para qual posicao.

## Execucao

Classe principal:

```text
br.ufabc.sma.evacuacao.Main
```

Argumento opcional de cenario:

```text
COM_BRIGADISTA
SEM_BRIGADISTA
ALTO_CONGESTIONAMENTO
SAIDA_BLOQUEADA
RISCO_CRESCENTE
```

Argumentos opcionais de execucao:

```text
SEM_GUI
RAPIDO
```

Exemplo de validacao rapida sem interface:

```bash
java -cp /tmp/simulacao-evacuacao-build:../simulacao-jade/lib/jade.jar br.ufabc.sma.evacuacao.Main RISCO_CRESCENTE SEM_GUI RAPIDO
```

Dependencia necessaria:

```text
/home/bruno/projetos_jade/simulacao-jade/lib/jade.jar
```

## Validacao por Terminal

```bash
javac -cp ../simulacao-jade/lib/jade.jar -d /tmp/simulacao-evacuacao-build $(find src -name '*.java' | sort)
```

Para validar todos os cenarios:

```bash
./scripts/validar_cenarios.sh
```

Para executar repeticoes controladas por cenario, usando `SEM_BRIGADISTA` como baseline:

```bash
REPETICOES=5 ./scripts/executar_repeticoes.sh
```

Mais detalhes em `VALIDACAO_CENARIOS.md`.

## Proximos Passos

1. Rodar uma bateria maior de repeticoes para alimentar a analise do PIBIC.
2. Validar visualmente os cenarios no IntelliJ.
3. Refinar pesos dinamicos do grafo por risco e congestionamento.
4. Gerar graficos a partir dos CSVs.
