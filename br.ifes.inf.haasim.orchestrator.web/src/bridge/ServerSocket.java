package bridge;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerSocket implements Runnable {
	static boolean on = false;
	@Override
	public void run() {
        int porta = 9998;
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
	    	     			TelevotoBean.setCallTotalHistory(Integer.parseInt(string[0].split("=")[1]));
	    	     			TelevotoBean.setCallClosedHistory(Integer.parseInt(string[1].split("=")[1]));
	    	     			TelevotoBean.setCallLostHistory(Integer.parseInt(string[2].split("=")[1]));
	     					break;
	     				case "agent":
	    	     			TelevotoBean.setAgentTotal(Integer.parseInt(string[0].split("=")[1]));
	     					break;
	     				case "data":
	    	     			TelevotoBean.setOrigAgents(Integer.parseInt(string[0].split("=")[1]));
	    	     			TelevotoBean.setOrigRedial(Integer.parseInt(string[1].split("=")[1]));
	    	     			TelevotoBean.setOrigFrequency(Float.parseFloat(string[2].split("=")[1]));
	     					break;
	     			}
	     		}
	     	} catch (IOException e) { e.printStackTrace(); }
     	}
	}
}
