package br.ufabc.sma.evacuacao.agentes;

import br.ufabc.sma.evacuacao.modelo.EstadoSimulacao;
import br.ufabc.sma.evacuacao.modelo.Movimento;
import br.ufabc.sma.evacuacao.modelo.Posicao;
import br.ufabc.sma.evacuacao.modelo.Predio;
import br.ufabc.sma.evacuacao.utils.Configuracao;
import br.ufabc.sma.evacuacao.utils.Conversas;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PessoaAgent extends Agent {

    private static final Posicao[] POSICOES_INICIAIS = {
            new Posicao(1, 1),
            new Posicao(2, 1),
            new Posicao(0, 1),
            new Posicao(1, 2),
            new Posicao(2, 2),
            new Posicao(0, 2),
            new Posicao(1, 3),
            new Posicao(2, 3),
            new Posicao(0, 3),
            new Posicao(1, 4),
            new Posicao(2, 4),
            new Posicao(0, 4)
    };

    private Random random;
    private AID ambiente;
    private Posicao posicao;
    private Posicao orientacaoBrigadista;
    private int panico = 10;
    private boolean evacuada = false;

    @Override
    protected void setup() {
        int identificador = obterIdentificador();
        random = new Random(Configuracao.sementeBase() + identificador);
        posicao = POSICOES_INICIAIS[Math.floorMod(identificador - 1, POSICOES_INICIAIS.length)];

        SequentialBehaviour inicializacao = new SequentialBehaviour(this);
        inicializacao.addSubBehaviour(new WakerBehaviour(this, 500) {
            @Override
            protected void onWake() {
                ambiente = localizarAmbiente();
            }
        });
        inicializacao.addSubBehaviour(new WakerBehaviour(this, 500) {
            @Override
            protected void onWake() {
                registrarNoAmbiente();
            }
        });
        addBehaviour(inicializacao);

        addBehaviour(new TickerBehaviour(this, Configuracao.intervaloPessoaMs()) {
            @Override
            protected void onTick() {
                if (evacuada) {
                    stop();
                    myAgent.doDelete();
                    return;
                }
                if (ambiente == null) {
                    ambiente = localizarAmbiente();
                    return;
                }
                perceberDecidirEProporMovimento();
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId(Conversas.ENCERRAMENTO);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    System.out.println("[" + getLocalName() + "] encerrando: " + msg.getContent());
                    doDelete();
                } else {
                    block();
                }
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchConversationId(Conversas.ORIENTACAO_BRIGADISTA);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    receberOrientacaoBrigadista(msg);
                } else {
                    block();
                }
            }
        });
    }

    private int obterIdentificador() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Integer) {
            int identificador = (Integer) args[0];
            return identificador > 0 ? identificador : 1;
        }
        return random.nextInt(POSICOES_INICIAIS.length) + 1;
    }

    private AID localizarAmbiente() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Conversas.SERVICO_AMBIENTE);
        template.addServices(sd);

        try {
            DFAgentDescription[] resultado = DFService.search(this, template);
            if (resultado.length > 0) {
                return resultado[0].getName();
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void registrarNoAmbiente() {
        if (ambiente == null) {
            return;
        }

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(ambiente);
        msg.setConversationId(Conversas.REGISTRO_PESSOA);

        try {
            msg.setContentObject(posicao);
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void perceberDecidirEProporMovimento() {
        Predio predio = solicitarPercepcao();
        if (predio == null || predio.ehSaida(posicao)) {
            return;
        }

        EstadoSimulacao estado = consultarEstadoSimulacao();
        Posicao destino = orientacaoValida(predio, estado) ? orientacaoBrigadista : null;
        orientacaoBrigadista = null;

        if (estado != null) {
            destino = destino == null ? estado.sugestoesProximaPosicao().get(getLocalName()) : destino;
        }
        if (destino == null) {
            destino = escolherProximaPosicao(predio, estado);
        }
        if (destino == null) {
            return;
        }

        String conversaMovimento = Conversas.PROTOCOLO_MOVIMENTO + "-" + getLocalName() + "-" + System.currentTimeMillis();
        ACLMessage proposta = new ACLMessage(ACLMessage.PROPOSE);
        proposta.addReceiver(ambiente);
        proposta.setProtocol(Conversas.PROTOCOLO_MOVIMENTO);
        proposta.setConversationId(conversaMovimento);

        try {
            proposta.setContentObject(new Movimento(destino, panico));
            send(proposta);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        MessageTemplate respostaMovimento = MessageTemplate.and(
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                        MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)
                ),
                MessageTemplate.and(
                        MessageTemplate.MatchConversationId(conversaMovimento),
                        MessageTemplate.MatchSender(ambiente)
                )
        );
        ACLMessage resposta = blockingReceive(respostaMovimento, 500);

        if (resposta != null && resposta.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
            posicao = destino;
            atualizarPanicoAposMovimento(predio, destino);
            System.out.println("[" + getLocalName() + "] moveu para " + posicao);
            if (predio.ehSaida(posicao)) {
                evacuada = true;
                System.out.println("[" + getLocalName() + "] chegou a saida.");
            }
        } else if (resposta != null && resposta.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
            panico = Math.min(100, panico + 8);
        }
    }

    private void receberOrientacaoBrigadista(ACLMessage msg) {
        try {
            Object conteudo = msg.getContentObject();
            if (conteudo instanceof Posicao) {
                orientacaoBrigadista = (Posicao) conteudo;
                System.out.println("[" + getLocalName() + "] recebeu orientacao do brigadista: " + orientacaoBrigadista);
            }
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
    }

    private boolean orientacaoValida(Predio predio, EstadoSimulacao estado) {
        return orientacaoBrigadista != null
                && predio.posicaoValida(orientacaoBrigadista)
                && posicaoLivre(orientacaoBrigadista, estado, predio);
    }

    private EstadoSimulacao consultarEstadoSimulacao() {
        String conversa = Conversas.STATUS_SIMULACAO + "-" + getLocalName() + "-" + System.currentTimeMillis();

        ACLMessage request = new ACLMessage(ACLMessage.QUERY_REF);
        request.addReceiver(ambiente);
        request.setConversationId(conversa);
        request.setContent(Conversas.STATUS_SIMULACAO);
        send(request);

        MessageTemplate respostaStatus = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(conversa)
        );
        ACLMessage resposta = blockingReceive(respostaStatus, 500);

        if (resposta == null) {
            return null;
        }

        try {
            return (EstadoSimulacao) resposta.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Predio solicitarPercepcao() {
        String conversa = Conversas.PERCEPCAO_AMBIENTE + "-" + getLocalName() + "-" + System.currentTimeMillis();

        ACLMessage request = new ACLMessage(ACLMessage.QUERY_REF);
        request.addReceiver(ambiente);
        request.setConversationId(conversa);
        request.setContent(getLocalName());
        send(request);

        MessageTemplate respostaPercepcao = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(conversa)
        );
        ACLMessage resposta = blockingReceive(respostaPercepcao, 500);

        if (resposta == null) {
            return null;
        }

        try {
            return (Predio) resposta.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Posicao escolherProximaPosicao(Predio predio, EstadoSimulacao estado) {
        List<Posicao> candidatos = List.of(
                new Posicao(posicao.x() + 1, posicao.y()),
                new Posicao(posicao.x() - 1, posicao.y()),
                new Posicao(posicao.x(), posicao.y() + 1),
                new Posicao(posicao.x(), posicao.y() - 1)
        );

        List<Posicao> validos = candidatos.stream()
                .filter(predio::posicaoValida)
                .filter(candidato -> posicaoLivre(candidato, estado, predio))
                .toList();

        if (validos.isEmpty()) {
            return null;
        }

        if (panico >= 60 && random.nextDouble() < 0.35) {
            return validos.get(random.nextInt(validos.size()));
        }

        return validos.stream()
                .filter(candidato -> !predio.ehRisco(candidato) || panico < 45)
                .min(Comparator.comparingInt(candidato -> distanciaManhattan(candidato, predio.saidaPrincipal())))
                .orElse(validos.get(0));
    }

    private void atualizarPanicoAposMovimento(Predio predio, Posicao destino) {
        if (predio.ehRisco(destino)) {
            panico = Math.min(100, panico + 20);
        } else {
            panico = Math.max(0, panico - 5);
        }
    }

    private boolean posicaoLivre(Posicao candidata, EstadoSimulacao estado, Predio predio) {
        if (predio.ehSaida(candidata) || estado == null) {
            return true;
        }

        for (Map.Entry<String, Posicao> entry : estado.posicoesPessoas().entrySet()) {
            if (!entry.getKey().equals(getLocalName()) && entry.getValue().equals(candidata)) {
                return false;
            }
        }
        return true;
    }

    private int distanciaManhattan(Posicao origem, Posicao destino) {
        return Math.abs(origem.x() - destino.x()) + Math.abs(origem.y() - destino.y());
    }
}
