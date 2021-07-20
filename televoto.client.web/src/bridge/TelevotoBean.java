package bridge;

public class TelevotoBean {
	private static int call_total_h;
	private static int call_closed_h;
	private static int call_lost_h;
	private static int agent_total;
	private static int orig_agent;
	private static int orig_redial;
	private static float orig_frequency;
	
	public static int getCallTotalHistory() {
		return call_total_h;
	}
	public static int getCallClosedHistory() {
		return call_closed_h;
	}
	public static int getCallLostHistory() {
		return call_lost_h;
	}
	public static int getAgentTotal() {
		return agent_total;
	}
	public static int getOrigAgents() {
		return orig_agent;
	}
	public static int getOrigRedial() {
		return orig_redial;
	}
	public static float getOrigFrequency() {
		return orig_frequency;
	}
	
	public static void setCallTotalHistory(int total) {
		call_total_h = total;
	}
	public static void setCallClosedHistory(int closed) {
		call_closed_h = closed;
	}
	public static void setCallLostHistory(int lost) {
		call_lost_h = lost;
	}
	public static void setAgentTotal(int total) {
		agent_total = total;
	}
	public static void setOrigAgents(int orig_agent) {
		TelevotoBean.orig_agent = orig_agent;
	}
	public static void setOrigRedial(int orig_redial) {
		TelevotoBean.orig_redial = orig_redial;
	}
	public static void setOrigFrequency(float orig_frequency) {
		TelevotoBean.orig_frequency = orig_frequency;
	}
}