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
