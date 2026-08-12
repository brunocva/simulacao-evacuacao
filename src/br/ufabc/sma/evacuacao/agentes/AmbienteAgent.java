package br.ufabc.sma.evacuacao.agentes;

import br.ufabc.sma.evacuacao.grafo.GrafoPredio;
import br.ufabc.sma.evacuacao.grafo.MetricasGrafo;
import br.ufabc.sma.evacuacao.gui.DashboardFrame;
import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;
import br.ufabc.sma.evacuacao.modelo.EstadoSimulacao;
import br.ufabc.sma.evacuacao.modelo.Movimento;
import br.ufabc.sma.evacuacao.modelo.Posicao;
import br.ufabc.sma.evacuacao.modelo.Predio;
import br.ufabc.sma.evacuacao.utils.Configuracao;
import br.ufabc.sma.evacuacao.utils.Conversas;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AmbienteAgent extends Agent {

    private final Map<AID, Posicao> posicoes = new HashMap<>();
    private final Map<AID, Integer> panicos = new HashMap<>();
    private final Map<AID, Integer> cicloEntrada = new HashMap<>();
    private final Map<AID, Integer> temposEvacuacao = new HashMap<>();
    private Predio predio;
    private GrafoPredio grafoPredio;
    private MetricasGrafo metricasGrafo;
    private int totalPessoas = 0;
    private int pessoasEvacuadas = 0;
    private int movimentosAceitos = 0;
    private int movimentosRejeitados = 0;
    private int movimentosEmRisco = 0;
    private int orientacoesBrigadista = 0;
    private int cicloAtual = 0;
    private String ultimaOrientacaoBrigadista = "nenhuma";
    private String pessoaOrientadaBrigadista;
    private CenarioSimulacao cenario = CenarioSimulacao.COM_BRIGADISTA;
    private DashboardFrame dashboard;

    @Override
    protected void setup() {
        totalPessoas = obterTotalPessoas();
        cenario = obterCenario();
        predio = Predio.criarParaCenario(cenario);
        grafoPredio = GrafoPredio.criar(predio);
        metricasGrafo = grafoPredio.calcularMetricas();
        registrarServico();
        if (Configuracao.interfaceGraficaHabilitada()) {
            dashboard = new DashboardFrame(predio, grafoPredio, cenario);
            dashboard.exibir();
        }
        atualizarDashboard();
        addBehaviour(new ReceberMensagensBehaviour());
        System.out.println("[Ambiente] iniciado com cenario base de evacuacao.");
    }

    private class ReceberMensagensBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg == null) {
                block();
                return;
            }

            try {
                switch (msg.getPerformative()) {
                    case ACLMessage.INFORM:
                        processarInform(msg);
                        break;
                    case ACLMessage.QUERY_REF:
                        responderConsulta(msg);
                        break;
                    case ACLMessage.PROPOSE:
                        avaliarMovimento(msg);
                        break;
                    default:
                        responderNaoEntendido(msg);
                        break;
                }
            } catch (Exception e) {
                System.err.println("[Ambiente] erro ao processar mensagem: " + e.getMessage());
            }
        }
    }

    private int obterTotalPessoas() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof Integer) {
            return (Integer) args[0];
        }
        return 0;
    }

    private CenarioSimulacao obterCenario() {
        Object[] args = getArguments();
        if (args != null && args.length > 1 && args[1] instanceof CenarioSimulacao) {
            return (CenarioSimulacao) args[1];
        }
        return CenarioSimulacao.COM_BRIGADISTA;
    }

    private void processarInform(ACLMessage msg) throws UnreadableException {
        if (Conversas.CICLO_ATUAL.equals(msg.getConversationId())) {
            atualizarCiclo(msg);
        } else if (Conversas.ENCERRAMENTO.equals(msg.getConversationId())) {
            encerrarSimulacao(msg.getContent());
        } else if (Conversas.ORIENTACAO_REGISTRADA.equals(msg.getConversationId())) {
            registrarOrientacaoBrigadista(msg.getContent());
        } else if (Conversas.REGISTRO_PESSOA.equals(msg.getConversationId())) {
            registrarPessoa(msg);
        } else {
            responderNaoEntendido(msg);
        }
    }

    private void atualizarCiclo(ACLMessage msg) {
        try {
            cicloAtual = Integer.parseInt(msg.getContent());
            atualizarDashboard();
        } catch (NumberFormatException e) {
            System.err.println("[Ambiente] ciclo invalido recebido: " + msg.getContent());
        }
    }

    private void registrarPessoa(ACLMessage msg) throws UnreadableException {
        Object conteudo = msg.getContentObject();
        if (!(conteudo instanceof Posicao)) {
            return;
        }

        if (posicoes.containsKey(msg.getSender())) {
            return;
        }

        Posicao posicao = (Posicao) conteudo;
        posicoes.put(msg.getSender(), posicao);
        panicos.put(msg.getSender(), 10);
        cicloEntrada.put(msg.getSender(), cicloAtual);
        atualizarDashboard();
        System.out.println("[Ambiente] " + msg.getSender().getLocalName() + " registrado em " + posicao);
    }

    private void registrarOrientacaoBrigadista(String descricao) {
        orientacoesBrigadista++;
        if (descricao != null && !descricao.isBlank()) {
            ultimaOrientacaoBrigadista = descricao;
            pessoaOrientadaBrigadista = extrairPessoaOrientada(descricao);
        }
        atualizarDashboard();
    }

    private String extrairPessoaOrientada(String descricao) {
        String marcador = " orientou ";
        int inicio = descricao.indexOf(marcador);
        int fim = descricao.indexOf(" para ", inicio + marcador.length());
        if (inicio >= 0 && fim > inicio) {
            return descricao.substring(inicio + marcador.length(), fim).trim();
        }
        return null;
    }

    private void responderConsulta(ACLMessage msg) throws IOException {
        if (Conversas.STATUS_SIMULACAO.equals(msg.getContent())) {
            responderStatus(msg);
        } else {
            responderPercepcao(msg);
        }
    }

    private void responderStatus(ACLMessage msg) throws IOException {
        ACLMessage resposta = msg.createReply();
        resposta.setPerformative(ACLMessage.INFORM);
        resposta.setConversationId(msg.getConversationId());
        resposta.setContentObject(new EstadoSimulacao(
                cicloAtual,
                totalPessoas,
                posicoes.size(),
                pessoasEvacuadas,
                movimentosAceitos,
                movimentosRejeitados,
                movimentosEmRisco,
                panicoMedio(),
                orientacoesBrigadista,
                tempoMedioEvacuacao(),
                predio.saidaPrincipal(),
                metricasGrafo,
                posicoesPorNome(),
                sugestoesPorGrafo()
        ));
        send(resposta);
    }

    private void responderPercepcao(ACLMessage msg) throws IOException {
        ACLMessage resposta = msg.createReply();
        resposta.setPerformative(ACLMessage.INFORM);
        resposta.setConversationId(msg.getConversationId());
        resposta.setContentObject(predio);
        send(resposta);
    }

    private void avaliarMovimento(ACLMessage msg) throws UnreadableException {
        Object conteudo = msg.getContentObject();
        Movimento movimento = extrairMovimento(conteudo);
        if (movimento == null) {
            responderNaoEntendido(msg);
            return;
        }

        Posicao destino = movimento.destino();
        panicos.put(msg.getSender(), movimento.panicoAtual());
        cicloEntrada.putIfAbsent(msg.getSender(), cicloAtual);
        ACLMessage resposta = msg.createReply();

        if (movimentoPermitido(destino)) {
            movimentosAceitos++;
            if (predio.ehRisco(destino)) {
                movimentosEmRisco++;
            }
            resposta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            resposta.setContent(destino.x() + "," + destino.y());
            System.out.println("[Ambiente] movimento aceito para " + msg.getSender().getLocalName() + ": " + destino);

            if (predio.ehSaida(destino)) {
                posicoes.remove(msg.getSender());
                panicos.remove(msg.getSender());
                registrarTempoEvacuacao(msg.getSender());
                pessoasEvacuadas++;
                System.out.println("[Ambiente] " + msg.getSender().getLocalName() + " evacuou o predio.");
            } else {
                posicoes.put(msg.getSender(), destino);
            }
            atualizarDashboard();
        } else {
            movimentosRejeitados++;
            resposta.setPerformative(ACLMessage.REJECT_PROPOSAL);
            resposta.setContent("MOVIMENTO_INVALIDO");
        }

        send(resposta);
    }

    private Movimento extrairMovimento(Object conteudo) {
        if (conteudo instanceof Movimento) {
            return (Movimento) conteudo;
        }
        if (conteudo instanceof Posicao) {
            return new Movimento((Posicao) conteudo, 10);
        }
        return null;
    }

    private void responderNaoEntendido(ACLMessage msg) {
        ACLMessage resposta = msg.createReply();
        resposta.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        resposta.setContent("Mensagem nao reconhecida pelo ambiente.");
        send(resposta);
    }

    private void registrarServico() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(Conversas.SERVICO_AMBIENTE);
        sd.setName("Servico-Ambiente-Evacuacao");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void encerrarSimulacao(String motivo) {
        System.out.println("[Ambiente] encerrando simulacao: " + motivo);
        for (AID pessoa : posicoes.keySet()) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(pessoa);
            msg.setConversationId(Conversas.ENCERRAMENTO);
            msg.setContent(motivo);
            send(msg);
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

    private boolean movimentoPermitido(Posicao destino) {
        if (!predio.posicaoValida(destino)) {
            return false;
        }
        return predio.ehSaida(destino) || !posicoes.containsValue(destino);
    }

    private Map<String, Posicao> sugestoesPorGrafo() {
        Map<String, Posicao> sugestoes = new HashMap<>();
        Set<Posicao> ocupadas = new HashSet<>(posicoes.values());
        Set<Posicao> reservadas = new HashSet<>();

        for (Map.Entry<AID, Posicao> entry : posicoes.entrySet()) {
            Set<Posicao> bloqueadas = new HashSet<>(ocupadas);
            bloqueadas.addAll(reservadas);
            bloqueadas.remove(entry.getValue());

            List<Posicao> caminho = grafoPredio.caminhoMaisCurtoPonderado(
                    entry.getValue(),
                    bloqueadas,
                    ocupadas,
                    predio.posicoesRisco()
            );
            if (caminho.size() > 1) {
                Posicao proxima = caminho.get(1);
                sugestoes.put(entry.getKey().getLocalName(), proxima);
                if (!predio.ehSaida(proxima)) {
                    reservadas.add(proxima);
                }
            }
        }
        return sugestoes;
    }

    private Map<String, Posicao> posicoesPorNome() {
        return posicoes.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getLocalName(),
                        Map.Entry::getValue
                ));
    }

    private Map<String, Integer> panicosPorNome() {
        return panicos.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getLocalName(),
                        Map.Entry::getValue
                ));
    }

    private int panicoMedio() {
        if (panicos.isEmpty()) {
            return 0;
        }
        int soma = panicos.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
        return soma / panicos.size();
    }

    private void registrarTempoEvacuacao(AID pessoa) {
        Integer cicloInicial = cicloEntrada.get(pessoa);
        if (cicloInicial != null) {
            temposEvacuacao.put(pessoa, Math.max(0, cicloAtual - cicloInicial));
        }
    }

    private int tempoMedioEvacuacao() {
        if (temposEvacuacao.isEmpty()) {
            return 0;
        }
        int soma = temposEvacuacao.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
        return soma / temposEvacuacao.size();
    }

    private void atualizarDashboard() {
        if (dashboard == null) {
            return;
        }
        Map<String, Posicao> pessoas = posicoesPorNome();
        dashboard.atualizar(
                pessoas,
                pessoasEvacuadas,
                cicloAtual,
                movimentosAceitos,
                movimentosRejeitados,
                movimentosEmRisco,
                panicoMedio(),
                orientacoesBrigadista,
                tempoMedioEvacuacao(),
                ultimaOrientacaoBrigadista,
                pessoaOrientadaBrigadista,
                panicosPorNome(),
                metricasGrafo
        );
    }
}
