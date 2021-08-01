package br.ufes.inf.haasim.logic;

import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public abstract class GeneratorProfiles {
	MustacheFactory mf = new DefaultMustacheFactory();
	Mustache myMustache;
	public GeneratorProfiles() {
		// TODO Auto-generated constructor stub
	}
	public String getTemplate(String nTemplate) {
		String template = null;
	    switch (nTemplate) {
			case "bloodpressure":
				template = "haasim.bloodpressure.mustache";
				break;
			case "glucose": 
				template = "haasim.glucose.mustache";
				break;
			case "hearthate":
				template = "haasim.heartrate.mustachehaasim.heartrate.mustache";
				break;
			case "step":
				template = "haasim.step.mustachehaasim.step.mustache";
				break;
			case "temperature": 
				template = "haasim.temperature.mustache";
				break;
		}
		return template;
	}

	public Map<String, Object> getContextData(String nGenerator, String nClassification, String nAgentName) {
		HashMap<String, Object> nContext = new HashMap<String, Object>();
		Instant now = Instant.now();
		ZonedDateTime zdt = ZonedDateTime.ofInstant(now,ZoneId.systemDefault());
		switch (nGenerator) {
		case "bloodpressure":
			switch (nClassification) {
			case "normal":
				nContext.put("systolic", aleatoriar(100, 119));
				nContext.put("diastolic", aleatoriar(60, 89));
				break;
			case "prehypertension":
				nContext.put("systolic", aleatoriar(120, 139));
				nContext.put("diastolic", aleatoriar(80, 89));
				break;
			case "hypertension1":
				nContext.put("systolic", aleatoriar(140, 159));
				nContext.put("diastolic", aleatoriar(90, 99));
				break;
			case "hypertension2":
				nContext.put("systolic", aleatoriar(160, 180));
				nContext.put("diastolic", aleatoriar(100, 120));
				break;
			}
			break;
		case "glucose":
			switch (nClassification) {
			case "normal":
				nContext.put("concentration", aleatoriar(60, 110));
				break;
			case "impaired":
				nContext.put("concentration", aleatoriar(100, 125));
				break;
			case "diabetes":
				nContext.put("concentration", aleatoriar(126, 250));
				break;
			}
			break;
		case "hearthate":
			switch (nClassification) {
			case "normal":
				nContext.put("bpm", aleatoriar(60, 100));
				break;
			case "tachycardic":
				nContext.put("bpm", aleatoriar(101, 150));
				break;
			case "bradycardic":
				nContext.put("bpm", aleatoriar(40, 59));
				break;
			}
			break;
		case "step":
			nContext.put("steps", aleatoriar(500, 7000));
			break;
		case "temperature":
			nContext.put("temperature", aleatoriar(33, 43));
			break;
		}
		nContext.put("user", nAgentName);
		nContext.put("date", zdt);
		nContext.put("result", 3);
		return nContext;
	}
	public String getData(String nGenerator, String nTemplate, Map<String, Object> nContext) {
		final StringWriter writer = new StringWriter();
		switch (nGenerator) {
			case "bloodpressure":
				myMustache = mf.compile(nTemplate);
			case "glucose":
				myMustache = mf.compile(nTemplate);
			case "hearthate":
				myMustache = mf.compile(nTemplate);
			case "step":
				myMustache = mf.compile(nTemplate);
			case "temperature":
				myMustache = mf.compile(nTemplate);
		}
		myMustache.execute(writer, nContext);
		return writer.toString();
	}
	
	public static int aleatoriar(int minimo, int maximo) {
		Random random = new Random();
		return random.nextInt((maximo - minimo) + 1) + minimo;
	}

}
