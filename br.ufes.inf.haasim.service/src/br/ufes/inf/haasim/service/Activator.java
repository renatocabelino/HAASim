package br.ufes.inf.haasim.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;


public class Activator implements BundleActivator {

	private static BundleContext context;
	@SuppressWarnings("rawtypes")
	private static ServiceReference jadeRef;
	private static JadeRuntimeService jrs;
	private AgentController ac;
	private static float creationtime;
	private static int i, k, firstextension, firstclientport, quantityextensions, redial, firstaudioport, timeoutwaitingservice, proxyport;
	private static String domain, password, proxyaddress, serviceextension, ipweb;;
	private static ArrayList<String> agents = new ArrayList<String>();
	private static PoissonDistribution pd;
	private static DatagramSocket clientSocket, serverSocket;
	private static Thread tSocket;
	private static jade.core.Runtime runtime;
	private static Profile profileContainer;
	private ContainerController containerHipertenso, containerDiabetico;
	
	/* Método interno para receber conexões via socket.
	 * @param String - tipo de mensagem que será enviada por Socket.
	 * */
	public void clientSocket(String tipo) {
		try {
			clientSocket = new DatagramSocket();
			InetAddress destino = InetAddress.getByName(ipweb);
			String mensagem = null;
			switch (tipo){
				case "data":
					mensagem = "data:agents="+quantityextensions+"-redial="+redial+"-frequency="+creationtime;
					break;
				case "agent": 
					mensagem = "agent:total="+agents.size();
					break;
			}
			System.out.println(mensagem);
			byte[] dados = mensagem.getBytes();
			int porta = 9998;
			DatagramPacket pacote = new DatagramPacket(dados, dados.length, destino, porta);
			clientSocket.send(pacote);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	/* Método externo para receber conexões via socket.
	 * */
	public class ServerSocket implements Runnable {
		public void run() {
			int porta = 9992;
		    DatagramPacket pacote = null;
		     byte[] dados;
		     System.out.println("Socket listening");
			 try {
			   	serverSocket = new DatagramSocket(porta);
			   	dados = new byte[100];
			   	pacote = new DatagramPacket(dados, dados.length);
			   	while (true) {
			   		serverSocket.receive(pacote);
			    	String mensagem = new String(pacote.getData(), 0, pacote.getLength() );
			    	//System.out.println(mensagem);
			     	switch (mensagem.split(":")[0]) {
			     	case "agents":
				     	int qtt = Integer.parseInt( (mensagem.split(":")[1]).split("=")[1] );
				     	if ( (mensagem.split(":")[1]).split("=")[0].equals("start") ) {
				     		if (agents.size() == 0)
				     			startAgents(qtt * quantityextensions / 100 );
				     		else
				     			startAgents(qtt * agents.size() / 100 );
				     	}
				     	break;
			     	}
			   	}
			} catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	/* Método interno para iniciar mais agentes chamadores.
	 * param int - quantidade de agentes que serão iniciados.
	 * */
	@SuppressWarnings("unchecked")
	private void startAgents(int qtt) {
		int lastExtension;
		Object parametros[] = new Object[11];
		if(jadeRef != null) {
			jrs = context.getService(jadeRef);
			
			try {
				if (agents.isEmpty()) {
					i = firstextension;
					k = 1;
					lastExtension = i + quantityextensions;
				}
				else {
					i = Integer.parseInt( agents.get( agents.size()-1 ) ) + 1;
					lastExtension = i + qtt;
				}
				for ( ; i<lastExtension; i++, k++) {
					parametros[0] = (String) Integer.toString(i);
					parametros[1] = domain;
					parametros[2] = password;
					parametros[4] = firstclientport+k; 
					parametros[5] = firstaudioport + 2*k-1;
					parametros[6] = proxyaddress;
					parametros[7] = proxyport;
					parametros[8] = serviceextension;
					parametros[9] = redial;
					parametros[10]= timeoutwaitingservice;
					agents.add( Integer.toString(i) );
					try {
					  Agent myAgent = new br.ufes.inf.haasim.logic.PacienteHipertenso();
					  ac = containerHipertenso.acceptNewAgent(parametros[0].toString(), myAgent);
					  ac.start();
					  //Thread.sleep( pd.sample() );
					} catch (StaleProxyException e) {
					    e.printStackTrace();
					}
				}				
			} catch(Exception e) { System.out.println("Cannot start Agent: " + e); }
		} else {
			System.out.println("Cannot start Agent: JadeRuntimeService cannot be found");
		}
		clientSocket("agent");
	}

	/* Método externo para ler o arquivo, e chamar o método para iniciar mais agentes chamadores.
	 * param BundleContext - contexto do bundle.
	 * */
	public void start(BundleContext bundleContext) throws Exception {	
		Activator.context = bundleContext;
		jadeRef = context.getServiceReference(JadeRuntimeService.class.getName());
		//Get the JADE runtime interface (singleton)
		runtime = jade.core.Runtime.instance();
		//Create a Profile, where the launch arguments are stored
		profileContainer = new ProfileImpl();
		profileContainer.setParameter(Profile.CONTAINER_NAME, "AgentesHipertensos");
		profileContainer.setParameter(Profile.MAIN_HOST, "192.168.0.213");
		profileContainer.setParameter(Profile.MAIN_PORT, "1099");
		profileContainer.setParameter(Profile.ACCEPT_FOREIGN_AGENTS, "true");
		//criando container para agentes do perfil hipertenso
		containerHipertenso = runtime.createAgentContainer(profileContainer);
		//criando container para agentes do perfil diabetico
		profileContainer = new ProfileImpl();
		profileContainer.setParameter(Profile.CONTAINER_NAME, "AgentesDiabeticos");
		profileContainer.setParameter(Profile.MAIN_HOST, "localhost");
		profileContainer.setParameter(Profile.ACCEPT_FOREIGN_AGENTS, "true");
		containerDiabetico = runtime.createAgentContainer(profileContainer);
		
		try {
			FileReader arq = new FileReader("/tmp/televotoclient.conf"); 
			BufferedReader lerArq = new BufferedReader(arq); 
			String linha = lerArq.readLine();
			while (linha != null) {
				if (!linha.startsWith("#")) {
					switch ( (linha.split("="))[0] ) {
						case ("firstclientport"):
							firstclientport = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("quantityextensions"):
							quantityextensions = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("firstextension"):
							firstextension = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("domain"):
							domain = linha.split("=")[1];
							break;
						case ("password"):
							password = linha.split("=")[1];
							break;
						case ("proxyaddress"):
							proxyaddress = linha.split("=")[1];
							break;
						case ("proxyport"):
							proxyport = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("firstaudioport"):
							firstaudioport = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("redial"):
							redial = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("timeoutwaitingservice"):
							timeoutwaitingservice = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("creationtime"):
							creationtime = Float.parseFloat(linha.split("=")[1]);
							pd = new PoissonDistribution((double) (1000 / creationtime) );
							break;
						case ("serviceextension"):
							serviceextension = linha.split("=")[1];
							break;
						case ("ipweb"):
							ipweb = linha.split("=")[1];
							break;
					}  
				}
				linha = lerArq.readLine();
			}
			arq.close(); 
			clientSocket("data");
			tSocket = new Thread( new ServerSocket() );
			tSocket.start();
			startAgents(quantityextensions);
		} catch (IOException e) { 
			System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage()); 
			System.exit(0);
		} 	
	}

	/* Método externo para desativar o agente, e remove-lo da lista.
	 * param String - nome do agente no formato nome.
	 * */
	public void takeOff(String id) throws Exception {
		if(jadeRef != null) {
			ac = jrs.getAgent(id);
			System.out.println(ac.getName() + " offline");
			ac.kill();
			agents.remove(id);
		}
		clientSocket("agent");
	}
	
	/* Método externo para parar os agentes.
	 * param BundleContext - contexto do bundle.
	 * */
	public synchronized void stop(BundleContext bundleContext) throws Exception {
		String agentName;
		if(jadeRef != null) {
			Iterator<String> it = agents.iterator();
			while ( it.hasNext() ) { 
				agentName = it.next();
				ac = jrs.getAgent(agentName);
				System.out.println(ac.getName() + " offline");
				ac.kill();
				Thread.sleep(500);
			}
			agents.clear();
			context.ungetService(jadeRef);
		}
		tSocket.interrupt();
		clientSocket.close();
		serverSocket.close(); //Dá erro, mas funciona	
		Activator.context = null;
	}
}