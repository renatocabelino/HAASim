package br.ufes.inf.ngn.televoto.server.as.service;

import jade.core.ContainerID;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.wrapper.AgentController;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import java.util.Iterator;

public class Activator implements BundleActivator {
	@SuppressWarnings("rawtypes")
	private ServiceReference jadeRef;
	private JadeRuntimeService jrs;
	private AgentController ac;
	private static String domain, password, proxyaddress, proxyport, audiofile;
	private static int k, quantityextensions, firstextension, firstserverport, audioformat, firstaudioport;
	private static ArrayList<String> agents;
	
	//-------------------------------------------------------------------------------
	// MÉTODOS PÚBLICOS
	
	/* Método externo para iniciar agentes. Geralmente chamado por outra classe.
	 * @param quantity - quantidade de agentes que serão encerrados.
	 * */
	public void start(int quantity) {
		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		startAgents(quantity, context);
	}

	/* Método externo para iniciar agentes. Geralmente chamado pela linha de comando OSGi.
	 * @param context - contexto do bundle.
	 * */
	public void start(BundleContext context) {	
		startAgents(0, context);
	}
	
	/* Método externo para encerrar agentes. Geralmente chamado por outra classe.
	 * @param list - nome dos agentes que serão parados
	 * */
	@SuppressWarnings("unchecked")
	public synchronized void stop(Object[] list) {
		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		jadeRef = context.getServiceReference(JadeRuntimeService.class.getName());
		int i; 
		String nome;
		boolean find = false;
		try {
			if(jadeRef != null) {
				jrs = (JadeRuntimeService) context.getService(jadeRef);		
				for (i=0; i<list.length; i++ ) {
					if (list[i] != null) {
						Iterator<String> ag = agents.iterator();
						while (ag.hasNext()) {
							if (ag.next().equals((String) list[i])) {
								find = true;
							}
						}
						if (find) {
							//System.out.println("Remover agente: " + list[i]);
							nome = ((String) list[i]).split("@")[0];
							ac = jrs.getAgent(nome);
							ac.kill();
							agents.remove(list[i]);
							System.out.println(nome + ": offline");
							Thread.sleep(500);
						}
					}
				}
				context.ungetService(jadeRef);
			}
		} catch (Exception e) {	e.printStackTrace(); }
		
	}
	
	/* Método externo para encerrar agentes. Geralmente chamado por outra classe.
	 * @param quantity - quantidade de agentes que serão encerrados.
	 * */
	public void stop(int quantity) {
		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		stopAgents(quantity, context);
	}
	
	/* Método externo para encerrar agentes. Geralmente chamado pela linha de comando OSGi.
	 * @param context - contexto do bundle.
	 * */
	public void stop(BundleContext context) {	
		stopAgents(agents.size(), context);
	}
	
	/* Método externo para reiniciar um agente "travado". Geralmente chamado por outra classe.
	 * @param int - id do agente.
	 * */
	public void restart(String id) {
		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		stopAgent(id, context);
		startAgent(id, context);
	}
	
	/* Método externo que retorna os agentes registrados. Geralmente chamado pela linha de comando OSGi.
	 * @return ArrayList<String> - retorna um ArrayList de nomes dos agentes no formato nome@dominio.
	 * */
	public ArrayList<String> getAgents() {
		return agents;
	}
	
	//-------------------------------------------------------------------------------
	// MÉTODOS PRIVADOS
	
	/* Método interno para iniciar agentes.
	 * @param quantity - quantidade de agentes que serão encerrados.
	 * */
	@SuppressWarnings("unchecked")
	private void startAgents(int quantity, BundleContext context) {
		Object parametros[] = new Object[9];
		ContainerID destination = new ContainerID();
		destination.setName( "Container-1" );
		jadeRef = context.getServiceReference(JadeRuntimeService.class.getName());
		int i, stopExtension;
		if(jadeRef != null) {
			jrs = (JadeRuntimeService) context.getService(jadeRef);			
			try { 
				if (quantity == 0) {
					readFile();
					agents = new ArrayList<String>();
					k = 1;
					i = firstextension;
					stopExtension = i + quantityextensions;
				}
				else {
					i = Integer.parseInt( agents.get(agents.size() - 1).split("@")[0] );
					i++;
					stopExtension = i + quantity;
				}
				
				for ( ; i<stopExtension; i++, k++) {	
					parametros[0] = (String) String.valueOf(i);
					parametros[1] = domain;
					parametros[2] = password;
					parametros[3] = firstserverport + k;
					parametros[4] = proxyaddress;
					parametros[5] = proxyport;
					parametros[6] = firstaudioport + 2*k-1;
					parametros[7] = audioformat;
					parametros[8] = audiofile;
					agents.add((String) String.valueOf(i) + "@" + domain);
					ac = jrs.createNewAgent(String.valueOf(i), "br.ufes.inf.ngn.televoto.server.as.logic.Listener", parametros, "br.ufes.inf.ngn.televoto.server.as.logic");
					ac.start();
					Thread.sleep(500);
				}				
			} catch(Exception e) { 	System.out.println("Cannot start Agent" + e); }
		} else {
			System.out.println("Cannot start Agent: JadeRuntimeService cannot be found");
		}
	}
	
	/* Método interno para iniciar agente que estava "travado".
	 * @param id - nome do agente que será inicializado.
	 * */
	@SuppressWarnings("unchecked")
	private void startAgent(String nome, BundleContext context) {
		Object parametros[] = new Object[9];
		ContainerID destination = new ContainerID();
		destination.setName( "Container-1" );
		jadeRef = context.getServiceReference(JadeRuntimeService.class.getName());
		if(jadeRef != null) {
			jrs = (JadeRuntimeService) context.getService(jadeRef);			
			try {
					parametros[0] = nome;
					parametros[1] = domain;
					parametros[2] = password;
					parametros[3] = firstserverport + k;
					parametros[4] = proxyaddress;
					parametros[5] = proxyport;
					parametros[6] = firstaudioport + 2*k-1;
					parametros[7] = audioformat;
					parametros[8] = audiofile;
					agents.add(nome);
					ac = jrs.createNewAgent(nome, "br.ufes.inf.ngn.televoto.server.as.logic.Listener", parametros, "br.ufes.inf.ngn.televoto.server.as.logic");
					ac.start();
			
			} catch(Exception e) { 	System.out.println("Cannot start Agent" + e); }
		} else {
			System.out.println("Cannot start Agent: JadeRuntimeService cannot be found");
		}
	}
	
	/* Método interno para encerrar agentes.
	 * @param quantity - quantidade de agentes que serão encerrados.
	 * @param context - contexto do bundle
	 * */
	@SuppressWarnings("unchecked")
	private void stopAgent(String nome, BundleContext context) {
		jadeRef = context.getServiceReference(JadeRuntimeService.class.getName());
		try {
			if(jadeRef != null) {
				jrs = (JadeRuntimeService) context.getService(jadeRef);		
				ac = jrs.getAgent(nome);
				agents.remove(ac);
				System.out.println(nome + " offline");
				ac.kill();
				context.ungetService(jadeRef);
			}
		} catch (Exception e) {	e.printStackTrace(); }
	}
	
	/* Método interno para encerrar agentes.
	 * @param quantity - quantidade de agentes que serão encerrados.
	 * @param context - contexto do bundle
	 * */
	@SuppressWarnings("unchecked")
	private void stopAgents(int quantity, BundleContext context) {
		int i; 
		int limit = agents.size()-1-quantity;
		jadeRef = context.getServiceReference(JadeRuntimeService.class.getName());
		String nome;
		try {
			if(jadeRef != null) {
				jrs = (JadeRuntimeService) context.getService(jadeRef);		
				if (quantity > agents.size())
					limit = firstextension;
				for (i=(agents.size()-1); i>limit; i--) {
					nome = agents.get(i).split("@")[0];
					ac = jrs.getAgent(nome);
					agents.remove(i);
					System.out.println(nome + " offline");
					ac.kill();
					Thread.sleep(500);
				}
				context.ungetService(jadeRef);
			}
		} catch (Exception e) {	e.printStackTrace(); }
	}
	
	/* Método interno para ler o arquivo de configuração.
	 * */
	private void readFile() {
		try { 
			FileReader arq = new FileReader("/televotoserveras.conf"); 
			BufferedReader lerArq = new BufferedReader(arq); 
			String linha = lerArq.readLine();
			while (linha != null) {
				if (!linha.startsWith("#")) {
					switch ( (linha.split("="))[0] ) {
						case ("firstserverport"):
							firstserverport = Integer.parseInt(linha.split("=")[1]);
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
							proxyport = linha.split("=")[1];
							break;
						case ("audioformat"):
							audioformat = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("firstaudioport"):
							firstaudioport = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("audiofile"):
							audiofile = linha.split("=")[1];
							break;
					}
				}
				linha = lerArq.readLine();
			}
			arq.close(); 
		} catch (IOException e) { 
			System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage()); 
			System.exit(0);
		}
	}

	
}
