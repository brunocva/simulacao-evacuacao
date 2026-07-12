# Plano de Desenvolvimento - Simulacao de Evacuacao

## Objetivo

Criar um simulador simples de evacuacao em predio, inspirado na estrutura do projeto `infection`, mas documentado desde o inicio com tabela FIPA, normas de SMA e futura integracao com grafos.

## Etapas

1. Estrutura inicial do projeto Java/JADE.
2. Agentes minimos: ambiente e pessoas.
3. Comunicacao FIPA ACL documentada.
4. Modelo de predio em grade.
5. Visualizacao Swing.
6. Grafo de caminhos e comunicacao.
7. Agentes brigadistas.
8. Cenarios comparaveis.
9. Metricas e relatorio final.
10. Documentacao tecnica/cientifica.

## Status Atual

- Estrutura inicial criada.
- `AmbienteAgent` criado.
- `PessoaAgent` criado.
- `ObservadorAgent` criado.
- Modelo `Predio` e `Posicao` criado.
- Modelo `Celula`, `TipoCelula` e `EstadoSimulacao` criado.
- Dashboard Swing inicial criado.
- Ciclo logico inicial criado.
- Pacote `grafo` criado.
- `GrafoPredio` calcula nos, arestas, distancia media ate saida e gargalos.
- Pessoas consultam sugestao de movimento baseada em caminho mais curto.
- Sugestoes de movimento consideram ocupacao e reservas do ciclo.
- Dashboard mostra movimentos aceitos e rejeitados.
- `BrigadistaAgent` criado para orientar pessoas por FIPA ACL.
- Pessoas priorizam orientacao do brigadista quando valida.
- `CenarioSimulacao` criado.
- `Main` aceita argumento de cenario.
- Cenarios `COM_BRIGADISTA`, `SEM_BRIGADISTA`, `ALTO_CONGESTIONAMENTO` e `SAIDA_BLOQUEADA` iniciados.
- Relatorio textual final criado em `output/`.
- CSV consolidado `output/resultados-cenarios.csv` criado ao final da simulacao.
- CSV por ciclo `output/metricas-ciclos.csv` criado durante a simulacao.
- Comparativo automatico `output/comparativo-cenarios.txt` criado ao final da simulacao.
- Cenario `RISCO_CRESCENTE` criado.
- Celulas de risco e panico simples adicionados.
- Dashboard, relatorio e CSV incluem panico medio e movimentos em risco.
- Orientacoes do brigadista passaram a ser contabilizadas no dashboard, relatorio e CSV.
- Dashboard mostra a ultima orientacao enviada pelo brigadista.
- Tempo medio de evacuacao passou a ser calculado e exportado.
- `RELATORIO_TECNICO.md` criado para documentacao academica do projeto.
- Comparador de cenarios revisado para preferir maior evacuacao com menos ciclos e menos rejeicoes.
- Parser do CSV consolidado ficou mais robusto para linhas antigas e futuras.
- Modo `SEM_GUI` criado para validacao sem Swing/JADE GUI.
- Modo `RAPIDO` criado para smoke tests.
- Smoke tests executados com `COM_BRIGADISTA`, `SEM_BRIGADISTA`, `RISCO_CRESCENTE`, `ALTO_CONGESTIONAMENTO` e `SAIDA_BLOQUEADA`.
- Corrigido re-registro tardio de pessoas no `AmbienteAgent`.
- Porta JADE alternativa automatica adicionada no modo `SEM_GUI`.
- Script `scripts/validar_cenarios.sh` criado para validar todos os cenarios.
- `VALIDACAO_CENARIOS.md` criado com comandos e resultados esperados.
- Tabela FIPA inicial criada em `ARQUITETURA_SMA.md`.
- Baseline experimental consolidado como `SEM_BRIGADISTA`.
- Enum `BASE` removido para evitar ambiguidade com o baseline real.
- Script `scripts/executar_repeticoes.sh` ajustado para repeticoes controladas por cenario com `SEED_BASE`.
- Smoke tests passaram a verificar explicitamente `EVACUACAO_CONCLUIDA`.
- Comparativo de apresentacao passou a mostrar deltas de tempo, panico e rejeicoes em relacao ao baseline.
- `texto_pibic.md` passou a documentar a evolucao metodologica do experimento.
- Comparativo final passou a gerar leitura interpretativa automatica por cenario.
- CSV `output/estatisticas-cenarios.csv` adicionado para graficos e analises do artigo.
- Documentos redundantes foram reduzidos: `texto_pibic.md` ficou como texto academico principal e `RELATORIO_TECNICO.md` como nota tecnica curta.

## Planejamento Atual

Este planejamento registra os proximos passos combinados para deixar o projeto mais forte como simulador, como experimento de PIBIC e como material de apresentacao.

### Prioridade 1 - Interface grafica para demonstracao

Objetivo: melhorar o dashboard Swing para que a simulacao fique mais clara durante apresentacoes, bancas e gravacoes.

Melhorias previstas:

1. Adicionar legenda visual para pessoas, saida, risco, obstaculos e grafo.
2. Reorganizar as metricas em um painel lateral ou superior mais legivel.
3. Separar metricas principais em blocos: ciclo, pessoas no predio, evacuadas, rejeicoes, panico medio, tempo medio e orientacoes.
4. Destacar visualmente pessoas com estado especial, como panico elevado ou orientacao recente do brigadista.
5. Adicionar opcao para exibir ou ocultar o grafo no painel.
6. Melhorar espacamento, fontes, contraste e acabamento visual para capturas de tela.
7. Avaliar botoes de controle, como pausar, continuar, reiniciar e salvar imagem da tela.

Primeira entrega recomendada:

1. Legenda.
2. Painel de metricas reorganizado.
3. Alternancia de exibicao do grafo.
4. Destaque da ultima orientacao do brigadista.

### Prioridade 2 - Evidencias experimentais para o PIBIC

Objetivo: gerar dados suficientes para sustentar a discussao do `texto_pibic.md`.

Melhorias previstas:

1. Rodar bateria maior de repeticoes por cenario, usando `scripts/executar_repeticoes.sh`.
2. Preservar `SEED_BASE`, quantidade de repeticoes e data da bateria experimental.
3. Usar `output/estatisticas-cenarios.csv` para gerar graficos de media, desvio e deltas contra o baseline.
4. Usar `output/metricas-ciclos.csv` para gerar graficos de evolucao temporal.
5. Atualizar o `texto_pibic.md` com resultados observados somente depois da bateria maior.

### Prioridade 3 - Graficos e apresentacao

Objetivo: transformar as saidas CSV em material visual para relatorio e apresentacao.

Graficos recomendados:

1. Tempo medio de evacuacao por cenario.
2. Taxa de evacuacao por cenario.
3. Panico medio por cenario.
4. Movimentos rejeitados por cenario.
5. Deltas em relacao ao baseline `SEM_BRIGADISTA`.
6. Evolucao por ciclo para um ou dois cenarios representativos.

### Prioridade 4 - Refinamentos tecnicos futuros

Objetivo: melhorar a qualidade do modelo sem aumentar demais a complexidade.

Possibilidades:

1. Refinar pesos dinamicos do grafo considerando risco e congestionamento.
2. Testar novas politicas de decisao para `PessoaAgent`.
3. Comparar estrategias diferentes de `BrigadistaAgent`.
4. Registrar configuracao completa de cada execucao em arquivo de metadados.
5. Avaliar suporte a multiplas saidas ou bloqueios dinamicos.

## Repositorio

O projeto foi versionado em um repositorio Git proprio e enviado para:

```text
https://github.com/brunocva/simulacao-evacuacao
```
