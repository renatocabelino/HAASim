package br.ufes.inf.ngn.televoto.server.as.logic;

import gov.nist.javax.sip.header.Allow;
import gov.nist.javax.sip.header.Authorization;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.Supported;
import gov.nist.javax.sip.header.ims.PPreferredIdentityHeader;
import gov.nist.javax.sip.header.ims.Privacy;
import jade.core.Agent;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.SourceCloneable;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.media.EndOfMediaEvent;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
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
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import splibraries.SdpInfo;
import splibraries.SdpManager;
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
	private ContactHeader contactHeader;
	private Dialog dialog;
	private SipFactory sipFactory;
	private ClientTransaction clientTransaction;
	private ServerTransaction serverTransaction;
	private Response responseReceived;
	private SdpManager sdpManager;
	private SdpInfo answerInfo;
	private SdpInfo offerInfo;
	private AVTransmit2 at;
    private Boolean Unregistring = false;
    private Timer timer, timerAlive;
    public DataSource myDataSource;
	public AudioFormat afmt;
	private MediaLocator ml;
    private int totalVotos = 0;  
	private int status;
	private int port;
	private int	audioFormat;
	private int audioPort;
	private String ip;
	private String name;
	private String userID;
	private String password;	
	private String server;
	private String proxy;
	private String extension;
	private String audioFile;
	private boolean flagCanceled = false;
	
	Calendar tArrive;
	
   	static final int YES=0;
	static final int NO=1;
	static final int SEND_MESSAGE=2;
	static final int UNREGISTERED=-2;
	static final int REGISTERING=-1;
	static final int IDLE=0;
	static final int WAIT_PROV=1;
	static final int WAIT_FINAL=2;
	static final int ESTABLISHED=4;
	static final int RINGING=5;
	static final int WAIT_ACK=6;

	private Log logger = (Log) LogFactory.getLog(Listener.class);
	
	class Alive extends TimerTask { //aqui
		public Alive() { }
		public void run() {
			Calendar t = Calendar.getInstance();
			long diff = t.getTimeInMillis() - tArrive.getTimeInMillis();
			if (status == ESTABLISHED) {
				if ( (diff/1000) > 40) {
					System.out.println(name + " Travado ("+ diff/100 +"s)");
					logger.info(name + " Travado");
					//flagCanceled = true;
					//stopDialog();
				}
			}
		}
	}
	
	//#######################################################################################################
	//### Defaults Methods of Agent
	
	//First method invoked by the agent
	@SuppressWarnings({ "deprecation", "rawtypes" })
	public void setup () {
		try {
			
			if (!Unregistring) { //Se é um registro
			    Object parametros[] = new Object[11];
				parametros = getArguments();
				myDataSource = null;
				extension = (String) parametros[0];						
				server    = (String) parametros[1]; 		
				password  = (String) parametros[2]; 				
				port      = (Integer) parametros[3]; 						
				proxy     = parametros[4] + ":" + parametros[5] + "/UDP";
				name = (String) this.getLocalName();
				userID = (String) extension + "@" + server;
				audioPort = (Integer) parametros[6];
				audioFormat = (int) parametros[7];	
				audioFile = (String) parametros[8];
				ip = InetAddress.getLocalHost().getHostAddress();		
			}
			
			//Audio
			if ((ml = new MediaLocator("file:" + audioFile)) == null) {
				System.err.println("Cannot build media locator from: " + audioFile);
				System.exit(0);
			}
			try {
				myDataSource = (DataSource) Manager.createDataSource(ml);
			} catch (Exception e) {
				System.err.println("Cannot create DataSource from: " + ml);
				System.exit(0);
			}
			myDataSource = (DataSource) Manager.createCloneableDataSource(myDataSource);
			if (myDataSource == null) {
				System.err.println("Cannot clone the given DataSource");
				System.exit(0);
			}
			switch (audioFormat) {
				case 0:
					afmt = new AudioFormat(AudioFormat.DVI_RTP, 8000, 4, 1);
					break;
				case 1:
					afmt = new AudioFormat(AudioFormat.DVI_RTP, 11025, 4, 1);
					break;
				case 2:
					afmt = new AudioFormat(AudioFormat.DVI_RTP, 22050, 4, 1);
					break;
				case 3:
					afmt = new AudioFormat(AudioFormat.ULAW_RTP, 8000, 8, 1);
					break;
				case 4:
					afmt = new AudioFormat(AudioFormat.GSM_RTP, 8000, 8, 1);
					break;
			}
			
			sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");
			sdpManager=new SdpManager();
			answerInfo=new SdpInfo();
			offerInfo=new SdpInfo();
			properties = new Properties();
			properties.setProperty("javax.sip.STACK_NAME", name);
			properties.setProperty("javax.sip.OUTBOUND_PROXY", proxy); //Proxy
			sipStack = sipFactory.createSipStack(properties);
			messageFactory = sipFactory.createMessageFactory();
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			
			if (!Unregistring) { //REGISTER
				ListIterator provider = (ListIterator) sipStack.getSipProviders();
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
			}
		} catch (Exception e) { e.printStackTrace(); }
		register(1);
	}
	
	//Method for finishing agent  //???
	protected void takeDown() {	
		Unregistring = true;
		System.out.println(name + " total de votos computados: " + totalVotos);
		/*try {
			DatagramSocket socket = new DatagramSocket();
			InetAddress destino = InetAddress.getByName("127.0.0.1");
			String mensagem = name + ";" + totalVotos;
			byte[] dados = mensagem.getBytes();
			int porta = 9999;
			DatagramPacket pacote = new DatagramPacket(dados, dados.length, destino, porta);
			socket.send(pacote);
			} catch (SocketException e) { 
				e.printStackTrace(); 
			} catch (IOException e) { 
				e.printStackTrace(); 
		}*/
		register(1);
	}

	//#######################################################################################################
	//### Defaults Methods of SIP
	
	//Method for processing requests
	public synchronized void processRequest(RequestEvent requestReceivedEvent) {
		Request myRequestReceived = requestReceivedEvent.getRequest();
		String method = myRequestReceived.getMethod();
		logger.info("<<< " + myRequestReceived.toString());
		try{			
			switch (status) {
				case IDLE:
					if (method.equals("INVITE")) {
						serverTransaction = sipProvider.getNewServerTransaction(myRequestReceived);
						//Sends "180 Ringing"
						Response myResponse=messageFactory.createResponse(180,myRequestReceived);
						myResponse.addHeader(contactHeader);	
						serverTransaction.sendResponse(myResponse);
						dialog=serverTransaction.getDialog();
						logger.info(">>> "+myResponse.toString());
						status=RINGING;
						//Sends "200 OK"
						myResponse = messageFactory.createResponse(200, myRequestReceived);
						myResponse.addHeader(contactHeader);
						offerInfo = sdpManager.getSdp((byte[]) myRequestReceived.getContent());
						answerInfo.IpAddress = ip;
						answerInfo.aport = offerInfo.aport;
						answerInfo.aformat = offerInfo.aformat;
						myResponse.setContent(sdpManager.createSdp(answerInfo) ,headerFactory.createContentTypeHeader("application","sdp"));
						serverTransaction.sendResponse(myResponse);
						logger.info(">>> " + myResponse.toString());
						dialog = serverTransaction.getDialog();
						status = WAIT_ACK;
					}
					break;
				case ESTABLISHED:
					if (method.equals("BYE")) {
						timerAlive.cancel();
						stopMedia();
						Response myResponse = messageFactory.createResponse(200,myRequestReceived);
						myResponse.addHeader(contactHeader);
						serverTransaction = requestReceivedEvent.getServerTransaction();
						serverTransaction.sendResponse(myResponse);
						logger.info(">>> " + myResponse.toString());
						status=IDLE;
					}
					break;
				case RINGING:
					if (method.equals("CANCEL")) {
						Response myResponse=messageFactory.createResponse(200,myRequestReceived);
						serverTransaction.sendResponse(myResponse);
						logger.info(">>> "+myResponse.toString());
						status=IDLE;
					}
					break;
				case WAIT_ACK:
					if (method.equals("ACK")) {
						status=ESTABLISHED;	//aqui
						tArrive = Calendar.getInstance();
						startMedia(offerInfo.IpAddress, offerInfo.aport);
						timerAlive = new Timer();
						timerAlive.schedule(new Alive(),60*1000);
					}
					break;
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	//Method for processing responses
	public synchronized void processResponse(ResponseEvent responseReceivedEvent) {
		try{
			clientTransaction = responseReceivedEvent.getClientTransaction();
			Response myResponseReceived = responseReceivedEvent.getResponse();
			logger.info("<<< "+myResponseReceived.toString());
			int myStatusCode = myResponseReceived.getStatusCode();
						
			switch(status){

				case WAIT_PROV:
					switch (myStatusCode) {
						case 100: case 180:
							status = WAIT_FINAL;
							dialog = clientTransaction.getDialog();
							break;
						case 200:
							dialog = clientTransaction.getDialog();
							Request myAck = dialog.createAck(((CSeqHeader) clientTransaction.getRequest().getHeader(CSeqHeader.NAME)).getSeqNumber());
							myAck.addHeader(contactHeader);
							dialog.sendAck(myAck);
							logger.info(">>> "+myAck.toString());
							status=ESTABLISHED;
							answerInfo=sdpManager.getSdp((byte[]) myResponseReceived.getContent());
							break;
					}
					break;
				case WAIT_FINAL:
					switch (myStatusCode) {
						case 100: case 180:
							status=WAIT_FINAL;
							dialog=clientTransaction.getDialog();
							break;
						case 200:
							break;
					}
					if (myStatusCode<200) {
						status=ESTABLISHED;
						dialog=clientTransaction.getDialog();
						Request myAck = dialog.createAck(((CSeqHeader) clientTransaction.getRequest().getHeader(CSeqHeader.NAME)).getSeqNumber());
						myAck.addHeader(contactHeader);
						dialog.sendAck(myAck);
						logger.info(">>> "+myAck.toString());
						answerInfo=sdpManager.getSdp((byte[]) myResponseReceived.getContent());
					}
					break;
				case REGISTERING:
					switch (myStatusCode){
						case 200:
							status=IDLE;
							if (!Unregistring) {
								System.out.println(name + ": online");
								timer = new Timer();
								timer.schedule(new KeepAlive(),55*60*1000);//Re-register em 55 minutos
							} else 
								this.setOff();
							break;
						case 401:
							responseReceived = myResponseReceived;
							register(2);
							break;
						case 403:
							System.out.println("Problemas com credenciais!!!!\n");
							break;
					}
					break;
			}
		} catch(Exception excep){ excep.printStackTrace(); }
	}

	public void processTimeout(TimeoutEvent timeoutEvent) { }

	public void processTransactionTerminated(TransactionTerminatedEvent tevent) { }

	public void processDialogTerminated(DialogTerminatedEvent tevent) { }

	public void processIOException(IOExceptionEvent tevent) { }
	
	
	
	//#######################################################################################################
	//### Other methods
	
	// Generates a request REGISTER
	/* step = 1 ----> first step authentication
	 * step = 2 ----> second step authentication */
	public void register(int step) {
		try {	
			ViaHeader myViaHeader = headerFactory.createViaHeader(ip, port,"udp", null);
			Address fromAddress = addressFactory.createAddress("<sip:"+ userID +">");
			Address registrarAddress=addressFactory.createAddress("sip:"+ server);
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
			Address contactAddress = addressFactory.createAddress("sip:"+ name+ '@' + ip +":"+ port + ";transport=udp");
			ContactHeader myContactHeader = headerFactory.createContactHeader(contactAddress);
			myRegisterRequest.addHeader(myContactHeader);
			contactHeader = myContactHeader;
			//Authorization
			Authorization myAuthorization = new Authorization();
			myAuthorization.setScheme("Digest");
			myAuthorization.setUsername(userID);
			myAuthorization.setRealm(server);
			myAuthorization.setNonce("");
			myAuthorization.setURI( myRegisterRequest.getRequestURI() ) ;
			myAuthorization.setResponse("");
			myRegisterRequest.addHeader(myAuthorization);
			//PPreferredIdentity	
			HeaderFactoryImpl myHeaderFactoryImpl = new HeaderFactoryImpl();
			PPreferredIdentityHeader myPPreferredIdentityHeader = myHeaderFactoryImpl.createPPreferredIdentityHeader(addressFactory.createAddress("sip:"+ name+ '@' + server));
			myRegisterRequest.addHeader(myPPreferredIdentityHeader);
			//Privacy
			Privacy myPrivacy = new Privacy("none");
			myRegisterRequest.addHeader(myPrivacy);
			//Supported
			Supported mySupported = new Supported("path");
			myRegisterRequest.addHeader(mySupported);
			//Authentication
			if (step == 2) {
				AuthorizationHeader myWWWAuthenticateHeader = Utils.makeAuthHeader(headerFactory, responseReceived, myRegisterRequest, userID, password);
				myRegisterRequest.addHeader(myWWWAuthenticateHeader);
			}
			clientTransaction = sipProvider.getNewClientTransaction(myRegisterRequest);
			clientTransaction.sendRequest();
			logger.info(">>> " +myRegisterRequest.toString());
			status=REGISTERING;
		}catch (Exception e) { e.printStackTrace(); }
	}
	
	//Start media media transmission
	public void startMedia(String IP, int aport) {
		at = new AVTransmit2(myDataSource, IP, aport, afmt, name);
		at.start();		
	}

	//Stop media transmission
	public void stopMedia() {
		at.stopTransmiter();
	}
	
	//Send "BYE", to close dialogue
	public void stopDialog() {
		Request myBye = null;
		try {
			myBye = dialog.createRequest("BYE");
			myBye.addHeader(contactHeader);
			clientTransaction = sipProvider.getNewClientTransaction(myBye);
			dialog.sendRequest(clientTransaction);
			logger.info(">>> "+clientTransaction.getRequest().toString());
		} catch (SipException e) { e.printStackTrace();}
		if (flagCanceled)
			System.out.println("AS:   " + name + " is idle (Canceled)");
		else
			System.out.println("AS:   " + name + " is idle");
		status = IDLE;	
	}
	
	//Close connections
	public void setOff(){
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
	

	
	//#######################################################################################################
	//### Other Subclass
	
	//Class KeepAlive: Keeps the record (send REGISTER again) -----------------------------
	class KeepAlive extends TimerTask {
		public KeepAlive(){ }
			
		public void run() {
			register(1);
		}
	}
	
	//Class AVT Transmit2 ------------------------------------------------------------------	
		public class AVTransmit2 {
			private DataSource ds;
			private String ipAddress;
			private int portBase;
			private AudioFormat audioFormat;
			private String name;
			private Integer stateLock = new Integer(0);
			private boolean failed = false;
			public Processor processor = null;
			private RTPManager rtpMgrs[];
			private DataSource dataOutput = null;
			public SendStream sendStream;
			private boolean mediaTransmitting = true;
		
			public AVTransmit2(DataSource ds, String ipAddress, int pb,	AudioFormat format, String name) {
				this.ds = ds;
				this.ipAddress = ipAddress;
				this.portBase = pb;
				this.audioFormat = format;
				this.name = name;
			}
			
			public void start() {
				String result = new String();
				result = this.startTransmiter();
				if (result != null) {
					System.out.println("Error : " + result);
					System.exit(0);
				}
				//logger.info(name + "Start transmission"); //aqui
				System.out.println("AS:   Start transmission from " + name);		
			}
		
			public SendStream getSendStream () {
				return sendStream;
			}
		
			public synchronized String startTransmiter() {
				//logger.info(name + " startTransmiter() - Início "); //aqui
				String result;
				result = createProcessor();
				//logger.info(name + " startTransmiter() - 1 - result: " + result ); //aqui
				if (result != null) {
					processor.deallocate();
					processor.close();
					return result;
				}	//logger.info(name + " startTransmiter() - 2 - result: " + result ); //aqui
				result = createTransmitter();
				if (result != null) {
					processor.deallocate();
					processor.close();
					processor = null;
					return result;
				} //logger.info(name + " startTransmiter() -Last - result: " + result ); //aqui
				processor.start();
				return null;
			}
		
			public void stopTransmiter() {
				synchronized (this) {
					if (processor != null) {
						if (mediaTransmitting) { //Receives request BYE to close dialogue
							System.out.println("AS:   " + name + " is idle");
						}
						else 
							stopDialog(); //Sends request BYE to close dialogue
						processor.stop();
						processor.close();
						for (int i = 0; i < rtpMgrs.length; i++) {
							rtpMgrs[i].removeTargets( "Session ended.");
							rtpMgrs[i].dispose();
						}
						totalVotos++;
					}
				}
			}
		
			private synchronized String createProcessor() {
				//logger.info(name + " createProcessor -1  "); //aqui
				DataSource clonedDataSource;
				if (processor== null) {
					clonedDataSource = ((SourceCloneable) ds).createClone();
					if (ds == null) {
					    System.err.println("Cannot clone the given DataSource");
					    System.exit(0);
					}
					try {
						processor = javax.media.Manager.createProcessor(clonedDataSource);
					} catch (NoProcessorException npe) { return "Couldn't create processor"; }
					  catch (IOException ioe) { return "IOException creating processor"; } 
					boolean result = waitForState(processor, Processor.Configured);
					if (result == false)
						return "Couldn't configure processor";
					TrackControl [] tracks = processor.getTrackControls();
					if (tracks == null || tracks.length < 1)
						return "Couldn't find tracks in processor";
					ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
					processor.setContentDescriptor(cd);
					Format supported[];
					Format chosen = null;
					supported = tracks[0].getSupportedFormats();
					//logger.info(name + " createProcessor -2  "); //aqui
					if (supported.length > 0) {
						for (int i=0; i <= supported.length-1; i++) {
							if (supported[i].matches(audioFormat)) 
								chosen = supported[i];
						}
						if (chosen == null)
							chosen = supported[3];
						tracks[0].setFormat(chosen);
					} else {
						tracks[0].setEnabled(false);
					}
					//logger.info(name + " createProcessor -3 "); //aqui
					result = waitForState(processor, Controller.Realized);
					//logger.info(name + " createProcessor -4  "); //aqui
					if (result == false)
						return "Couldn't realize processor";
					dataOutput = processor.getDataOutput();
				}
				//logger.info(name + " createProcessor - last  "); //aqui
				return null;
			}
		    
			private String createTransmitter() {
				PushBufferDataSource pbds = (PushBufferDataSource)dataOutput;
				PushBufferStream pbss[] = pbds.getStreams();
				rtpMgrs = new RTPManager[pbss.length];
				SessionAddress localAddr, destAddr;
				InetAddress ipAddr;
				for (int i = 0; i < pbss.length; i++) {
					try {
						rtpMgrs[i] = RTPManager.newInstance();	    
						ipAddr = InetAddress.getByName(ipAddress);
						localAddr = new SessionAddress( InetAddress.getLocalHost(),	audioPort);
						//localAddr = new SessionAddress( InetAddress.getLocalHost(),	portBase); //Des-mudei
						destAddr = new SessionAddress( ipAddr, portBase);
						rtpMgrs[i].initialize( localAddr);
						rtpMgrs[i].addTarget( destAddr);
						sendStream = rtpMgrs[i].createSendStream(dataOutput, i);
						sendStream.start();
					} catch (Exception  e) { return e.getMessage(); }
				}
				return null;
			}
		
			public Integer getStateLock() {
				return stateLock;
			}
		
			public void setFailed() {
				failed = true;
			}
		
			private synchronized boolean waitForState(Processor p, int state) {
				//logger.info(name + "waitForState("+p+","+state+") - 1"); //aqui
				p.addControllerListener(new StateListener());
				failed = false;
				if (state == Processor.Configured) {
					p.configure(); }
				else if (state == Processor.Realized) {
					//logger.info(name + "waitForState() - 4"); //aqui
					p.realize();}
				//logger.info(name + "waitForState() - 4-1"); //aqui
				while (p.getState() < state && !failed) {
					synchronized (getStateLock()) {
						try {
							//logger.info(name + "waitForState() - 4-2"); //aqui
							getStateLock().wait();
							//logger.info(name + "waitForState() - 4-3"); //aqui
						} catch (InterruptedException ie) { return false; }
					}
				}
				//logger.info(name + "waitForState() - 5"); //aqui
				if (failed)
					return false;
				else
					return true;
			}
			
			class StateListener implements ControllerListener {
				public void controllerUpdate(ControllerEvent ce) {
					if (ce instanceof ControllerClosedEvent)
						setFailed();
					if (ce instanceof ControllerEvent) {
						synchronized (getStateLock()) {
							//getStateLock().notify(); //des-mudei aqui
							getStateLock().notifyAll(); 
						}
					}
					if ( (ce instanceof EndOfMediaEvent) &&  mediaTransmitting == true) {
						mediaTransmitting = false;
						stopTransmiter();
					}
				}
			}
		
		}

}
