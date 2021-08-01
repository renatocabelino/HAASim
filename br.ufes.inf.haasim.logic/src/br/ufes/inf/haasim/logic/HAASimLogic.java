package br.ufes.inf.haasim.logic;
import java.util.Map;

import jade.core.Agent;

public class HAASimLogic extends Agent {
	private GeneratorProfiles gpHAASim = new GeneratorProfiles() { };
	private static final long serialVersionUID = 1L;
	protected void setup() {
		Object parametros[] = new Object[this.getArguments().length];
		parametros = this.getArguments();
		String myAgentName = this.getLocalName();
		String myGenerator = parametros[1].toString();
		String myClassification = parametros[2].toString();
		String myTemplate = gpHAASim.getTemplate(myGenerator);
		Map<String, Object> myContext = gpHAASim.getContextData(myGenerator, myClassification, myAgentName);
		System.out.println(gpHAASim.getData(myGenerator, myTemplate, myContext));
	}

}