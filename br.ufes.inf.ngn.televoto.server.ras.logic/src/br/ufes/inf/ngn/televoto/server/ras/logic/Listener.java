package br.ufes.inf.ngn.televoto.server.ras.logic;

import gov.nist.javax.sip.header.Allow;
import gov.nist.javax.sip.header.Authorization;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.Supported;
import gov.nist.javax.sip.header.ims.PPreferredIdentityHeader;
import gov.nist.javax.sip.header.ims.Privacy;
import jade.core.Agent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory; 
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;	
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import splibraries.Utils;

public class Listener extends Agent implements SipListener {
	private static final long serialVersionUID = 1L;
	private SipStack sipStack;
	private ListeningPoint listeningPoint;
	private SipProvider sipProvider;			
	private MessageFactory messageFactory; 	
	private HeaderFactory headerFactory;		
	private AddressFactory addressFactory;	
	private Properties properties;		
	private SipFactory sipFactory;
	private ClientTransaction clientTransaction;
	private Response responseReceived;
	private ContactHeader contactHeader;
	private Timer timer, timerCheckTime, timerCheckAgents;
	private boolean Unregistring = false;
	private boolean flagCheckAgents = true;
	private boolean flagStartStopAgents = true;
	private boolean flagStopAgents = true;
	private boolean agentsOnDemand;
	private boolean checkLocked;
	private boolean timeoutWaitingService;
	private boolean timeoutForwardingNextAS;
	private int status;
	private int timeForwardingNextAS;
	private int timeWaitingService;
	private int port;
	private int timeoutLocked;
	private int arrivalRateRealTime=0;
	private double tWaitingRealTime, tQueueRealTime;
	private double tWaitingAverage, tQueueAverage, averageArrivalRate;
	private double tQueueAverageEstimated = 0, taArrivalRateAverageEstimated = 0;
	private ArrayList<Double> taWaiting=new ArrayList<Double>(), taQueue=new ArrayList<Double>();
	private ArrayList<Integer> taArrive=new ArrayList<Integer>();
	private ArrayList<String> taWaitingAverage=new ArrayList<String>(), taQueueAverage=new ArrayList<String>(), taArrivalRateAverage=new ArrayList<String>();
	private ArrayList<String> taEstablished=new ArrayList<String>(), taWaitingQueue=new ArrayList<String>(), taCanceled=new ArrayList<String>();
	private String ip;
	private String extension;
	private String name;
	private String userID;
	private String password;
	private String domain;
	private String proxy;
	private String ipweb;
	private DatagramSocket serverSocket, clientSocket;
	private br.ufes.inf.ngn.televoto.server.as.service.Activator asService;
	private Thread tCheckTime, tSocket, tFirstAS;
	private int callNotAnswered=0, callOnNegotiation=0, callEstablished=0, callInClosing=0;
       
    private static ArrayList<RAS> RASs;
    private static ArrayList<AS> ASsRegistered;
    private static Queue<AS> ASsIdles;
    
    private static int call_total = 0;
    private static int call_closed = 0;
    private static int call_lost = 0;
    private static int call_canceled = 0;
    
	static final int UNREGISTERED=-2;
	static final int REGISTERING=-1;
	static final int IDLE=0;
	static final int WAIT_PROV=1;
	static final int WAIT_FINAL=2;
	static final int ESTABLISHED=4;
	static final int RINGING=5;
	static final int WAIT_ACK=6;
	private final static int NOTANSWERED = 0;
	private final static int ONNEGOTIATION = 1;
	private final static int INCLOSING = -1;
	private final static float ALFA = (float)1/8;

	private Log logger = (Log) LogFactory.getLog(Listener.class);
	
	/* Método externo para configurar e iniciar o agente.
	 * */
	@SuppressWarnings("deprecation")
	public void setup () {
		try {
			Object parametros[] = new Object[14];
			parametros = getArguments();
			extension 	= (String) parametros[0];
			domain 		= (String) parametros[1]; 
			password	= (String) parametros[2];
			port   	   	= (Integer) parametros[3]; // Porta SIP de escuta do serviço televoto
			proxy 		= ((String) parametros[4])	+ ":" + ((String)parametros[5]) + "/UDP";
			timeoutForwardingNextAS = (Boolean) parametros[6];
			timeForwardingNextAS = (Integer) parametros[7];
			timeoutWaitingService 	= (Boolean) parametros[8];
			timeWaitingService      = (Integer) parametros[9];
			agentsOnDemand = (Boolean) parametros[10]; 
			checkLocked = (Boolean) parametros[11]; 
			timeoutLocked = (Integer) parametros[12];
			ipweb = (String) parametros[13];
			userID = (String) extension + "@" + domain;
			name = (String) this.getLocalName();
			ip = InetAddress.getLocalHost().getHostAddress();	
			sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");
			properties = new Properties();
			properties.setProperty("javax.sip.STACK_NAME", name);
			properties.setProperty("javax.sip.OUTBOUND_PROXY", proxy);
			sipStack = sipFactory.createSipStack(properties);
			messageFactory = sipFactory.createMessageFactory();
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
				
			ListIterator<?> provider = (ListIterator<?>) sipStack.getSipProviders();
			int i;
			for ( i=0 ; provider.hasNext() ; ++i ) provider.next();
			if (i > 0) {
				Object sipProvider = (SipProvider) provider.previous();
				sipProvider = ((SipProvider) sipProvider);
				listeningPoint = ((SipProvider) sipProvider).getListeningPoint();
			} else {
				listeningPoint = sipStack.createListeningPoint(ip, port, "udp");
				sipProvider = sipStack.createSipProvider(listeningPoint);
				sipProvider.addSipListener(this);
			}
		} catch (Exception e) { e.printStackTrace(); }
		register(1);
		ASsRegistered = new ArrayList<AS>();
		ASsIdles = new LinkedList<AS>();
		startAgents(0);
		
		tCheckTime = new Thread( new CheckTime() );
		tCheckTime.start();
		
		if (agentsOnDemand) {
			timerCheckAgents = new Timer();
			timerCheckAgents.schedule(new CheckAgents(),1*1000);
		}
	}
	
	/* Método externo para finalizar o agente e salva os dados no arquivo.
	 * */
	public void takeDown() {
		Unregistring = true;
		register(1);
		try {
			FileWriter fw = new FileWriter("/televotodata.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Waiting Time: " + taWaitingAverage);
			bw.newLine();
			bw.write("Queue Time: " + taQueueAverage);
			bw.newLine();
			bw.write("Arrival Rate" + taArrivalRateAverage);
			bw.newLine();
			bw.write("Call Queue: " + taWaitingQueue);
			bw.newLine();
			bw.write("Call Established: " + taEstablished);
			bw.newLine();
			bw.write("Call Canceled: " + taCanceled);
			bw.close();
			System.out.println("Dados salvos em /televotodata.txt");
			logger.info("Dados salvos em /televotodata.txt");
		} catch (IOException e) { e.printStackTrace(); }
		clientSocket.close();
		serverSocket.close();
		tCheckTime.interrupt();
		/*int i;
		for (i=0; i<RASs.size(); i++ ) {
			RASs.get(i).stop();
		}*/
	}	

	/* Método externo para processar requisições SIP.
	 * @param requestReceivedEvent - evento de requisição SIP.
	 * */
	public synchronized void processRequest(RequestEvent requestReceivedEvent) {
		String method = requestReceivedEvent.getRequest().getMethod();
		Iterator<RAS> it = RASs.iterator();
		String myCallID = ((CallIdHeader) (requestReceivedEvent.getRequest()).getHeader("Call-ID")).getCallId();
		boolean find = false;
		while (it.hasNext()) {
			RAS ras = it.next();
			if ( ras.getCallID().equals(myCallID) ) {
				ras.processRequest(requestReceivedEvent);
				find = true;
				break;
			}
		}
		if (!find) {			
			if (method.equals("INVITE")) {
				Calendar t = Calendar.getInstance();
				RAS ras = new RAS(myCallID, t);
				RASs.add(ras);
				ras.processRequest(requestReceivedEvent);
			} 
		}
	}

	/* Método externo para processar respostas SIP.
	 * @param responseReceivedEvent - evento de resposta SIP.
	 * */
	public synchronized void processResponse(ResponseEvent responseReceivedEvent) {
		Response myResponseReceived = responseReceivedEvent.getResponse();
		clientTransaction = responseReceivedEvent.getClientTransaction();
		if (status == REGISTERING) {
			int myStatusCode = myResponseReceived.getStatusCode();
			switch (myStatusCode) {
				//200 OK
				case (200):
					status=IDLE;
					if (!Unregistring) {
						System.out.println(name + ": online");
						logger.info(name + ": online");
						tSocket = new Thread( new ServerSocket() );
						tSocket.start();
						timer = new Timer();
						timer.schedule(new KeepAlive(),55*60*1000);//Re-register em 55 minutos
						RASs = new ArrayList<RAS>();
					} else {
						try{
							sipProvider.removeSipListener(this);
							sipProvider.removeListeningPoint(listeningPoint);
							sipStack.deleteListeningPoint(listeningPoint);
							sipStack.deleteSipProvider(sipProvider);
							listeningPoint=null;
							sipProvider=null;
							sipStack=null;
						}
						catch(Exception e){}
					}
					break;
				//401 Unauthorized
				case (401):
					responseReceived = myResponseReceived;
					register(2);
					break;
				//403 Forbbidden
				case (403):
					System.out.println("Problemas com credenciais!!!!\n");
					logger.info("Problemas com credenciais!!!!\n");
					break;
			}
		} else {
			Iterator<RAS> it = RASs.iterator();
			String myCallID = ((CallIdHeader) (responseReceivedEvent.getResponse()).getHeader("Call-ID")).getCallId();
			while (it.hasNext()) {
				RAS ras = it.next();
				if ( ras.getCallID().equals(myCallID)) {
					ras.processResponse(responseReceivedEvent);
					break;
				}
			}
		}
	}

	public void processTimeout(TimeoutEvent timeoutEvent) {	}

	public void processTransactionTerminated(TransactionTerminatedEvent tevent) {}

	public void processDialogTerminated(DialogTerminatedEvent tevent) {}

	public void processIOException(IOExceptionEvent tevent) {}
	
	/* Método externo para receber conexões via socket.
	 * */
	public class ServerSocket implements Runnable {

		public void run() {
			int porta = 9991;
	     	DatagramPacket pacote = null;
	     	byte[] dados;
	     	System.out.println("Socket listening");
	     	logger.info("Socket listening");
		    try {
		    	serverSocket = new DatagramSocket(porta);
		     	dados = new byte[100];
		     	pacote = new DatagramPacket(dados, dados.length);
		     	while (true) {
		     		serverSocket.receive(pacote);
			     	String message = new String(pacote.getData(), 0, pacote.getLength() );
			     	switch (message.split(":")[0]) {
			     	case "agents":
			     		if (flagStartStopAgents) {
				     		flagStartStopAgents = false;
				     		int qtt = Integer.parseInt( (message.split(":")[1]).split("=")[1] );
				     		if ( (message.split(":")[1]).split("=")[0].equals("start") ) {
				     			startAgents(qtt * ASsRegistered.size() / 100 );
				     		} else if ((message.split(":")[1]).split("=")[0].equals("stop")) {
				     			stopAgents(qtt * ASsRegistered.size() / 100 );
				     		}
				     		break;
				     	}
			     		else {
			     			System.out.println("Cannot start/stop agents at the time.");
			     			logger.info("Cannot start/stop agents at the time.");			     		}
			     	}
		     	}
		     } catch (IOException e) { e.printStackTrace(); }	
		}
	}
	
	/* Método interno para receber conexões via socket.
	 * */
	public void clientSocket(String tipo) {
		DecimalFormat df = new DecimalFormat("#.##");
		try {
			clientSocket = new DatagramSocket();
			InetAddress destino = InetAddress.getByName(ipweb);
			String message = null;
			switch (tipo){
				case "call": 
					Iterator<RAS> it = RASs.iterator();
					callNotAnswered=0;
					callOnNegotiation=0;
					callEstablished=0;
					callInClosing=0;
					while (it.hasNext()) {
						RAS ras = (RAS) it.next();
						switch (ras.getServiceStatus()) {
							case NOTANSWERED:
								callNotAnswered++;
								break;
							case ONNEGOTIATION:
								callOnNegotiation++;
								break;
							case ESTABLISHED:
								callEstablished++;
								break;
							case INCLOSING:
								callInClosing++;
								break;
						}
					}
					message = "call:tot="+RASs.size()+"-notAns="+callNotAnswered+"-onNeg="+callOnNegotiation+"-est="+callEstablished+"-inClos="+callInClosing+
							   "-totH="+call_total+"-closed="+call_closed+"-lost="+call_lost+"-canc="+call_canceled;
					break;
				case "agent": 
					message = "agent:total="+ASsRegistered.size()+"-idles="+ASsIdles.size()+"-busy="+(ASsRegistered.size()-ASsIdles.size());
					break;
				case "time":
					message = "time:wai="+df.format(tWaitingRealTime)+"-qu="+df.format(tQueueRealTime)+"-arrRat="+df.format(arrivalRateRealTime)+"-avWait="+df.format(tWaitingAverage)+"-avQu="+df.format(tQueueAverage)+"-avArrRat="+df.format(averageArrivalRate);
					arrivalRateRealTime = 0;
					break;
			}
			byte[] dados = message.getBytes();
			int porta = 9999;
			DatagramPacket pacote = new DatagramPacket(dados, dados.length, destino, porta);
			clientSocket.send(pacote);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	/* Método interno para verificar a quantidade de agentes disponíveis em relação a quantidade de chamadas. 
	 * A opção "timeoutWaitingService" deve estar habilitada no arquivo de configuração.
	 * */
	private class CheckAgents extends  TimerTask { //aqui
		public void run() {
			tQueueAverageEstimated = (float)(1-	ALFA)*tQueueAverageEstimated+(ALFA)*tQueueRealTime;		
			taArrivalRateAverageEstimated = (float)(1-	ALFA)*taArrivalRateAverageEstimated+(ALFA)*arrivalRateRealTime;
			float rateService = (float)1/(float)18;
			float utilizationRate = (float)taArrivalRateAverageEstimated/((float)(ASsRegistered.size()*(float)rateService));
			int qttServerNeeded = (int) Math.round( (float) averageArrivalRate / (float) rateService);
			int qttServerActual = ASsRegistered.size();
			//Iniciar agentes
			if ( flagCheckAgents && tQueueAverageEstimated >= (ASsRegistered.size()/averageArrivalRate) && call_total > ASsRegistered.size()) {
				flagCheckAgents = false;
				if (qttServerNeeded > qttServerActual)
					startAgents(qttServerNeeded - qttServerActual);
			} //Desligar agentes
			else if ( flagCheckAgents && utilizationRate < 0.8 && call_total > ASsRegistered.size() ) {
				flagCheckAgents = false;
				if (qttServerNeeded < qttServerActual)
					stopAgents(qttServerActual - qttServerNeeded);
			}
			timerCheckAgents = new Timer();
			timerCheckAgents.schedule(new CheckAgents(),1*1000);
		}
	}
	
	/* Método interno para iniciar mais agentes. E depois para inserir esses agentes no sistema.
	 * @param quantity - quantidade de agentes que serão iniciados. Setar valor para ZERO, para que seja definida a quantidade automaticamente.  
	 * */
	private void startAgents(int quantity) {
		if (quantity == 0 ) {
			asService = new br.ufes.inf.ngn.televoto.server.as.service.Activator();
		} else {
			try {
				System.out.println("Starting " + quantity + " agents");
				logger.info("Starting " + quantity + " agents");
				asService.start( quantity );
			} catch (Exception e) { e.printStackTrace(); }
		}
		getASs();
	}
	

	
	class CheckTime implements Runnable {
		int t = 1;
		int call_canceled_RT = 0;
		ArrayList<RAS> aux;
		public void run() {
			timerCheckTime = new Timer();
			timerCheckTime.schedule(new Checks(),t*1000); //1 segundo
		}
		class Checks extends TimerTask {
			RAS as;
			Iterator<RAS> it;
			Iterator<Double> itt;
			Iterator<Integer> ittt;
			long timeWaitingL, timeQueueL;
			double timeWaitingF, timeQueueF;
			int rasWaiting, rasQueue;	
			DecimalFormat df = new DecimalFormat("#.########");
			
			public Checks(){ }	
			
			@SuppressWarnings("unchecked")
			public void run() {
				if ( !RASs.isEmpty()) {
					aux = (ArrayList<RAS>) RASs.clone();
					it = aux.iterator();
					timeWaitingL = 0;
					timeQueueL = 0;
					rasWaiting = 0;
					rasQueue = 0;
					while (it.hasNext()) {
						as = (RAS) it.next();
						if ( as.getServiceStatus() == NOTANSWERED ) {
								//System.out.print((float)as.getTimeinQueue1()/1000 + "   ");
								timeWaitingL += as.getTimeinQueue1();
								rasWaiting++;
						}
						if ( as.getServiceStatus() == NOTANSWERED || as.getServiceStatus() == ONNEGOTIATION ) {
							timeQueueL += as.getTimeinQueue2();
							rasQueue++;
						}
						tWaitingRealTime = (((double)timeWaitingL/(double)rasWaiting)/1000);
						if (Double.isNaN(tWaitingRealTime))
							tWaitingRealTime = 0;
						tQueueRealTime = (((double)timeQueueL/(double)rasQueue)/1000);
						if (Double.isNaN(tQueueRealTime))
							tQueueRealTime = 0;
					}
					//System.out.println();
					taWaiting.add(tWaitingRealTime);
					taQueue.add(tQueueRealTime);
					taArrive.add(arrivalRateRealTime);					
					
					taWaitingAverage.add((df.format(tWaitingRealTime)).replace(',', '.'));
					taQueueAverage.add((df.format(tQueueRealTime)).replace(',', '.'));
					taArrivalRateAverage.add((df.format(arrivalRateRealTime)).replace(',', '.'));
					taEstablished.add( (df.format(callEstablished)).replace(',', '.') );
					taWaitingQueue.add( (df.format(callNotAnswered+callOnNegotiation)).replace(',', '.') );
					taCanceled.add( (df.format(call_canceled - call_canceled_RT)).replace(',', '.') );
					call_canceled_RT = call_canceled;
				}
				
				if (!taWaiting.isEmpty()) {
					itt = taWaiting.iterator();
					timeWaitingF = 0;
					while ( itt.hasNext() ) {
						timeWaitingF = timeWaitingF + itt.next();
					}
					tWaitingAverage = (double)timeWaitingF / (double)taWaiting.size();
					//taWaitingAverage.add((df.format(tWaitingAverage)).replace(',', '.'));
				}
				if (!taQueue.isEmpty()) {
					itt = taQueue.iterator();
					timeQueueF = 0;
					while (itt.hasNext()) {
						timeQueueF = timeQueueF + itt.next();
					}
					tQueueAverage = (double)timeQueueF / (double)taQueue.size();
					//taQueueAverage.add((df.format(tQueueAverage)).replace(',', '.'));
				}
				
				if (!taArrive.isEmpty()) {
					ittt = taArrive.iterator();
					int arrival = 0;
					while (ittt.hasNext()) {
						arrival = arrival + ittt.next();
					}
					averageArrivalRate = (double)arrival / (double)taArrive.size();
					//taArrivalRateAverage.add((df.format(averageArrivalRate)).replace(',', '.'));
				}				
				clientSocket("time"); 
				timerCheckTime = new Timer();
				timerCheckTime.schedule(new Checks(),t*1000);
			}
		}
	}
	
	/* Método interno para reiniciar agentes. E depois para ataualizar esses agentes no sistema.
	 * @param qtt - quantidade de agentes que serão parados.  
	 * */
	private void restartAgents(String id) {
		System.out.println("RAS: Restart agent " + id);
		logger.info("RAS: Restart agent " + id);
		try {
			asService.restart(id);
		} catch (Exception e) { e.printStackTrace(); }
		getAS(id); 
	}
	
	/* Método interno para encerrar agentes. E depois para inserir esses agentes no sistema.
	 * @param qtt - quantidade de agentes que serão parados.  
	 * */
	private void stopAgents(int qtt) {
		Object list[] = new Object[qtt+1];
		System.out.println("Stopping " + qtt + " agents");
		logger.info("Stopping " + qtt + " agents");
		try {
			if (qtt > ASsRegistered.size()  ) {
				asService.stop( ASsRegistered.size() );
			} else {
				int t=0;
				flagStopAgents = false;
				synchronized (ASsIdles) {
					Iterator<AS> it = ASsIdles.iterator();
					while (it.hasNext() && t<qtt) {
						AS as = it.next();
						list[t] = as.getIdentity();
						ASsIdles.remove(as);
						ASsRegistered.remove(as);
						t++;
					}
				}
				flagStopAgents = true;
				flagCheckAgents = true;
				if (list.length != 0)
					asService.stop(list);
				ASsIdles.notifyAll();
			}
		} catch (Exception e) { //e.printStackTrace();
			flagStopAgents = true;
			flagCheckAgents = true;
			if (list.length != 0)
				asService.stop(list);
			//ASsIdles.notifyAll();
		}
		//getASs();
	}
	
	/* Método interno para consultar os agentes disponíveis, e inserí-los no sistema.
	 * */
	private void getAS(String id) {
		Iterator<String> agents = (Iterator<String>) asService.getAgents().iterator();
		boolean flagFind = false;
		while ( agents.hasNext() ) {
			if (id.equals(agents.next())) {
				flagFind = true;
				break;
			}
		}
		if (!flagFind)
			System.out.println("Agent " + id + " not find!!!");
	}
	
	/* Método interno para consultar os agentes disponíveis, e inserí-los no sistema.
	 * */
	private void getASs() {
		Iterator<String> agents = (Iterator<String>) asService.getAgents().iterator();
		if (ASsRegistered.isEmpty()) {
			while ( agents.hasNext() ) {
				String nome = agents.next();
				AS as = new AS(nome.split("@")[0], nome, 0);
				ASsRegistered.add(as);
				ASsIdles.add(as);
			}
		} else {
			//Adicionando
			while ( agents.hasNext() ) {
				String nome = agents.next();
				Iterator<AS> it = ASsRegistered.iterator();
				int flag = 0; //Não encontrado
				while (it.hasNext()) {
					if ( ((AS)it.next()).getID().equals( nome.split("@")[0] ) ) {
						flag = 1;
						break;
					}
				}
				if (flag == 0) {						
					AS as = new AS(nome.split("@")[0], nome, 0);
					ASsRegistered.add(as);
					ASsIdles.add(as);
				}
			}
			//Removendo
			Iterator<AS> it = ASsRegistered.iterator();
			ArrayList<AS> asTemp = new ArrayList<AS>();
			while ( it.hasNext() ) {
				agents = (Iterator<String>) asService.getAgents().iterator();
				String nome;
				int flag = 0; //Não encontrado
				AS as = (AS)it.next();
				while (agents.hasNext()) {
					nome = agents.next();
					if ( as.getID().equals(nome.split("@")[0]) ) {
						flag = 1;
						break;
					}
				}
				if (flag == 0) {
					asTemp.add(as);
					ASsIdles.remove(as);
				}
			}
			if (asTemp.size() != 0) {
				Iterator<AS> itt = asTemp.iterator();
				while (itt.hasNext()) {
					ASsRegistered.remove( itt.next() );
				}
			} // Final Removendo
		}		
		flagCheckAgents = true;
		flagStartStopAgents = true;
		clientSocket("agent");
		System.out.println("Server agents available: " + ASsRegistered.size());
		logger.info("Server agents available: " + ASsRegistered.size());
	}
	
	/* Classe para verificar se há um AS disponível.
	 * Se tiver um agente disponível, será enviada a requisição para ele.
	 * */
	public class FirstAS implements Runnable {
		RAS ras;
		public FirstAS(RAS ras) {
			this.ras = ras;
		}
		@Override
		public void run() {
			synchronized (ASsIdles) {
				while (ASsIdles.isEmpty() || !flagStopAgents) {
					try {
						ASsIdles.wait();
					} catch (InterruptedException e) { e.printStackTrace(); }
				}
				if (!ras.canceled) {
					AS asIdle = ASsIdles.poll();
					ASsIdles.notifyAll();
					AS asBusy = ASsRegistered.get( ASsRegistered.indexOf(asIdle) );
					asBusy.setStatus(1);	
					clientSocket("agent");
					ras.asAddress =  asIdle.getIdentity();
					ras.sendInviteToAS();
				}
			}
		}
	}	
	
	/* Método interno para inserir um agente disponível (que antes estava ocupado), no final da fila de agentes ociosos.
	 * @param asAddress - nome do agente no formato nome@dominio.
	 * */
	private void setLastAS(String asAddress) {
		Iterator<AS> it = ASsRegistered.iterator();
		while (it.hasNext()) {
			AS as = it.next();
			if (as.getIdentity().equals( asAddress )) {
				as.setStatus(0); 	//Seta status para idle
				synchronized (ASsIdles) {
					ASsIdles.add(as); 	//Insere atendedor novamente do final da fila
					ASsIdles.notifyAll();
				}
				clientSocket("agent");
				break;
			}
		}		
	}
	
	/* Método interno para manter o registro do agente.
	 * */
	class KeepAlive extends TimerTask {
		public KeepAlive(){	}
		
		public void run() {
			register(1);
		}
	}
		
	/* Método interno para registrar/desregistrar um agente.
	 * @param step -  fase do processo de autenticação. Valores 1 ou 2.
	 * */
	private void register(int step) {
		try {	
			ViaHeader myViaHeader = headerFactory.createViaHeader(ip, port,"udp", null);

			Address fromAddress = addressFactory.createAddress("<sip:"+ userID +">");
			Address registrarAddress=addressFactory.createAddress("sip:"+ domain);
			Address registerToAddress = fromAddress;
			Address registerFromAddress=fromAddress;

			ToHeader myToHeader = headerFactory.createToHeader(registerToAddress, null);
			FromHeader myFromHeader = headerFactory.createFromHeader(registerFromAddress, "647554");

			ArrayList<ViaHeader> myViaHeaders = new ArrayList<ViaHeader>();
			myViaHeaders.add(myViaHeader);

			MaxForwardsHeader myMaxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
			Random random = new Random();
			CSeqHeader myCSeqHeader = headerFactory.createCSeqHeader(random.nextInt(1000) * 1L, "REGISTER");
				
			CallIdHeader myCallIDHeader = sipProvider.getNewCallId();
			SipURI myRequestURI = (SipURI) registrarAddress.getURI();
				
			//Create SIP Request
			Request myRegisterRequest = messageFactory.createRequest(myRequestURI,"REGISTER", myCallIDHeader, myCSeqHeader, myFromHeader, myToHeader,myViaHeaders, myMaxForwardsHeader);
			
			//Expires
			ExpiresHeader myExpiresHeader;
			if (Unregistring)
				myExpiresHeader = headerFactory.createExpiresHeader(0);
			else 
				myExpiresHeader = headerFactory.createExpiresHeader(3600);
			myRegisterRequest.addHeader(myExpiresHeader);
				
			//Allow
			Allow myAllow = new Allow();
			myAllow.setMethod("INVITE, ACK, CANCEL, BYE, MESSAGE, OPTIONS, NOTIFY, PRACK, UPDATE, REFER");
			myRegisterRequest.addHeader(myAllow);
				
			//Contact
			contactHeader = headerFactory.createContactHeader(addressFactory.createAddress("sip:"+ name+ '@' + ip +":"+ port + ";transport=udp"));
			myRegisterRequest.addHeader(contactHeader);
				
			//Authorization
			Authorization myAuthorization = new Authorization();
			myAuthorization.setScheme("Digest");
			myAuthorization.setUsername(userID);
			myAuthorization.setRealm(domain);
			myAuthorization.setNonce("");
			myAuthorization.setURI( myRegisterRequest.getRequestURI() ) ;
			myAuthorization.setResponse("");
			myRegisterRequest.addHeader(myAuthorization);

			//PPreferredIdentity	
			HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();
			PPreferredIdentityHeader myPPreferredIdentityHeader = headerFactoryImpl.createPPreferredIdentityHeader(addressFactory.createAddress("sip:"+ userID));
			myRegisterRequest.addHeader(myPPreferredIdentityHeader);
				
			//Privacy
			Privacy myPrivacy = new Privacy("none");
			myRegisterRequest.addHeader(myPrivacy);
				
			//Supported
			Supported mySupported = new Supported("path");
			myRegisterRequest.addHeader(mySupported);
				
			if (step == 2) {
				//Authentication
				AuthorizationHeader myWWWAuthenticateHeader = Utils.makeAuthHeader(headerFactory, responseReceived, myRegisterRequest, userID, password);
				myRegisterRequest.addHeader(myWWWAuthenticateHeader);
			}
				
			clientTransaction = sipProvider.getNewClientTransaction(myRegisterRequest);
			clientTransaction.sendRequest();

			logger.info(myRegisterRequest.toString());
			status=REGISTERING;
		}catch (Exception e) { e.printStackTrace(); }
	}
	
	
	
	/************************************
	 **			Inner Class RAS			*
	 ************************************/
	
	/* Classe interna que relaciona o agente chamador com o agente atendedor.
	 * */
	public class RAS implements SipListener {
		private SipStack mySipStack;
		private ListeningPoint myListeningPoint;
		private SipProvider mySipProvider;			//Used to send SIP messages.
		private Properties myProperties;			//Other properties.
		private SipFactory sipFactory;
		private ViaHeader myViaHeader;
		private Dialog UEDialog;
		private Dialog ASDialog;
		private ClientTransaction clientTransaction;
		private ServerTransaction serverTransaction;
		private Request originalRequestFromUE;
	    private Response originalResponseFromAS;
	    private Timer timerCheckAnswer, timerCheckTimeoutWaitingService, timerCheckTimeoutEstablished, timerCheckTimeoutOnNegotiation; 
		private int serverStatus;
		private int clientStatus;
		private int status;
		private int serviceStatus; // -1 Not answered | 0 On negotiation: | 1 Established | 2 In closing
		private String callID;
		private String asAddress;
		private String ueAddress;
		private boolean expired=false, removed=false;
		private boolean canceled = false;
		private boolean byeFromAS;
		private boolean callLocked = false;
		private Calendar timeCallArrival;
		private Calendar timeCallNoReply;
		private Calendar timeCallSet;
		private Calendar timeCallEnd;
		
		/* Método construtor da classe, que seta os parâmetros iniciais.
		 * @param myCallID - Identificação da chamada. */
		@SuppressWarnings("deprecation")
		private RAS(String myCallID, Calendar callArrival) {
			timeCallArrival = callArrival;
			callID = myCallID;
			try {	
				myProperties = new Properties();
				myProperties.setProperty("javax.sip.STACK_NAME", name);
				myProperties.setProperty("javax.sip.OUTBOUND_PROXY", proxy); 
				sipFactory = SipFactory.getInstance();
				sipFactory.setPathName("gov.nist");
				mySipStack = sipFactory.createSipStack(myProperties);
				messageFactory = sipFactory.createMessageFactory();
				headerFactory = sipFactory.createHeaderFactory();
				addressFactory = sipFactory.createAddressFactory();
				ListIterator<?> provider = (ListIterator<?>) mySipStack.getSipProviders();
				int i;
				for ( i=0 ; provider.hasNext() ; ++i ) provider.next();
				if (i > 0) {
					Object sipProvider = (SipProvider) provider.previous();
					mySipProvider = ((SipProvider) sipProvider);
					myListeningPoint = mySipProvider.getListeningPoint();
				} else {
					myListeningPoint = mySipStack.createListeningPoint(ip, port, "udp");
					mySipProvider = mySipStack.createSipProvider(myListeningPoint);
					mySipProvider.addSipListener(this);
				}			
			} catch (Exception e) { e.printStackTrace(); }	
			serverStatus = IDLE;
			clientStatus = IDLE;
			status = IDLE;
		}
		
		@SuppressWarnings("deprecation")
		public void stop() {
			timerCheckTimeoutOnNegotiation.cancel();
			tFirstAS.stop();
			/*timerCheckTimeoutOnNegotiation.purge();
		    if (timerCheckAnswer.can
		    , timerCheckTimeoutWaitingService, timerCheckTimeoutEstablished, timerCheckTimeoutOnNegotiation;*/

		}
		
		/* Método interno que retorna o status do serviço/atendimento.
		 * @return int - NOTANSWERED / ONNEGOTIATION / ESTABLISHED / INCLOSING
		 * */
		private int getServiceStatus() {
			return serviceStatus;
		}
		
		/* Método interno que retorna a identificação da chamada.
		 * @return String - identificação da ligação.
		 * */
		public String getCallID() {
			return callID;
		}

		/* Método interno que retorna o nome do AS.
		 * @return String - nome do AS, sem o domínio.
		 * */
		public String getAS() {
			return asAddress.split("@")[0];
		}
		
		/* Método interno que retorna o nome do cliente.
		 * @return String - nome do cliente, sem o domínio.
		 * */
		public String getUE() {
			return ueAddress.split("@")[0];
		}		
		
		/* Método interno que retorna o tempo de espera do cliente, até que inicie a negociação com um AS disponível.
		 * @return long - tempo de espera para início da negociação.
		 * */
		public long getTimeinQueue1() {
			if (timeCallNoReply != null)
				return (timeCallNoReply.getTimeInMillis() - timeCallArrival.getTimeInMillis());
			else {
				return (Calendar.getInstance().getTimeInMillis() - timeCallArrival.getTimeInMillis());
				}
		}
		
		/* Método interno que retorna o tempo de espera do cliente, até que a ligação tenha sido estabelecida com o AS e o cliente.
		 * @return long - tempo de espera até que a ligação seja estabelecida.
		 * */		
		public long getTimeinQueue2() {
			if (timeCallSet != null)
				return (timeCallSet.getTimeInMillis() - timeCallArrival.getTimeInMillis());
			else
				return (Calendar.getInstance().getTimeInMillis() - timeCallArrival.getTimeInMillis());
		}
		
		/* Método interno que retorna o tempo total da ligação, até que receba o BYE.
		 * @return long - tempo total da ligação.
		 * */	
		public long getTimeTotal() {
			return (timeCallEnd.getTimeInMillis() - timeCallArrival.getTimeInMillis());
		}
		
		/* Classe para verificar se a resposta a uma solicitação, foi respodinda em um tempo definido.
		 * Se o tipo de requisição for "INVITE" e não for respondido dentro do tempo difinido, então será selecionado outro AS.
		 * Se o tipo de requisição for "BYE" e não for respondido dentro do tempo, o AS será inserido na fila de agentes ociosos e o RAS será removido.
		 * @param type = tipo de requisição, "INVITE" ou "BYE". 
		 * @param ras = objeto RAS que será removido (this).
		 * */
		class CheckAnswer extends TimerTask {
			String type;
			RAS ras;
			public CheckAnswer(String type, RAS ras) { 
				this.type = type;
				this.ras = ras;
			}	
			public void run() {
				if (type.equals("INVITE")) {
					if ( ((clientStatus == WAIT_PROV) || (clientStatus == WAIT_FINAL)) && serverStatus == RINGING && !expired && !removed) {
						//Checks for response "200 OK" from AS
						System.out.println(getUE() + " selecting new AS (Previos AS: " + getAS() + ")");
						logger.info(getUE() + " selecting new AS (Previos AS: " + getAS() + ")");
						setLastAS(asAddress);
						tFirstAS = new Thread( new FirstAS(ras) );
						tFirstAS.start();
						send180ToUE();
					}
				} else
					if (type.equals("BYE") && !removed) {
						//Check response for BYE
						removed = true;
						if (!byeFromAS)
							setLastAS(asAddress);
						RASs.remove(ras);
						if(callLocked)
							call_canceled++;
						else
							call_closed++;	
						clientSocket("call");
					}
			}
		}
		
		/* Classe para verificar se o tempo de estabelecimento da chamada está maior que o normal.
		 * Se o tempo tiver excedido, a ligação será encerrada e o RAS excluído.
		 * Para funcionar é necessário setar a opção no arquivo de configuração.
		 * */
		class CheckTimeoutEstablished extends TimerTask {
			RAS ras;
			public CheckTimeoutEstablished(RAS ras) { 
				this.ras = ras;
			}	
			public void run() { 
				if ( status == ESTABLISHED && !removed) {
					System.out.println(getUE() + ": Tempo Expirado (ESTABLISHED)!");
					logger.info(getUE() + ": Tempo Expirado (ESTABLISHED)!");
					serviceStatus=INCLOSING;
					callLocked = true;
					restartAgents(getAS());
					byeFromAS = false;
					try {
						Request myRequest = UEDialog.createRequest("BYE");
						myRequest.addHeader(contactHeader);
						clientTransaction = mySipProvider.getNewClientTransaction(myRequest);
						UEDialog.sendRequest(clientTransaction);
						logger.info(">>> "+clientTransaction.getRequest().toString());
					} catch (Exception e) { e.printStackTrace();  }
					timerCheckAnswer = new Timer();
					timerCheckAnswer.schedule(new CheckAnswer("BYE", ras),timeForwardingNextAS*1000);
				}
			}
		}
		
		/* Classe para verificar se o tempo de negociação da chamada está maior que o normal.
		 * Se o tempo tiver excedido, a ligação será encerrada e o RAS excluído.
		 * Para funcionar é necessário setar a opção no arquivo de configuração.
		 * */
		class CheckTimeoutOnNegotiation extends TimerTask {
			RAS ras;
			public CheckTimeoutOnNegotiation(RAS ras) { 
				this.ras = ras;
			}	
			public void run() {
				if ( serviceStatus == ONNEGOTIATION) {
					System.out.println(getUE() + ": Tempo Expirado (ONNEGOTIATION)! " + getAS());
					logger.info(getUE() + ": Tempo Expirado (ONNEGOTIATION)! " + getAS());
					removed = true;
					setLastAS(asAddress);
					status = INCLOSING;
					RASs.remove(ras);
					clientSocket("call");
				}
			}
		}
		
		/* Classe para verificar se o tempo de espera na fila de atendimento foi excedido.
		 * Se o tempo tiver excedido, a ligação será encerrada e o RAS excluído.
		 * Para funcionar é necessário setar a opção no arquivo de configuração.
		 * */
		class CheckTimeoutWaitingService extends TimerTask {
			RAS ras;
			public CheckTimeoutWaitingService(RAS ras) { 
				this.ras = ras;
			}	
			public void run() {
				if ( ( status != ESTABLISHED )) {
					System.out.println(getUE() + ": Tempo Expirado!");
					logger.info(getUE() + ": Tempo Expirado!");
					expired = true;
					call_lost++;
					try {
						if (clientStatus==WAIT_PROV || clientStatus==WAIT_ACK) 	{ //Sends "CANCEL" to AS
							Request myRequest = clientTransaction.createCancel();
							clientTransaction = sipProvider.getNewClientTransaction(myRequest);
							clientTransaction.sendRequest();
							logger.info(">>> "+clientTransaction.getRequest().toString());							
						}
						if (serverStatus==RINGING || serverStatus==WAIT_ACK) { //Sends 486 "Busy Here" to UE
							Response myResponse = messageFactory.createResponse(486, originalRequestFromUE);
							myResponse.addHeader(contactHeader);
							serverTransaction.sendResponse(myResponse);
							logger.info(">>> " + myResponse.toString());
						}
					} catch (Exception e) { e.printStackTrace();  }
					setLastAS(asAddress);
					RASs.remove(ras);
					clientSocket("call");
				}
			}
		}

		/* Método interno para enviar um requisição INVITE para um AS.
		 * */
		private void sendInviteToAS() {
			if (!canceled) {
				ueAddress = (((((FromHeader) originalRequestFromUE.getHeader("From")).getAddress()).toString().split(":"))[1]).replace(">", "");
				System.out.println("RAS:  " + getUE() + " to " + getAS());
				logger.info("RAS:  " + getUE() + " to " + getAS());
				timeCallNoReply = Calendar.getInstance();
				try {				
					SipURI myRequestURI = (SipURI) (addressFactory.createAddress("sip:"+ asAddress)).getURI();
					CallIdHeader myCallIdHeader = (CallIdHeader) originalRequestFromUE.getHeader("Call-ID");
					CSeqHeader myCSeqHeader = (CSeqHeader) originalRequestFromUE.getHeader("CSeq");
					FromHeader myFromHeader = headerFactory.createFromHeader(addressFactory.createAddress("<sip:"+userID+">"), "647554");
					ToHeader myToHeader = headerFactory.createToHeader(addressFactory.createAddress("<sip:"+asAddress+">"),null);
					myViaHeader = headerFactory.createViaHeader(ip, port,"udp", null);
					myViaHeader.setRPort();
					ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
					viaHeaders.add(myViaHeader);
					MaxForwardsHeader myMaxForwardsHeader = (MaxForwardsHeader) originalRequestFromUE.getHeader("Max-Forwards");
					Request myRequest = messageFactory.createRequest(myRequestURI,"INVITE", myCallIdHeader, myCSeqHeader,myFromHeader,myToHeader, viaHeaders, myMaxForwardsHeader);
					myRequest.addHeader(contactHeader);
					RouteHeader myRouteHeader= headerFactory.createRouteHeader(addressFactory.createAddress("sip:"+domain+";lr"));
					myRequest.addHeader(myRouteHeader);
					myRequest.setContent(originalRequestFromUE.getContent(), headerFactory.createContentTypeHeader("application","sdp"));
					clientTransaction = mySipProvider.getNewClientTransaction(myRequest);
					clientTransaction.sendRequest(); 
					logger.info(">>> " + myRequest.toString());
					ASDialog = clientTransaction.getDialog();
					serviceStatus = ONNEGOTIATION;
					if(timeoutForwardingNextAS) {
						timerCheckAnswer = new Timer();
						timerCheckAnswer.schedule(new CheckAnswer("INVITE", this),timeForwardingNextAS*1000);
					}
					if(checkLocked) {
						timerCheckTimeoutOnNegotiation = new Timer();
						timerCheckTimeoutOnNegotiation.schedule(new CheckTimeoutOnNegotiation(this),timeoutLocked*1000);
					}
					clientSocket("call");
					clientStatus = WAIT_PROV;
					status = WAIT_PROV;
				} catch (Exception e) { e.printStackTrace(); }
			} else {
				System.out.println(getUE() + "SendInvite. Canceled=true");
				setLastAS(asAddress);
				RASs.remove(this);
				clientSocket("call");
			}
		}
		
		/* Método interno para enviar uma resposta do tipo "180" para o cliente.
		 * */
		private void send180ToUE() {
			try {
				Response myResponse = messageFactory.createResponse(180,originalRequestFromUE);
				myResponse.addHeader(contactHeader);
				if (serverTransaction == null)
					serverTransaction = mySipProvider.getNewServerTransaction(originalRequestFromUE);	
				serverTransaction.sendResponse(myResponse);
				logger.info(">>> " + myResponse.toString());	
				UEDialog = serverTransaction.getDialog();		
				serverStatus=RINGING;
			} catch (Exception e) { e.printStackTrace(); }
		}
		
		/* Método interno para processar requisições SIP.
		 * @param requestReceivedEvent - evento de requisição SIP.
		 * */
		public synchronized void processRequest(RequestEvent requestReceivedEvent) {
			Request myRequestReceived = requestReceivedEvent.getRequest();
			logger.info(">>> " + myRequestReceived.toString());
			String method = myRequestReceived.getMethod();
			Request myRequest;
			Response myResponse;
			try{			
				switch (status) {
					case IDLE:
						if (method.equals("INVITE")) {
							serviceStatus = NOTANSWERED;
							call_total++;
							arrivalRateRealTime++;
							clientSocket("call");
							if (timeoutWaitingService) {
								timerCheckTimeoutWaitingService= new Timer();
								timerCheckTimeoutWaitingService.schedule(new CheckTimeoutWaitingService(this),timeWaitingService*1000);
							}
							originalRequestFromUE = myRequestReceived;
							send180ToUE();	
							tFirstAS = new Thread( new FirstAS(this) );
							tFirstAS.start();
						}
						if (method.equals("CANCEL")) {
							//long diff = Calendar.getInstance().getTimeInMillis() - timeCallArrival.getTimeInMillis();
							//System.out.println("Call canceled ("+ diff/1000 +" === "+ getTimeinQueue1() +")");
							canceled = true;
							call_canceled++;
							//Sends "200 OK" to AS
							myResponse = messageFactory.createResponse(200, myRequestReceived);
							myResponse.addHeader(contactHeader);
							serverTransaction = requestReceivedEvent.getServerTransaction();	
							serverTransaction.sendResponse(myResponse);
							logger.info(">>> " + myResponse.toString());
							status = IDLE;
							serverStatus = IDLE;
							//Sends "CANCEL" to AS
							if (clientStatus!=IDLE) 	{
								myRequest = clientTransaction.createCancel();
								clientTransaction = sipProvider.getNewClientTransaction(myRequest);
								clientTransaction.sendRequest();
								logger.info(">>> "+clientTransaction.getRequest().toString());	
								clientStatus = IDLE;
								setLastAS(asAddress);
							}
							if (timeoutWaitingService) 
								timerCheckTimeoutWaitingService.cancel();
							removed = true;
							RASs.remove(this);
							clientSocket("call");
						}
						break; //status=IDLE
						
					case ESTABLISHED:	
						if (method.equals("BYE")) {	
							timeCallEnd = Calendar.getInstance();
							serviceStatus = INCLOSING;
							clientSocket("call");
							//Send "200 OK" to AS (or UE)						
							myResponse = messageFactory.createResponse(200, myRequestReceived);
							myResponse.addHeader(contactHeader);
							serverTransaction = requestReceivedEvent.getServerTransaction();	
							serverTransaction.sendResponse(myResponse);
							logger.info(">>> " + myResponse.toString());
							serverStatus = IDLE;
							
							Address myFrom = ((FromHeader) myRequestReceived.getHeader("From")).getAddress();
							Address UEAddress = UEDialog.getRemoteParty();	
							if (myFrom.equals(UEAddress)) {
								//Sends "BYE" to AS
								myRequest = ASDialog.createRequest("BYE");
								myRequest.addHeader(contactHeader);
								clientTransaction = mySipProvider.getNewClientTransaction(myRequest);
								ASDialog.sendRequest(clientTransaction);
								logger.info(">>> "+clientTransaction.getRequest().toString());
								byeFromAS = false;
							}
							else{
								//Sends "BYE" to UE
								myRequest = UEDialog.createRequest("BYE");
								myRequest.addHeader(contactHeader);
								clientTransaction = mySipProvider.getNewClientTransaction(myRequest);
								UEDialog.sendRequest(clientTransaction);
								logger.info(">>> "+clientTransaction.getRequest().toString());
								byeFromAS = true;
								setLastAS(asAddress);
							}
							timerCheckAnswer = new Timer();
							timerCheckAnswer.schedule(new CheckAnswer("BYE", this),timeForwardingNextAS*1000);
							/*if(checkLocked) {
								timerCheckTimeoutEstablished.cancel();
							}*/ //comentei
						}
						break;
					case WAIT_ACK:
						if (method.equals("ACK")) {
							timeCallSet = Calendar.getInstance();
							if (timeoutWaitingService)
								timerCheckTimeoutWaitingService.cancel();
							serverStatus = ESTABLISHED;
							//Sends ACK to AS
							myRequest = ASDialog.createAck(((CSeqHeader) originalResponseFromAS.getHeader("CSeq")).getSeqNumber());
							myRequest.addHeader(contactHeader);
							ASDialog.sendAck(myRequest);
							logger.info(">>> "+ myRequest.toString());
							serviceStatus = ESTABLISHED;
							clientStatus = ESTABLISHED;
							status = ESTABLISHED;
							clientSocket("call");
							if(checkLocked) {
								timerCheckTimeoutOnNegotiation.cancel();
								/*timerCheckTimeoutEstablished = new Timer();
								timerCheckTimeoutEstablished.schedule(new CheckTimeoutEstablished(this),timeoutLocked*1000);*/ //comentei
							}
						}
						break; //WAIT_ACK
				}
			}catch (Exception e) { e.printStackTrace(); }
		}

		/* Método interno para processar respostas SIP.
		 * @param responseReceivedEvent - evento de resposta SIP.
		 * */
		public synchronized void processResponse(ResponseEvent responseReceivedEvent) {
			try{
				Response myResponseReceived = responseReceivedEvent.getResponse();
				logger.info("<<< "+ myResponseReceived.toString());
				int myStatusCode = myResponseReceived.getStatusCode();
										
				switch(status) {		
					case WAIT_PROV:
						if (myStatusCode==180) {
							//Receives "180" from AS
							clientTransaction = responseReceivedEvent.getClientTransaction();
							ASDialog = clientTransaction.getDialog();
							clientStatus = WAIT_FINAL;
							status = WAIT_FINAL;
						}
						if (myStatusCode==200){
							if(timeoutForwardingNextAS)
								timerCheckAnswer.cancel();
							//Receives "200 OK" from AS
							clientTransaction = responseReceivedEvent.getClientTransaction();
							if (clientTransaction != null)
								ASDialog = clientTransaction.getDialog();
							else
								ASDialog = responseReceivedEvent.getDialog();
							//Sends "200 OK" to UE
							Response myResponse = messageFactory.createResponse(200, originalRequestFromUE);
							myResponse.addHeader(contactHeader);
							myResponse.setContent(myResponseReceived.getContent(),headerFactory.createContentTypeHeader("application","sdp"));
							serverTransaction.sendResponse(myResponse);
							logger.info(">>> " + myResponse.toString());
							UEDialog = serverTransaction.getDialog(); 
							serverStatus = WAIT_ACK;
							status = WAIT_ACK;							
							originalResponseFromAS = myResponseReceived;
						}
						break; //status=WAIT_PROV
					case WAIT_FINAL:
						if (myStatusCode==200){
							if(timeoutForwardingNextAS)
								timerCheckAnswer.cancel();
							//Receives "200 OK" from AS
							clientTransaction = responseReceivedEvent.getClientTransaction();
							ASDialog = clientTransaction.getDialog();
							//Sends "200 OK" to UE
							Response myResponse = messageFactory.createResponse(200, originalRequestFromUE);
							myResponse.addHeader(contactHeader);
							myResponse.setContent(myResponseReceived.getContent(),headerFactory.createContentTypeHeader("application","sdp"));
							serverTransaction.sendResponse(myResponse);
							logger.info(">>> " + myResponse.toString());
							UEDialog = serverTransaction.getDialog(); 
							serverStatus = WAIT_ACK;
							status = WAIT_ACK;
							originalResponseFromAS = myResponseReceived;
						}
						break;	
					case ESTABLISHED:
						if (myStatusCode==200 && serverStatus==IDLE && clientStatus==ESTABLISHED) { //Response "200 OK" for request BYE
							removed = true;
							timerCheckAnswer.cancel();
							if (!byeFromAS)
								setLastAS(asAddress);
							RASs.remove(this);
							if(callLocked)
								call_canceled++;
							else
								call_closed++;	
							clientSocket("call");
						}
						break;
				}
			} catch(Exception excep){ excep.printStackTrace(); }
		}

		public void processTimeout(TimeoutEvent timeoutEvent) { }

		public void processTransactionTerminated(TransactionTerminatedEvent tevent) { }

		public void processDialogTerminated(DialogTerminatedEvent tevent) { }

		public void processIOException(IOExceptionEvent tevent) { }

	}
	
}