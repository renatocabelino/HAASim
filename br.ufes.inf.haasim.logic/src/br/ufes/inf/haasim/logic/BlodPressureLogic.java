package br.ufes.inf.haasim.logic;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jade.core.Agent;

public class BlodPressureLogic extends Agent {
	private GeneratorProfiles gpBloodPressure = new GeneratorProfiles() {
	};
	private static final long serialVersionUID = 1L;
	private Log logger = (Log) LogFactory.getLog(BlodPressureLogic.class);
	protected void setup() {
		Object parametros[] = new Object[11];
		parametros = this.getArguments();
		System.out.println("Eu sou o agente hipertenso " + getLocalName());
		String pressao = aleatoriar(100, 119) + "," + aleatoriar(60, 89);
		System.out.println(pressao);
		System.out.println(gpBloodPressure.getTemplate("bloodpressure"));
	}
	 public static int aleatoriar(int minimo, int maximo) {
	        Random random = new Random();
	        return random.nextInt((maximo - minimo) + 1) + minimo;
	    }
}