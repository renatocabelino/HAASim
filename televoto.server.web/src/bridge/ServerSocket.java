package bridge;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerSocket implements Runnable {
	static boolean on = false;
	@Override
	public void run() {
        int porta = 9999;
     	DatagramSocket socket = null;
     	DatagramPacket pacote = null;
     	byte[] dados;
     	if (on == false) {
     		System.out.println("Socket Escutando");
	     	try {
	     		socket = new DatagramSocket(porta);
	     		dados = new byte[100];
	     		pacote = new DatagramPacket(dados, dados.length);
	     		on = true;
	     		while (true) {
	     			socket.receive(pacote);
	     			String mensagem = new String(pacote.getData(), 0, pacote.getLength() );
	     			String[] string = (mensagem.split(":")[1]).split("-");
	     			//System.out.println(mensagem);
	     			switch (mensagem.split(":")[0]) {
	     				case "call":
	     					TelevotoBean.setCallTotal(string[0].split("=")[1]);
	     					TelevotoBean.setCallNotAnswered(string[1].split("=")[1]);
	     					TelevotoBean.setCallOnNegotiation(string[2].split("=")[1]);
	     					TelevotoBean.setCallEstablished(string[3].split("=")[1]);
	     					TelevotoBean.setCallInClosing(string[4].split("=")[1]);
	    	     			TelevotoBean.setCallTotalHistory(string[5].split("=")[1]);
	    	     			TelevotoBean.setCallClosedHistory(string[6].split("=")[1]);
	    	     			TelevotoBean.setCallLostHistory(string[7].split("=")[1]);
	    	     			TelevotoBean.setCallCancelledHistory(string[8].split("=")[1]);
	     					break;
	     				case "agent":
	    	     			TelevotoBean.setAgentTotal(string[0].split("=")[1]);
	    	     			TelevotoBean.setAgentIdle(string[1].split("=")[1]);
	    	     			TelevotoBean.setAgentBusy(string[2].split("=")[1]);
	     					break;
	     				case "time":
	    	     			TelevotoBean.setWaitingTime(string[0].split("=")[1]);
	    	     			TelevotoBean.setQueueTime(string[1].split("=")[1]);
	    	     			TelevotoBean.setArrivalRate(string[2].split("=")[1]);
	    	     			TelevotoBean.setAverageWaitingTime(string[3].split("=")[1]);
	    	     			TelevotoBean.setAverageQueueTime(string[4].split("=")[1]);
	    	     			TelevotoBean.setAverageArrivalRate(string[5].split("=")[1]);
	     					break;
	     			}
	     		}
	     	} catch (IOException e) { e.printStackTrace(); }
     	}
	}
}
