package bridge;

public class TelevotoBean {
	private static String call_total;
	private static String call_not_answered;
	private static String call_on_negotiation;
	private static String call_established;
	private static String call_in_closing;
	private static String call_total_h;
	private static String call_closed_h;
	private static String call_lost_h;
	private static String call_cancelled_h;
	private static String agent_total;
	private static String agent_idle;
	private static String agent_busy;
	private static String WaitingTime;
	private static String QueueTime;
	private static String ArrivalRate;
	private static String averageWaitingTime;
	private static String averageQueueTime;
	private static String averageArrivalRate;
	
	public static String getCallTotal() {
		return call_total;
	}
	public static String getCallNotAnswered() {
		return call_not_answered;
	}
	public static String getCallOnNegotiation() {
		return call_on_negotiation;
	}
	public static String getCallEstablished() {
		return call_established;
	}
	public static String getCallInClosing() {
		return call_in_closing;
	}	
	public static String getCallTotalHistory() {
		return call_total_h;
	}
	public static String getCallClosedHistory() {
		return call_closed_h;
	}
	public static String getCallLostHistory() {
		return call_lost_h;
	}
	public static String getCallCancelledHistory() {
		return call_cancelled_h;
	}
	public static String getAgentTotal() {
		return agent_total;
	}
	public static String getAgentIdle() {
		return agent_idle;
	}
	public static String getAgentBusy() {
		return agent_busy;
	}
	public static String getWaitingTime() {
		return WaitingTime;
	}
	public static String getQueueTime() {
		return QueueTime;
	}
	public static String getArrivalRate() {
		return ArrivalRate;
	}
	
	public static void setCallTotal(String total) {
		call_total = total;
	}
	public static void setCallNotAnswered(String notAnswered) {
		call_not_answered = notAnswered;
	}
	public static void setCallOnNegotiation(String onNegotiation) {
		call_on_negotiation = onNegotiation;
	}
	public static void setCallEstablished(String established) {
		call_established = established;
	}
	public static void setCallInClosing(String inClosing) {
		call_in_closing = inClosing;
	}	
	public static void setCallTotalHistory(String total) {
		call_total_h = total;
	}
	public static void setCallClosedHistory(String closed) {
		call_closed_h = closed;
	}
	public static void setCallLostHistory(String lost) {
		call_lost_h = lost;
	}
	public static void setCallCancelledHistory(String canceled) {
		call_cancelled_h = canceled;
	}
	public static void setAgentTotal(String total) {
		agent_total = total;
	}
	public static void setAgentIdle(String idle) {
		agent_idle = idle;
	}
	public static void setAgentBusy(String busy) {
		agent_busy = busy;
	}
	public static void setWaitingTime(String averageWaiting) {
		TelevotoBean.WaitingTime = averageWaiting;
	}
	public static void setQueueTime(String averageQueue) {
		TelevotoBean.QueueTime = averageQueue;
	}
	public static void setArrivalRate(String arrivalRate) {
		ArrivalRate = arrivalRate;
	}
	public static String getAverageWaitingTime() {
		return averageWaitingTime;
	}
	public static void setAverageWaitingTime(String averageWaitingTime) {
		TelevotoBean.averageWaitingTime = averageWaitingTime;
	}
	public static String getAverageQueueTime() {
		return averageQueueTime;
	}
	public static void setAverageQueueTime(String averageQueueTime) {
		TelevotoBean.averageQueueTime = averageQueueTime;
	}
	public static String getAverageArrivalRate() {
		return averageArrivalRate;
	}
	public static void setAverageArrivalRate(String averageArrivalRate) {
		TelevotoBean.averageArrivalRate = averageArrivalRate;
	}
}