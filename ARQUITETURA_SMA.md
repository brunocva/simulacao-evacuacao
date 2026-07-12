# Arquitetura SMA - Simulacao de Evacuacao

## Agentes

| Agente | Papel | Tipo |
|---|---|---|
| `AmbienteAgent` | Gerencia o predio, registra pessoas, valida movimentos e responde percepcoes | ambiente/gerenciador |
| `PessoaAgent` | Percebe o ambiente, decide proxima posicao e propoe movimento | agente reativo |
| `ObservadorAgent` | Controla ciclos, consulta metricas e encerra a simulacao | observador/coordenador |
| `BrigadistaAgent` | Consulta o estado do ambiente e orienta pessoas com rotas sugeridas | coordenador auxiliar |

## Cenarios Experimentais

| Cenario | Descricao | Efeito |
|---|---|---|
| `COM_BRIGADISTA` | Cenario padrao com coordenacao auxiliar | cria `BrigadistaAgent` |
| `SEM_BRIGADISTA` | Baseline experimental sem coordenacao auxiliar | nao cria `BrigadistaAgent` |
| `ALTO_CONGESTIONAMENTO` | Cenario com mais pessoas | aumenta o total para 12 pessoas |
| `SAIDA_BLOQUEADA` | Cenario com bloqueio adicional perto da saida | altera obstaculos do predio |
| `RISCO_CRESCENTE` | Cenario com areas de risco | adiciona celulas de risco e influencia panico |

## Saidas de Analise

| Arquivo | Conteudo | Uso |
|---|---|---|
| `output/relatorio-<cenario>-<timestamp>.txt` | resumo final da execucao | leitura individual de uma simulacao |
| `output/resultados-cenarios.csv` | linha por execucao com metricas principais | comparacao entre cenarios |
| `output/metricas-ciclos.csv` | metricas por ciclo | graficos de evolucao e analise temporal |
| `output/comparativo-cenarios.txt` | resumo automatico das execucoes | leitura comparativa entre cenarios |
| `output/comparativo-apresentacao.txt` | resumo interpretativo enxuto | apoio para apresentacao |
| `output/comparativo-apresentacao.md` | resumo interpretativo em Markdown | apoio para relatorio e slides |
| `output/estatisticas-cenarios.csv` | medias, desvios e deltas por cenario | base para graficos e artigo |

Metricas principais:
- pessoas evacuadas;
- movimentos aceitos e rejeitados;
- movimentos em area de risco;
- panico medio;
- orientacoes do brigadista;
- tempo medio de evacuacao;
- metricas do grafo.

## Risco e Panico

- Celulas de risco aparecem como `R` no dashboard.
- Pessoas aumentam panico ao passar por celulas de risco.
- Movimentos rejeitados aumentam panico.
- Panico alto pode gerar decisao imperfeita.
- O ambiente registra panico medio e movimentos em area de risco.
- Orientacoes do brigadista sao registradas como metrica comparativa.

## Tabela FIPA Inicial

| Remetente | Destinatario | Performativa | Conteudo | Objetivo | Resposta Esperada |
|---|---|---|---|---|---|
| `PessoaAgent` | `AmbienteAgent` | `INFORM` | `Posicao` inicial | Registrar pessoa no ambiente | Nenhuma |
| `PessoaAgent` | `AmbienteAgent` | `QUERY_REF` | nome do agente | Solicitar percepcao do predio | `INFORM` com `Predio` |
| `AmbienteAgent` | `PessoaAgent` | `INFORM` | objeto `Predio` | Enviar percepcao do ambiente | Nenhuma |
| `PessoaAgent` | `AmbienteAgent` | `PROPOSE` | `Posicao` destino | Propor movimento | `ACCEPT_PROPOSAL` ou `REJECT_PROPOSAL` |
| `AmbienteAgent` | `PessoaAgent` | `ACCEPT_PROPOSAL` | coordenada aceita | Confirmar movimento valido | Pessoa atualiza posicao |
| `AmbienteAgent` | `PessoaAgent` | `REJECT_PROPOSAL` | `MOVIMENTO_INVALIDO` | Rejeitar movimento invalido | Pessoa tenta novamente em outro ciclo |
| `AmbienteAgent` | qualquer agente | `NOT_UNDERSTOOD` | erro textual | Indicar mensagem nao reconhecida | Revisao da mensagem |
| `ObservadorAgent` | `AmbienteAgent` | `INFORM` | ciclo atual | Atualizar ciclo logico | Ambiente atualiza dashboard |
| `ObservadorAgent` | `AmbienteAgent` | `QUERY_REF` | `status-simulacao` | Consultar estado atual | `INFORM` com `EstadoSimulacao` |
| `PessoaAgent` | `AmbienteAgent` | `QUERY_REF` | `status-simulacao` | Consultar sugestao de rota por grafo, posicoes ocupadas e metricas | `INFORM` com `EstadoSimulacao` |
| `BrigadistaAgent` | `AmbienteAgent` | `QUERY_REF` | `status-simulacao` | Consultar pessoas no predio e sugestoes de rota | `INFORM` com `EstadoSimulacao` |
| `BrigadistaAgent` | `PessoaAgent` | `INFORM` | `Posicao` sugerida | Orientar pessoa mais distante da saida | Pessoa prioriza orientacao se ainda for valida |
| `BrigadistaAgent` | `AmbienteAgent` | `INFORM` | nome do brigadista | Registrar orientacao enviada | Ambiente incrementa metrica |
| `ObservadorAgent` | `AmbienteAgent` | `INFORM` | motivo de encerramento | Encerrar por sucesso ou limite de ciclos | Ambiente notifica pessoas restantes |
| `AmbienteAgent` | `PessoaAgent` | `INFORM` | motivo de encerramento | Encerrar pessoas ainda no predio | Pessoa chama `doDelete()` |

## Normas SMA Representadas

- Autonomia: cada `PessoaAgent` decide seu movimento localmente.
- Reatividade: agentes respondem ao estado atual do predio.
- Interacao social: agentes comunicam com o ambiente via FIPA ACL.
- Ambiente compartilhado: o `AmbienteAgent` mantem a ocupacao e valida conflitos.
- Coordenacao: movimentos concorrentes sao aceitos ou rejeitados pelo ambiente.
- Observacao: o `ObservadorAgent` acompanha ciclos, metricas e criterio de parada.
- Cooperacao: o `BrigadistaAgent` orienta pessoas com base no estado global e nas rotas do grafo.

## Relacao com Grafos

O predio ja possui uma representacao inicial como grafo:

- nos: celulas transitaveis, saidas e pontos de interesse;
- arestas: caminhos possiveis entre celulas;
- pesos: custo inicial unitario de deslocamento;
- metricas: total de nos, total de arestas, distancia media ate saida e gargalos;
- decisao: o ambiente calcula sugestoes de proxima posicao pelo caminho mais curto;
- ocupacao: sugestoes evitam celulas ja ocupadas e reservas do ciclo atual;
- fallback: se nao houver sugestao, a pessoa usa heuristica local evitando celulas ocupadas.
