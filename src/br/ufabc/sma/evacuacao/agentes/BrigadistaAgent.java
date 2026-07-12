package br.ufabc.sma.evacuacao.agentes;

import br.ufabc.sma.evacuacao.modelo.EstadoSimulacao;
import br.ufabc.sma.evacuacao.modelo.Posicao;
import br.ufabc.sma.evacuacao.utils.Configuracao;
import br.ufabc.sma.evacuacao.utils.Conversas;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;

public class BrigadistaAgent extends Agent {

    private AID ambiente;

    @Override
    protected void setup() {
        registrarServico();
        ambiente = localizarAmbiente();

        addBehaviour(new TickerBehaviour(this, Configuracao.intervaloCicloMs() * 2L) {
            @Override
            protected void onTick() {
                if (ambiente == null) {
                    ambiente = localizarAmbiente();
                    return;
                }

                EstadoSimulacao estado = consultarEstado();
                if (estado != null && !estado.evacuacaoConcluida()) {
                    orientarPessoaMaisDistante(estado);
                }
            }
        });
    }

    private void orientarPessoaMaisDistante(EstadoSimulacao estado) {
        estado.posicoesPessoas()
                .entrySet()
                .stream()
                .filter(entry -> estado.sugestoesProximaPosicao().containsKey(entry.getKey()))
                .max(Comparator.comparingInt(entry -> distanciaAteSaida(entry.getValue(), estado)))
                .ifPresent(entry -> enviarOrientacao(entry.getKey(), estado.sugestoesProximaPosicao().get(entry.getKey())));
    }

    private int distanciaAteSaida(Posicao posicao, EstadoSimulacao estado) {
        return Math.abs(posicao.x() - estado.saidaPrincipal().x())
                + Math.abs(posicao.y() - estado.saidaPrincipal().y());
    }

    private void enviarOrientacao(String pessoaLocalName, Posicao proximaPosicao) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(pessoaLocalName, AID.ISLOCALNAME));
        msg.setConversationId(Conversas.ORIENTACAO_BRIGADISTA);

        try {
            msg.setContentObject(proximaPosicao);
            send(msg);
            registrarOrientacao(pessoaLocalName, proximaPosicao);
            System.out.println("[Brigadista] orientou " + pessoaLocalName + " para " + proximaPosicao);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registrarOrientacao(String pessoaLocalName, Posicao proximaPosicao) {
        if (ambiente == null) {
            return;
        }

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(ambiente);
        msg.setConversationId(Conversas.ORIENTACAO_REGISTRADA);
        msg.setContent(getLocalName() + " orientou " + pessoaLocalName + " para " + proximaPosicao);
        send(msg);
    }

    private EstadoSimulacao consultarEstado() {
        String conversa = Conversas.STATUS_SIMULACAO + "-" + getLocalName() + "-" + System.currentTimeMillis();

        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
        msg.addReceiver(ambiente);
        msg.setConversationId(conversa);
        msg.setContent(Conversas.STATUS_SIMULACAO);
        send(msg);

        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(conversa)
        );
        ACLMessage resposta = blockingReceive(mt, 500);
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

    private void registrarServico() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(Conversas.SERVICO_BRIGADISTA);
        sd.setName("Servico-Brigadista-Evacuacao");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
