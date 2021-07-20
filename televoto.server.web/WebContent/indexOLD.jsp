<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
import="java.io.IOException,
		java.net.DatagramPacket,
		java.net.DatagramSocket,
		bridge.TelevotoBean,
		bridge.ServerSocket,
		java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<% 
	//response.setIntHeader("Refresh", 1);
	//socket.disconnect();
	//socket.close();
%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Televoto's Report</title>
		<script src="jquery.js" type="text/javascript"></script>
		<!--script src="http://ajax.googleapis.com/ajax/libs/jquery/1.3/jquery.min.js" type="text/javascript"></script-->
		<style type="text/css">
		    .title { font-weight: bold; }    
		   	#container1 { float:left; width:100%; }
			#col1 { float:left; width:25%; }
			#col2 { float:left; width:25%; }
			#col3 { float:left; width:25%; }
			#col4 { float:left; width:25%; }
		</style>
		
		<script type="text/javascript">	  
			//Hour
	        jQuery(document).ready(function() {
	        	setInterval(function() {
	        		jQuery('#loadData').load("time.jsp");
	        		}, 1000);
	        	}
	        );
			//Total calls
	        jQuery(document).ready(function() {
	        	setInterval(function() {
	        		jQuery('#callTotal').load("callTotal.jsp");
	        		}, 1000);
	        	}
	        );
	      	//Closed call
	        jQuery(document).ready(function() {
	        	setInterval(function() {
	        		jQuery('#callClosed').load("callClosed.jsp");
	        		}, 1000);
	        	}
	        );
	      	//Losted call
	        jQuery(document).ready(function() {
	        	setInterval(function() {
	        		jQuery('#callLosted').load("callLosted.jsp");
	        		}, 1000);
	        	}
	        );
	     	//Canceled call
	        jQuery(document).ready(function() {
	        	setInterval(function() {
	        		jQuery('#callCanceled').load("callCanceled.jsp");
	        		}, 1000);
	        	}
	        );
	        //Agent total
	        jQuery(document).ready(function() {
	        	setInterval(function() {
	        		jQuery('#agentTotal').load("agentTotal.jsp");
	        		}, 1000);
	        	}
	        );
	        //Agent idle
	        jQuery(document).ready(function() {
	        	setInterval(function() {
	        		jQuery('#agentIdle').load("agentIdle.jsp");
	        		}, 1000);
	        	}
	        );
			//Agent busy
	        jQuery(document).ready(function() {
	        	setInterval(function() {
	        		jQuery('#agentBusy').load("agentBusy.jsp");
	        		}, 1000);
	        	}
	        );
			
		</script>
	</head>
	
	<body>
		<H1>Televoto's Report</H1>
		<p><!-- %= Calendar.getInstance().getTime() %--></p>
		<div id="loadData"> </div>
		
		<div id="container1">
			<div id="col1">
				<p class="title">Chamadas</p>
				<p>Totais: <span id="callTotal"></span> </p>
				<p>Encerradas: <span id="callClosed"></span> </p>
				<p>Perdidas: <span id="callLosted"></span> </p>
				<p>Canceladas: <span id="callCanceled"></span> </p>
			</div>
			
			<div id="col2">
				<p class="title">Chamadas em Tempo Real</p>
				
				<p>Totais: </p>
				<p>Não atendidas: </p>
				<p>Em negociação: </p>
				<p>Estabelecidas: </p>
				<p>Encerrando: </p>
			</div>
	    		
	    	<div id="col3">
	    		<p class="title">Agentes</p>
	    		<p>Totais: <span id="agentTotal"></span> </p>
	    		<p>Livres: <span id="agentIdle"></span> </p>
	    		<p>Ocupados: <span id="agentBusy"></span> </p>
			</div>
			
			<div id="col4">
				<p class="title">Chamadas e Agentes em Tempo Real</p>
			</div>	 
		</div>   
	</body>
</html>

<%	
	/*class ServerSocket implements Runnable {
		static boolean on = false;
		@Override
		public void run() {
	        int porta = 9999;
	     	DatagramSocket socket = null;
	     	DatagramPacket pacote = null;
	     	byte[] dados;
	     	if (on == false) {
		     	try {
		     		socket = new DatagramSocket(porta);
		     		dados = new byte[100];
		     		pacote = new DatagramPacket(dados, dados.length);
		     		System.out.println("Socket Escutando");
		     		on = true;
		     		while (true) {
		     			socket.receive(pacote);
		     			String mensagem = new String(pacote.getData(), 0, pacote.getLength() );
		     			String[] string = mensagem.split("-"); 
		     			TelevotoBean.setCallTotal(Integer.parseInt(string[0].split("=")[1]));
		     			TelevotoBean.setCallClosed(Integer.parseInt(string[1].split("=")[1]));
		     			TelevotoBean.setCallLosted(Integer.parseInt(string[2].split("=")[1]));
		     			TelevotoBean.setCallCanceled(Integer.parseInt(string[3].split("=")[1]));
		     		}
		     	} catch (IOException e) { e.printStackTrace(); }
	     	}
		}
	}*/

    Thread tSocket = new Thread( new ServerSocket() );
	tSocket.start();
%>