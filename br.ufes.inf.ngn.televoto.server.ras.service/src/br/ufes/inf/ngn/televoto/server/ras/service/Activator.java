package br.ufes.inf.ngn.televoto.server.ras.service;

import jade.core.ContainerID;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.wrapper.AgentController;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

	@SuppressWarnings("rawtypes")
	private ServiceReference jadeRef;
	private JadeRuntimeService jrs;
	private AgentController ac;
	private boolean agentsasdemand, timeoutwaitingservice, checklocked, timeoutforwardingnextas;
	private int serverport, timeforwardingnextas, timewaitingservice, timeoutlocked;
	private String extension, domain, password, proxyaddress, proxyport, ipweb;

	@SuppressWarnings("unchecked")
	public void start(BundleContext context) throws Exception {
		Object parametros[] = new Object[14];
		
		jadeRef = context.getServiceReference(JadeRuntimeService.class.getName());
		
		String containerName = "Container-1";
		ContainerID destination = new ContainerID();
		destination.setName(containerName);
		
		//lendo arquivo de configuracao do televoto
		try { 
			FileReader arq = new FileReader("/tmp/televotoserverras.conf"); 
			BufferedReader lerArq = new BufferedReader(arq); 
			String linha = lerArq.readLine();
			while (linha != null) {
				if (!linha.startsWith("#")) {
					switch ( (linha.split("="))[0] ) {
						case ("serverport"):
							serverport = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("extension"):
							extension = linha.split("=")[1];
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
						case ("timeoutforwardingnextas"):
							if ( (linha.split("=")[1]).equals("yes") )
								timeoutforwardingnextas = true;
							else
								timeoutforwardingnextas = false; 
							break;
						case ("timeforwardingnextas"):
							timeforwardingnextas = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("timeoutwaitingservice"):
							if ( (linha.split("=")[1]).equals("yes") )
								timeoutwaitingservice = true;
							else
								timeoutwaitingservice = false;
							break;
						case ("timewaitingservice"):
							timewaitingservice = Integer.parseInt(linha.split("=")[1]);
							break;
						case ("agentsondemand"):
							if ( (linha.split("=")[1]).equals("yes") )
								agentsasdemand = true;
							else
								agentsasdemand = false;
							break;
						case ("checklocked"):
							if ( (linha.split("=")[1]).equals("yes") )
								checklocked = true;
							else
								checklocked = false;
							break;
						case ("timeoutlocked"):
							timeoutlocked = Integer.parseInt( linha.split("=")[1] );
							break;
						case ("ipweb"):
							ipweb = linha.split("=")[1];
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
		
		if(jadeRef != null) {
			jrs = (JadeRuntimeService) context.getService(jadeRef);
			try {				
				parametros[0] = extension;
				parametros[1] = domain;
				parametros[2] = password;
				parametros[3] = serverport;
				parametros[4] = proxyaddress;
				parametros[5] = proxyport;
				parametros[6] = timeoutforwardingnextas;
				parametros[7] = timeforwardingnextas;
				parametros[8] = timeoutwaitingservice;
				parametros[9] = timewaitingservice;
				parametros[10] = agentsasdemand;
				parametros[11] = checklocked;
				parametros[12] = timeoutlocked;
				parametros[13] = ipweb;

				ac = jrs.createNewAgent(parametros[0].toString(), "br.ufes.inf.ngn.televoto.server.ras.logic.Listener", parametros, "br.ufes.inf.ngn.televoto.server.ras.logic");
				ac.start();
			} catch(Exception e) { System.out.println("Cannot start Agent" + e); }
		} else {
			System.out.println("Cannot start Agent: JadeRuntimeService cannot be found");
		}
	}
	
	public void stop(BundleContext context) throws Exception {
		if(jadeRef != null) {
			ac = jrs.getAgent(extension);
			System.out.println(ac.getName() + " offline");
			ac.kill();
			context.ungetService(jadeRef);
		}
		
	}
}
