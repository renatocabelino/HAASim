package br.ufes.inf.haasim.web;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;


@SuppressWarnings("rawtypes")
public class HttpServiceTracker extends ServiceTracker {
 
    @SuppressWarnings("unchecked")
	public HttpServiceTracker(BundleContext context) {
        super(context, HttpService.class.getName(), null);   
    
        Thread tSocket = new Thread( new Socket() );
        tSocket.start();
     }
 
    @SuppressWarnings({ "unchecked" })
	public Object addingService(ServiceReference reference) {
        HttpService httpService = (HttpService) context.getService(reference);
        try {  
            httpService.registerServlet("/televoto", new TelevotoServlet(), null, null);
            httpService.registerResources("/pages", "/WebContent/pages", null);
        } catch (Exception e) { e.printStackTrace(); }
        return httpService;
    }       
     
    @SuppressWarnings({ "unchecked" })
	public void removedService(ServiceReference reference, Object service) {
        HttpService httpService = (HttpService) service;
        httpService.unregister("/televoto");
        httpService.unregister("/pages");
        super.removedService(reference, service);
        //Parar socket
    }
    
    private class Socket implements Runnable {
    	boolean on = false;
		@Override
		public void run() {
			System.out.println("Socket Escutando");
	        int porta = 9999;
	     	DatagramSocket socket = null;
	     	DatagramPacket pacote = null;
	     	byte[] dados;
	     	if (on == false) {
		     	try {
		     		socket = new DatagramSocket(porta);
		     		dados = new byte[100];
		     		pacote = new DatagramPacket(dados, dados.length);
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
    }
 
}
