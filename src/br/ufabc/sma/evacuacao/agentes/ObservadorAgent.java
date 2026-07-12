package br.ufabc.sma.evacuacao.agentes;

import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;
import br.ufabc.sma.evacuacao.modelo.EstadoSimulacao;
import br.ufabc.sma.evacuacao.relatorio.ComparadorCenarios;
import br.ufabc.sma.evacuacao.relatorio.ExportadorCicloCsv;
import br.ufabc.sma.evacuacao.relatorio.ExportadorCsv;
import br.ufabc.sma.evacuacao.relatorio.ExportadorRelatorio;
import br.ufabc.sma.evacuacao.relatorio.RelatorioSimulacao;
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
import jade.wrapper.ControllerException;

import java.io.IOException;
import java.nio.file.Path;

public class ObservadorAgent extends Agent {

    private AID ambiente;
    private final ExportadorCicloCsv exportadorCicloCsv = new ExportadorCicloCsv();
    private int cicloAtual = 0;
    private boolean finalizado = false;
    private CenarioSimulacao cenario = CenarioSimulacao.COM_BRIGADISTA;
    private EstadoSimulacao ultimoEstado;

    @Override
    protected void setup() {
        cenario = obterCenario();
        ambiente = localizarAmbiente();

        addBehaviour(new TickerBehaviour(this, Configuracao.intervaloCicloMs()) {
            @Override
            protected void onTick() {
                if (finalizado) {
                    stop();
                    return;
                }

                if (ambiente == null) {
                    ambiente = localizarAmbiente();
                    return;
                }

                cicloAtual++;
                informarCicloAtual();
                EstadoSimulacao estado = consultarEstado();

                if (estado != null) {
                    ultimoEstado = estado;
                    registrarMetricasDoCiclo(estado);
                    System.out.println("[Observador] ciclo " + cicloAtual
                            + " | no predio: " + estado.pessoasNoPredio()
                            + " | evacuadas: " + estado.pessoasEvacuadas());

                    if (estado.evacuacaoConcluida()) {
                        finalizar(estado, "EVACUACAO_CONCLUIDA");
                    } else if (cicloAtual >= Configuracao.maxCiclos()) {
                        finalizar(estado, "LIMITE_DE_CICLOS_ATINGIDO");
                    }
                }
            }
        });
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

    private void registrarMetricasDoCiclo(EstadoSimulacao estado) {
        try {
            exportadorCicloCsv.registrar(cenario, estado);
        } catch (IOException e) {
            System.err.println("[Observador] erro ao registrar metricas do ciclo: " + e.getMessage());
        }
    }

    private void informarCicloAtual() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(ambiente);
        msg.setConversationId(Conversas.CICLO_ATUAL);
        msg.setContent(String.valueOf(cicloAtual));
        send(msg);
    }

    private EstadoSimulacao consultarEstado() {
        String conversa = Conversas.STATUS_SIMULACAO + "-" + cicloAtual;

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

    private void finalizar(EstadoSimulacao estadoFinal, String motivo) {
        finalizado = true;

        EstadoSimulacao estadoParaSalvar = estadoFinal != null ? estadoFinal : ultimoEstado;
        if (estadoParaSalvar != null) {
            salvarRelatorio(estadoParaSalvar, motivo);
        }

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(ambiente);
        msg.setConversationId(Conversas.ENCERRAMENTO);
        msg.setContent(motivo);
        send(msg);

        System.out.println("[Observador] simulacao finalizada: " + motivo);
        encerrarPlataformaSeNecessario();
        doDelete();
    }

    private void encerrarPlataformaSeNecessario() {
        if (Configuracao.interfaceGraficaHabilitada()) {
            return;
        }

        new Thread(() -> {
            try {
                Thread.sleep(300);
                getContainerController().getPlatformController().kill();
            } catch (ControllerException e) {
                System.err.println("[Observador] erro ao encerrar plataforma: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "encerramento-plataforma-jade").start();
    }

    private void salvarRelatorio(EstadoSimulacao estadoFinal, String motivo) {
        RelatorioSimulacao relatorio = new RelatorioSimulacao(cenario, estadoFinal, motivo);
        try {
            Path arquivo = new ExportadorRelatorio().salvar(relatorio);
            System.out.println("[Observador] relatorio salvo em: " + arquivo.toAbsolutePath());
            Path csv = new ExportadorCsv().registrar(cenario, estadoFinal, motivo);
            System.out.println("[Observador] resultado CSV registrado em: " + csv.toAbsolutePath());
            Path comparativo = new ComparadorCenarios().gerarComparativo();
            System.out.println("[Observador] comparativo atualizado em: " + comparativo.toAbsolutePath());
            System.out.println("[Observador] resumo de apresentacao salvo em: "
                    + Path.of("output", "comparativo-apresentacao.txt").toAbsolutePath());
            System.out.println("[Observador] resumo markdown salvo em: "
                    + Path.of("output", "comparativo-apresentacao.md").toAbsolutePath());
            System.out.println("[Observador] estatisticas consolidadas salvas em: "
                    + Path.of("output", "estatisticas-cenarios.csv").toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Observador] erro ao salvar relatorio: " + e.getMessage());
        }
    }

    private CenarioSimulacao obterCenario() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof CenarioSimulacao) {
            return (CenarioSimulacao) args[0];
        }
        return CenarioSimulacao.COM_BRIGADISTA;
    }
}
