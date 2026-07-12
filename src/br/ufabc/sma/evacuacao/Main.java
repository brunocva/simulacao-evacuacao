package br.ufabc.sma.evacuacao;

import br.ufabc.sma.evacuacao.agentes.AmbienteAgent;
import br.ufabc.sma.evacuacao.agentes.BrigadistaAgent;
import br.ufabc.sma.evacuacao.agentes.ObservadorAgent;
import br.ufabc.sma.evacuacao.agentes.PessoaAgent;
import br.ufabc.sma.evacuacao.modelo.CenarioSimulacao;
import br.ufabc.sma.evacuacao.utils.Configuracao;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static void main(String[] args) {
        try {
            configurarModoExecucao(args);
            configurarMetadadosExecucao();

            CenarioSimulacao cenario = CenarioSimulacao.deArgumentos(args);
            System.out.println("[Main] Iniciando cenario: " + cenario.name() + " - " + cenario.descricao());
            System.out.println("[Main] GUI JADE/Swing: " + (Configuracao.interfaceGraficaHabilitada() ? "habilitada" : "desabilitada"));
            System.out.println("[Main] Modo rapido: " + (Configuracao.modoRapido() ? "sim" : "nao"));

            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, Boolean.toString(Configuracao.interfaceGraficaHabilitada()));
            profile.setParameter(Profile.LOCAL_HOST, "127.0.0.1");
            if (!Configuracao.interfaceGraficaHabilitada()) {
                profile.setParameter(Profile.MAIN_PORT, String.valueOf(escolherPortaPrincipal()));
            }

            AgentContainer container = runtime.createMainContainer(profile);
            if (container == null) {
                throw new IllegalStateException("Nao foi possivel criar o container principal JADE.");
            }

            AgentController ambiente = container.createNewAgent(
                    "ambiente",
                    AmbienteAgent.class.getName(),
                    new Object[]{Configuracao.totalPessoas(cenario), cenario}
            );
            ambiente.start();

            for (int i = 1; i <= Configuracao.totalPessoas(cenario); i++) {
                AgentController pessoa = container.createNewAgent(
                        "pessoa-" + i,
                        PessoaAgent.class.getName(),
                        new Object[]{i}
                );
                pessoa.start();
            }

            if (cenario.usaBrigadista()) {
                AgentController brigadista = container.createNewAgent(
                        "brigadista-1",
                        BrigadistaAgent.class.getName(),
                        new Object[0]
                );
                brigadista.start();
            }

            AgentController observador = container.createNewAgent(
                    "observador",
                    ObservadorAgent.class.getName(),
                    new Object[]{cenario}
            );
            observador.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void configurarModoExecucao(String[] args) {
        if (args == null) {
            return;
        }

        for (String arg : args) {
            if ("SEM_GUI".equalsIgnoreCase(arg)) {
                System.setProperty("evacuacao.semGui", "true");
            } else if ("RAPIDO".equalsIgnoreCase(arg)) {
                System.setProperty("evacuacao.rapido", "true");
            }
        }
    }

    private static void configurarMetadadosExecucao() {
        if (System.getProperty("evacuacao.seed") == null || System.getProperty("evacuacao.seed").isBlank()) {
            System.setProperty("evacuacao.seed", String.valueOf(System.currentTimeMillis()));
        }
        if (System.getProperty("evacuacao.execucaoId") == null || System.getProperty("evacuacao.execucaoId").isBlank()) {
            System.setProperty(
                    "evacuacao.execucaoId",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            );
        }
    }

    private static int escolherPortaPrincipal() throws IOException {
        String portaConfigurada = System.getProperty("evacuacao.portaJade");
        if (portaConfigurada != null && !portaConfigurada.isBlank()) {
            return Integer.parseInt(portaConfigurada);
        }

        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            System.err.println("[Main] Nao foi possivel escolher porta JADE automaticamente. Usando 1099.");
            return 1099;
        }
    }
}
