package br.ufes.inf.haasim.logic;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jade.osgi.service.agentFactory.AgentFactoryService;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private AgentFactoryService agentFactory;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Bundle b = context.getBundle();
		
		agentFactory = new AgentFactoryService();
		agentFactory.init(b);

	}

	public void stop(BundleContext bundleContext) throws Exception {
		agentFactory.clean();
		Activator.context = null;
	}

}
