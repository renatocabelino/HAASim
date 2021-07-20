package br.ufes.inf.ngn.televoto.client.logic;
import gov.nist.javax.sip.header.Authorization;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.Supported;
import gov.nist.javax.sip.header.ims.PPreferredIdentityHeader;
import gov.nist.javax.sip.header.ims.Privacy;
import jade.core.Agent;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;
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
import javax.sip.header.AllowHeader;
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

import splibraries.AVTransmit2;
import splibraries.SdpInfo;
import splibraries.SdpManager;
import splibraries.Utils;

public class Listener extends Agent implements SipListener {
	
	private static final long serialVersionUID = 1L;
	private AddressFactory addressFactory;
	private ListeningPoint listeningPoint;
	private SipFactory sipFactory;
	private SipStack sipStack;
	private SipProvider sipProvider;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;
	private Properties properties;
	private AllowHeader allowHeader;
	private ContactHeader contactHeader;
	private FromHeader fromHeader;
	private ViaHeader viaHeader;
	private RouteHeader routeHeader;
	private Dialog dialog;
	private ClientTransaction clientTransaction;
	private ServerTransaction serverTransaction;
	private Response originalResponseReceived;
	private SdpManager sdpManager;
	private SdpInfo offerInfo;
	private DataSource myDataSource;
	private AVTransmit2 at;
	private AudioFormat afmt;
	private int status;
	private int audioPort;
	private int redial = 0;
	private int dialTimes = 0;
	//private int timeoutWaitingService;
	private Integer port;
	private Integer proxyPort;
	private String ip;
	private String name;
	private String userID;
	private String password;
	private String proxy;
	private String proxyIP;
	private String extension;
	private String domain;
	private String destination;
	private Boolean Unregistring = false;
	private Timer timer;
	private Calendar timeInvite;
	
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
	
	protected void takeDown() {
		Unregistring = true;
		register(1);
	}

	class KeepAlive extends TimerTask {
		public KeepAlive(){	}
				
		public void run() {
			register(1);
		}
	}
	
	//Send "INVITE" to AS
	public void sendInvite() {
		timeInvite = Calendar.getInstance();
		if (redial == 0)
			System.out.println(name + ": chamando " + destination);
		else
			System.out.println(name + ": chamando " + destination + "   (Rediscagem " + redial +")");
		try {
			offerInfo.IpAddress = ip;
			offerInfo.aport = audioPort;
			offerInfo.aformat = 0;
			byte[] content = sdpManager.createSdp(offerInfo);
			//Via
			viaHeader = headerFactory.createViaHeader(ip, port,"udp", null);
			viaHeader.setRPort();
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			viaHeaders.add(viaHeader);
			//To
			Address destinationAddress = addressFactory.createAddress("<sip:"+ destination + ">");
			javax.sip.address.URI myRequestURI = destinationAddress.getURI();
			ToHeader myToHeader = headerFactory.createToHeader(destinationAddress,null);																		
			//MaxForwards
			MaxForwardsHeader myMaxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
			//Call-ID
			CallIdHeader myCallIdHeader = sipProvider.getNewCallId();
			//CSeq
			int aux;
			do {
				aux = (new Random()).nextInt(1000);
			} while (aux <= 0);
			CSeqHeader myCSeqHeader=headerFactory.createCSeqHeader(aux * 1L,"INVITE");
			//Create SIP request
			Request myRequest = messageFactory.createRequest(myRequestURI,"INVITE", myCallIdHeader,myCSeqHeader,fromHeader,myToHeader,viaHeaders,myMaxForwardsHeader);
			//Contact
			myRequest.addHeader(contactHeader);
			//Allow		
			myRequest.addHeader(allowHeader);
			//Privacy
			myRequest.addHeader(new Privacy("none"));	
			//PPreferredIdentity	
			HeaderFactoryImpl myHeaderFactoryImpl = new HeaderFactoryImpl();
			PPreferredIdentityHeader myPPreferredIdentityHeader = myHeaderFactoryImpl.createPPreferredIdentityHeader(addressFactory.createAddress("sip:"+ userID));
			myRequest.addHeader(myPPreferredIdentityHeader);
			//Route
			routeHeader = headerFactory.createRouteHeader( addressFactory.createAddress("sip:orig@scscf."+domain+":6060;lr") );
			myRequest.addHeader(routeHeader);
			//Proxy
			SipURI outboundProxyURI = addressFactory.createSipURI("proxy", proxyIP);
			outboundProxyURI.setLrParam();
			outboundProxyURI.setPort(proxyPort);
			routeHeader = headerFactory.createRouteHeader(addressFactory.createAddress(outboundProxyURI));
			myRequest.addFirst(routeHeader);
			//Content Type
			myRequest.setContent(content, headerFactory.createContentTypeHeader("application","sdp"));
			clientTransaction = sipProvider.getNewClientTransaction(myRequest);
			clientTransaction.sendRequest();
			logger.info(">>> " + myRequest.toString());				
			status=WAIT_PROV;			
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	// Generates a request REGISTER
	/* step = 1 ----> first step authentication
	 * step = 2 ----> second step authentication */
	public void register(int step) {
		try {	
			viaHeader = headerFactory.createViaHeader(ip, port,"udp", null);
			ToHeader myToHeader = headerFactory.createToHeader(addressFactory.createAddress("<sip:"+ userID +">"), null);
			fromHeader = headerFactory.createFromHeader(addressFactory.createAddress("<sip:"+ userID +">"), "647554");
			ArrayList<ViaHeader> myViaHeaders = new ArrayList<ViaHeader>();
			myViaHeaders.add(viaHeader);
			MaxForwardsHeader myMaxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
			Random random = new Random();
			CSeqHeader myCSeqHeader = headerFactory.createCSeqHeader(random.nextInt(1000) * 1L, "REGISTER");		
			CallIdHeader myCallIDHeader = sipProvider.getNewCallId();
			SipURI myRequestURI = (SipURI) (addressFactory.createAddress("sip:"+ domain)).getURI();		
			Request myRegisterRequest = messageFactory.createRequest(myRequestURI,"REGISTER", myCallIDHeader, myCSeqHeader, fromHeader, myToHeader,myViaHeaders, myMaxForwardsHeader);
			//Expires
			ExpiresHeader myExpiresHeader;
			if (Unregistring) 
				myExpiresHeader = headerFactory.createExpiresHeader(0);
			else 
				myExpiresHeader = headerFactory.createExpiresHeader(3600);
			myRegisterRequest.addHeader(myExpiresHeader);
			//Allow
			allowHeader = headerFactory.createAllowHeader("INVITE, ACK, CANCEL, BYE, MESSAGE, OPTIONS, NOTIFY, PRACK, UPDATE, REFER");
			myRegisterRequest.addHeader(allowHeader);	
			//Contact
			contactHeader = headerFactory.createContactHeader( addressFactory.createAddress("sip:"+ name+ '@' + ip +":"+ port + ";transport=udp") );
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
			PPreferredIdentityHeader myPPreferredIdentityHeader = (new HeaderFactoryImpl()).createPPreferredIdentityHeader(addressFactory.createAddress("sip:"+ userID));
			myRegisterRequest.addHeader(myPPreferredIdentityHeader);	
			//Privacy
			myRegisterRequest.addHeader(new Privacy("none"));
			//Supported
			myRegisterRequest.addHeader(new Supported("path"));
			//Authentication
			if (step == 2) {
				AuthorizationHeader myWWWAuthenticateHeader = Utils.makeAuthHeader(headerFactory, originalResponseReceived, myRegisterRequest, userID, password);
				myRegisterRequest.addHeader(myWWWAuthenticateHeader);
			}
			clientTransaction = sipProvider.getNewClientTransaction(myRegisterRequest);
			clientTransaction.sendRequest();
			logger.info(">>> " + myRegisterRequest.toString());
			status=REGISTERING;
		}catch (Exception e) { e.printStackTrace(); }
	}
		

	public void setup () {
		try {
			//String log4jConfPath = "//etc//osgi//log4j.properties"; //comentei
			//PropertyConfigurator.configure(log4jConfPath);
			if (!Unregistring) { //REGISTER
				Object parametros[] = new Object[9];
				parametros = getArguments();
				destination = (String) parametros[8];						
				extension = (String) parametros[0];						
				domain    = (String) parametros[1]; 						
				userID = (String) extension + "@" + domain;
				password  = (String) parametros[2]; 						
				port = (Integer) parametros[4]; 		
				audioPort= (Integer) parametros[5];
				proxyIP = (String) parametros[6];
				proxyPort = (Integer) parametros[7];
				proxy     = parametros[6] + ":" + parametros[7] + "/UDP";
				name = (String) this.getLocalName();
				ip = InetAddress.getLocalHost().getHostAddress();
				dialTimes = (Integer) parametros[9];
				//timeoutWaitingService = (Integer) parametros[10];
				myDataSource =  Activator.ds;
				afmt = Activator.afmt;
				sipFactory = SipFactory.getInstance();
				sipFactory.setPathName("gov.nist");
			}
			sdpManager=new SdpManager();
			offerInfo=new SdpInfo();

			properties = new Properties();
			properties.setProperty("javax.sip.STACK_NAME", name);
			properties.setProperty("javax.sip.OUTBOUND_PROXY", proxy); //Proxy
			sipStack = sipFactory.createSipStack(properties);
			messageFactory = sipFactory.createMessageFactory();
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			if (!Unregistring) {
				listeningPoint = sipStack.createListeningPoint(ip, port, "udp");
				sipProvider = sipStack.createSipProvider(listeningPoint);
				sipProvider.addSipListener(this);
			}
			register(1);
			
		}catch (Exception e) { e.printStackTrace(); }
	}

	public void setOff(){
		try{
			sipProvider.removeSipListener(this);
			sipProvider.removeListeningPoint(listeningPoint);
			sipStack.deleteListeningPoint(listeningPoint);
			sipStack.deleteSipProvider(sipProvider);
			listeningPoint=null;
			sipProvider=null;
			sipStack=null;
			timer.cancel();
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	public void SendMedia(String IP, int aport) {
		at = new AVTransmit2(myDataSource, IP, aport, afmt, name);
		at.start();
	}

	public void processRequest(RequestEvent requestReceivedEvent) {
		Request requestReceived = requestReceivedEvent.getRequest();
		logger.info("<<< " + requestReceived.toString());
		String method = requestReceived.getMethod();
		Response myResponse;
		try{
			switch (status) {
				case WAIT_PROV:
					status=WAIT_ACK;
					serverTransaction = requestReceivedEvent.getServerTransaction();
					break;
				case ESTABLISHED:	
					if (method.equals("BYE")) {
						System.out.println(name + ": BYE");
						myResponse = messageFactory.createResponse(200,requestReceived);
						myResponse.addHeader(contactHeader);
						serverTransaction = requestReceivedEvent.getServerTransaction();
						serverTransaction.sendResponse(myResponse); 
						logger.info(">>> " + myResponse.toString());
						redial ++;
						if (redial <= dialTimes) {
							sendInvite();
						} else {
							status=IDLE;
							br.ufes.inf.ngn.televoto.client.service.Activator asService = new br.ufes.inf.ngn.televoto.client.service.Activator ();
							asService.takeOff(userID.split("@")[0]);
						}		
					}
					break;
				case RINGING:
					if (method.equals("CANCEL")) {
						myResponse = messageFactory.createResponse(200,requestReceived);
						serverTransaction.sendResponse(myResponse); 					
						logger.info(">>> "+myResponse.toString());
						status=IDLE;
					}
					break;
				}
		} catch (Exception e) { e.printStackTrace(); }
	}


	public void processResponse(ResponseEvent responseReceivedEvent) {
		try{
			Response responseReceived = responseReceivedEvent.getResponse();
			logger.info("<<< "+responseReceived.toString());	
			int myStatusCode = responseReceived.getStatusCode();
			Request myRequest;
			switch(status){
				case WAIT_PROV:
					clientTransaction = responseReceivedEvent.getClientTransaction();
					switch (myStatusCode) {
						case 180: case 100:
							dialog = clientTransaction.getDialog();
							status = WAIT_FINAL;
							break;
						case 200:
							dialog = clientTransaction.getDialog();
							myRequest = dialog.createAck( ((CSeqHeader) clientTransaction.getRequest().getHeader(CSeqHeader.NAME)).getSeqNumber() );
							myRequest.addHeader(contactHeader);
							dialog.sendAck(myRequest);
							status = ESTABLISHED;
							logger.info(">>> "+myRequest.toString());
							break;	
						case 408: case 486: case 503: case 504: case 600: case 606: //Fazer redial 						
							long diff = Calendar.getInstance().getTimeInMillis() - timeInvite.getTimeInMillis();
							System.out.println(name + ": Timeout -- Code: " + myStatusCode + "   ("+ diff/1000 +")");
							dialog = clientTransaction.getDialog();
							status = IDLE;	
							redial ++;
							if (redial <= dialTimes) {
								sendInvite();
							} else {
								status=IDLE;
								br.ufes.inf.ngn.televoto.client.service.Activator asService = new br.ufes.inf.ngn.televoto.client.service.Activator ();
								asService.takeOff(userID.split("@")[0]);
							}	
							break;
					}
					break;
				case WAIT_FINAL:
					clientTransaction = responseReceivedEvent.getClientTransaction(); 
					switch (myStatusCode) {
						case 100: case 180:
							dialog=clientTransaction.getDialog();
							status=WAIT_FINAL;
							break;
						case 200:
							dialog = clientTransaction.getDialog();
							myRequest = dialog.createAck( ((CSeqHeader) clientTransaction.getRequest().getHeader(CSeqHeader.NAME)).getSeqNumber() ); 
							myRequest.addHeader(contactHeader);
							dialog.sendAck(myRequest);
							status = ESTABLISHED;
							logger.info(">>> "+myRequest.toString());
							break;
						case 408: case 486: case 503: case 504: case 600: case 606:
							long diff = Calendar.getInstance().getTimeInMillis() - timeInvite.getTimeInMillis();
							System.out.println(name + ": Timeout -- Code: " + myStatusCode + "   ("+ diff/1000 +")");
							dialog = clientTransaction.getDialog();
							status = IDLE;	
							redial ++;
							if (redial <= dialTimes) {
								sendInvite();
							} else {
								status=IDLE;
								br.ufes.inf.ngn.televoto.client.service.Activator asService = new br.ufes.inf.ngn.televoto.client.service.Activator ();
								asService.takeOff(userID.split("@")[0]);
							}	
							break;
					}
					break;
				case REGISTERING:
					clientTransaction = responseReceivedEvent.getClientTransaction();
					switch (myStatusCode) {
						case 200:
							status=IDLE;
							if (!Unregistring) {
								timer = new Timer();
								timer.schedule(new KeepAlive(),55*60*1000);//Re-register em 55 minutos
								System.out.println(name + ": Registrado");					
								sendInvite();
							} else
								this.setOff();
							break;
						case 401:
							originalResponseReceived = responseReceived;
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

}