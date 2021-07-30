package br.ufes.inf.haasim.logic;

public abstract class GeneratorProfiles {
	public GeneratorProfiles() {
		// TODO Auto-generated constructor stub
	}
	public String getTemplate(String nTemplate) {
		String template = null;
	    switch (nTemplate) {
			case "bloodpressure":
				template = "{"+
						"            \"_uuid\": 1,"+
						"            \"_action\": \"data/record\","+
						"            \"_content\": [{"+
						"                \"data\": {"+
						"                    \"schema\": \"http://healthdash.com.br/blood_pressure_measurement/#1\","+
						"                     \"measurement\": {"+
						"                        \"systolic\": {{ systolic }},"+
						"                        \"diastolic\": {{ diastolic }}"+
						"                    },"+
						"                    \"date\": \"{{ date }}\""+
						"                },"+
						"                \"user\": \"{{ user }}\""+
						"            }]}";
				break;
			case "glucose": 
				template = "{"+
						"            \"_uuid\": 1,"+
						"            \"_action\": \"data/record\","+
						"            \"_content\": [{"+
						"                \"data\": {"+
						"                    \"schema\": \"http://healthdash.com.br/glucose_measurement/#1\","+
						"                     \"measurement\": {"+
						"                        \"concentration\": {{ concentration }},"+
						"                    },"+
						"                    \"date\": \"{{ date }}\""+
						"                },"+
						"                \"user\": \"{{ user }}\""+
						"            }]}";
				break;
			case "hearthate":
				template = "{"+
						"            \"_uuid\": 1,"+
						"            \"_action\": \"data/record\","+
						"            \"_content\": [{"+
						"                \"data\": {"+
						"                    \"schema\": \"http://healthdash.com.br/glucose_measurement/#1\","+
						"                     \"measurement\": {"+
						"                        \"bpm\": {{ bpm }},"+
						"                    },"+
						"                    \"date\": \"{{ date }}\""+
						"                },"+
						"                \"user\": \"{{ user }}\""+
						"            }]}";
				break;
			case "step":
				template = "{"+
						"            \"_uuid\": 1,"+
						"            \"_action\": \"data/record\","+
						"            \"_content\": [{"+
						"                \"data\": {"+
						"                    \"schema\": \"http://healthdash.com.br/glucose_measurement/#1\","+
						"                     \"measurement\": {"+
						"                        \"steps\": {{ steps }},"+
						"                    },"+
						"                    \"date\": \"{{ date }}\""+
						"                },"+
						"                \"user\": \"{{ user }}\""+
						"            }]}";
				break;
			case "temperature": 
				template = "{"+
						"            \"_uuid\": 1,"+
						"            \"_action\": \"data/record\","+
						"            \"_content\": [{"+
						"                \"data\": {"+
						"                    \"schema\": \"http://healthdash.com.br/glucose_measurement/#1\","+
						"                     \"measurement\": {"+
						"                        \"temperature\": {{ temperature }},"+
						"                        \"measurement_unit\": \"celcius\""+
						"                    },"+
						"                    \"date\": \"{{ date }}\""+
						"                },"+
						"                \"user\": \"{{ user }}\""+
						"            }]}";
				break;
		}
		return template;
	}
	public void getContextData(String nContext ) {
		
	}
	public void getData() {
		
	}

}
