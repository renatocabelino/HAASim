package br.ufes.inf.ngn.televoto.server.ras.logic;

import jade.osgi.service.agentFactory.AgentFactoryService;
import javax.media.protocol.DataSource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class Activator implements BundleActivator {

	private AgentFactoryService agentFactory;
	public static DataSource ds;
	
	public void start(BundleContext context) throws Exception {
		ds = null;
		agentFactory = new AgentFactoryService();
		agentFactory.init(context.getBundle());
	}

	public void stop(BundleContext context) throws Exception {
		agentFactory.clean();
	}

}
