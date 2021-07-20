package splibraries;

import java.io.IOException;

import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.SourceCloneable;

public class CloneDS {

	private MediaLocator locator;
	//private Format format; //comentei
	private int Clones;
	private Processor processor = null;
	//private RTPManager rtpMgrs[]; //comentei
	private DataSource dataOutput = null;
	private DataSource dataSources[];
	private Processor myProcessor;
	private DataSink dataSink;
	private String IP;
	private int port;

	private Integer stateLock = new Integer(0);
	private boolean failed = false;

	public CloneDS(MediaLocator locator, Format format, int clones) {
		this.locator = locator;
		//this.format = format; //comentei
		this.Clones = clones;
		dataSources = new DataSource[Clones+1];

	}
	
	public CloneDS(MediaLocator locator, String IP, int port, Format format) {
		this.locator = locator;
		this.IP = IP;
		this.port = port;
		//this.format = format;	//comentei
	}

	Integer getStateLock() {
		return stateLock;
	}

	void setFailed() {
		failed = true;
	}

	private synchronized boolean waitForState(Processor p, int state) {
		p.addControllerListener(new StateListener());
		failed = false;

		if (state == Processor.Configured) {
			p.configure();
		} else if (state == Processor.Realized) {
			p.realize();
		}

		while (p.getState() < state && !failed) {
			synchronized (getStateLock()) {
				try {
					getStateLock().wait();
				} catch (InterruptedException ie) {
					return false;
				}
			}
		}

		if (failed)
			return false;
		else
			return true;
	}

	/****************************************************************
	 * Inner Classes
	 ****************************************************************/

	class StateListener implements ControllerListener {

		public void controllerUpdate(ControllerEvent ce) {

			if (ce instanceof ControllerClosedEvent)
				setFailed();

			if (ce instanceof ControllerEvent) {
				synchronized (getStateLock()) {
					getStateLock().notifyAll();
				}
			}
		}
	}

	public String createProcessor() {
		DataSource cloneableDataSource = null;
		
		if (locator == null)
			return "Locator is null";

		DataSource ds;
		try {
			ds = javax.media.Manager.createDataSource(locator);
		} catch (Exception e) {
			return "Couldn't create DataSource";
		}

		try {
			processor = javax.media.Manager.createProcessor(ds);
		} catch (NoProcessorException npe) {
			return "Couldn't create processor";
		} catch (IOException ioe) {
			return "IOException creating processor";
		}

		boolean result = waitForState(processor, Processor.Configured);
		if (result == false)
			return "Couldn't configure processor";

		TrackControl[] tracks = processor.getTrackControls();

		if (tracks == null || tracks.length < 1)
			return "Couldn't find tracks in processor";

		ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
		processor.setContentDescriptor(cd);

		Format supported[];
		Format chosen = null;

		Format format = tracks[0].getFormat();
		if (tracks[0].isEnabled()) {

			supported = tracks[0].getSupportedFormats();

			if (supported.length > 0) {
				for (int i = 0; i <= supported.length-1; i++)
					if (format.equals(supported[i]))
						chosen = format;
				if (chosen == null)
					chosen = supported[3];
				tracks[0].setFormat(chosen);
				System.err.println("Track 0 is set to transmit as:");
				System.err.println("  " + chosen);
			} else
				tracks[0].setEnabled(false);
		} else
			tracks[0].setEnabled(false);

		result = waitForState(processor, Controller.Realized);
		if (result == false)
			return "Couldn't realize processor";
		myProcessor = processor;
		dataOutput = processor.getDataOutput();
        cloneableDataSource = Manager.createCloneableDataSource(dataOutput); 
		
		for (int i=0; i <= Clones; i++) 
            dataSources[i] = ((SourceCloneable)cloneableDataSource).createClone();
    	
		return null;
	}
	
	public String CreateDataSink() {
		//DataSource cloneableDataSource = null; //comentei
		
		if (locator == null)
			return "Locator is null";

		DataSource ds;
		try {
			ds = javax.media.Manager.createDataSource(locator);
		} catch (Exception e) {
			return "Couldn't create DataSource";
		}

		try {
			processor = javax.media.Manager.createProcessor(ds);
		} catch (NoProcessorException npe) { return "Couldn't create processor"; } 
		  catch (IOException ioe) { return "IOException creating processor"; }

		boolean result = waitForState(processor, Processor.Configured);
		if (result == false)
			return "Couldn't configure processor";

		TrackControl[] tracks = processor.getTrackControls();

		if (tracks == null || tracks.length < 1)
			return "Couldn't find tracks in processor";

		ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
		processor.setContentDescriptor(cd);

		Format supported[];
		Format chosen = null;

		Format format = tracks[0].getFormat();
		if (tracks[0].isEnabled()) {

			supported = tracks[0].getSupportedFormats();

			if (supported.length > 0) {
				for (int i = 0; i <= supported.length-1; i++)
					if (format.equals(supported[i]))
						chosen = format;
				if (chosen == null)
					chosen = supported[3];
				tracks[0].setFormat(chosen);
				System.err.println("Track 0 is set to transmit as:");
				System.err.println("  " + chosen);
			} else
				tracks[0].setEnabled(false);
		} else
			tracks[0].setEnabled(false);

		result = waitForState(processor, Controller.Realized);
		if (result == false)
			return "Couldn't realize processor";
		myProcessor = processor;
		dataOutput = processor.getDataOutput();
		
		String url= "rtp://"+ IP + ":" + port  + "/audio/1";
		 
        MediaLocator m = new MediaLocator(url);
		
		try {
			dataSink = Manager.createDataSink(dataOutput, m);
		} catch (NoDataSinkException e) { 
			e.printStackTrace();
			return "Error creating DataSink for MediaLocator.";
		}
		
		return null;
	}
	
	public DataSource[] getDataSources() {
		return this.dataSources;
	}
	
	public Processor getProcessor() {
		return this.myProcessor;
	}
	
	public DataSink getDataSink() {
		return this.dataSink;
	}
}
