package br.ufes.inf.ngn.televoto.web;

public class TelevotoBean {
	private static int call_total;
	private static int call_closed;
	private static int call_losted;
	private static int call_canceled;
		
	public static int getCallTotal() {
		return call_total;
	}
	public static int getCallClosed() {
		return call_closed;
	}
	public static int getCallLosted() {
		return call_losted;
	}
	public static int getCallCanceled() {
		return call_canceled;
	}
	
	public static void setCallTotal(int total) {
		call_total = total;
	}
	public static void setCallClosed(int closed) {
		call_closed = closed;
	}
	public static void setCallLosted(int losted) {
		call_losted = losted;
	}
	public static void setCallCanceled(int canceled) {
		call_canceled = canceled;
	}
}
