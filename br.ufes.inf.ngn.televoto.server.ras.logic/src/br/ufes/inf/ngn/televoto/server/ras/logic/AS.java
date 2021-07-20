package br.ufes.inf.ngn.televoto.server.ras.logic;

public class AS {
	
	private String id;
	private String identity;
	private int status; // Idle:0     Busy:1	
	static final int IDLE=0;
	static final int BUSY=1;
	
	public AS(String myID, String myIdentity, int myStatus) {
		id = myID;
		identity = myIdentity;
		status = myStatus;
	}
	
	public String getID() {
		return id;
	}
	
	public String getIdentity() {
		return identity;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int myStatus) {
		status = myStatus;
	}

}
