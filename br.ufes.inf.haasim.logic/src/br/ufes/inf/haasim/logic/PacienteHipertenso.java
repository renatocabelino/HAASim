package br.ufes.inf.haasim.logic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import jade.core.Agent;

public class PacienteHipertenso extends Agent {
	
	private static final long serialVersionUID = 1L;
	private Log logger = (Log) LogFactory.getLog(PacienteHipertenso.class);
	
	protected void setup() {
		System.out.println("Eu sou o agente hipertenso " + getLocalName());
		
	}
}