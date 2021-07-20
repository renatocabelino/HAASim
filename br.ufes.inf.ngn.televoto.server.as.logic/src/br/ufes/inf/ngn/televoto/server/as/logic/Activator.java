package br.ufes.inf.ngn.televoto.server.as.logic;

import jade.osgi.service.agentFactory.AgentFactoryService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private AgentFactoryService agentFactory;
	
	public void start(BundleContext context) throws Exception {				
		Bundle b = context.getBundle();	
		agentFactory = new AgentFactoryService();
		agentFactory.init(b);
	}

	public void stop(BundleContext context) throws Exception {
		agentFactory.clean();
	}

}
