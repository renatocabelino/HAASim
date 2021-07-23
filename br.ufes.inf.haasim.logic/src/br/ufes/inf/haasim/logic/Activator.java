package br.ufes.inf.haasim.logic;

import jade.osgi.service.agentFactory.AgentFactoryService;
import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private AgentFactoryService agentFactory;
	
	public static DataSource ds;
	public static AudioFormat afmt;


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
