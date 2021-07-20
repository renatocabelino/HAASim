package bridge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
public class StartStopAgentsServlet extends HttpServlet {
	String ipras, ipgenerator;
	boolean flag = true;
	
	private void readFile() {
		flag = false;
		try { 
			FileReader arq = new FileReader("/televotoweb.conf"); 
			BufferedReader lerArq = new BufferedReader(arq); 
			String linha = lerArq.readLine();
			while (linha != null) {
				if (!linha.startsWith("#")) {
					switch ( (linha.split("="))[0] ) {
						case ("ipras"):
							ipras = linha.split("=")[1];
							break;
						case ("ipgenerator"):
							ipgenerator = linha.split("=")[1];
							break;
					}
				}
				linha = lerArq.readLine();
			}
			arq.close(); 
		} catch (IOException e) { 
			System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage()); 
			System.exit(0);
		} 
	}
 
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("resource")
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (flag) 
			readFile();
		try {
			DatagramSocket socket = new DatagramSocket();
			InetAddress destino = InetAddress.getByName( ipras );
			String mensagem;
			if ( Integer.parseInt(req.getParameter("qtt")) > 0 ) {
				mensagem = "agents:start="+ req.getParameter("qtt");
			}
			else
			{
				String regex = req.getParameter("qtt").replaceAll("-", "");
				mensagem = "agents:stop="+ regex;
			}
			byte[] dados = mensagem.getBytes();
			int porta = 9991;
			DatagramPacket pacote = new DatagramPacket(dados, dados.length, destino, porta);
			socket.send(pacote);
		} catch (IOException e) { e.printStackTrace(); }
		resp.sendRedirect("/televoto.server.web");
		resp.setHeader("REFRESH", "0");
    }
	 
}
