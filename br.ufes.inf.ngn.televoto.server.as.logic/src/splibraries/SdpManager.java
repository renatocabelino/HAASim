package splibraries;


import java.util.*;

import javax.sdp.*;

public class SdpManager {

  SdpFactory mySdpFactory;
  SdpInfo mySdpInfo;
  byte[] mySdpContent;


  public SdpManager() {
    mySdpFactory = SdpFactory.getInstance();
  }

  public byte[] createSdp(SdpInfo sdpinfo) {

    try{
    //Session-level description lines
    Version myVersion = mySdpFactory.createVersion(0);
    long ss=SdpFactory.getNtpTime(new Date());
    Origin myOrigin = mySdpFactory.createOrigin("-",ss,ss,"IN","IP4",sdpinfo.IpAddress); //Mudei aqui: funcionar na rede externa
    //Origin myOrigin = mySdpFactory.createOrigin("-",ss,ss,"IN","IP4","200.137.71.139");
    SessionName mySessionName = mySdpFactory.createSessionName("-");
    Connection myConnection = mySdpFactory.createConnection("IN","IP4", sdpinfo.IpAddress);  //Mudei aqui: funcionar na rede externa
    //Connection myConnection = mySdpFactory.createConnection("IN","IP4", "200.137.71.139");

    //Time description lines
    Time myTime=mySdpFactory.createTime();
    Vector<Time> myTimeVector=new Vector<Time>();
    myTimeVector.add(myTime);

    //Media description lines
    int[] aaf=new int[1];
    aaf[0]=sdpinfo.aformat;

    //aaf[0]=sdpinfo.getAFormat();
    MediaDescription myAudioDescription = mySdpFactory.createMediaDescription("audio", sdpinfo.aport, 1, "RTP/AVP", aaf);
    Vector<MediaDescription> myMediaDescriptionVector=new Vector<MediaDescription>();
    myMediaDescriptionVector.add(myAudioDescription);

    SessionDescription mySdp = mySdpFactory.createSessionDescription();

    mySdp.setVersion(myVersion);
    mySdp.setOrigin(myOrigin);
    mySdp.setSessionName(mySessionName);
    mySdp.setConnection(myConnection);
    mySdp.setTimeDescriptions(myTimeVector);
    mySdp.setMediaDescriptions(myMediaDescriptionVector);

    mySdpContent=mySdp.toString().getBytes();

    }catch(Exception e){
      e.printStackTrace();
    }

    return mySdpContent;
  }

  public SdpInfo getSdp(byte[] content) {
    try{
      String s = new String(content);
      SessionDescription recSdp = mySdpFactory.createSessionDescription(s);

      String myPeerIp=recSdp.getConnection().getAddress();
      //String myPeerIp="200.139.71.139";

      //String myPeerName=recSdp.getOrigin().getUsername();
      Vector<?> recMediaDescriptionVector=recSdp.getMediaDescriptions(false);

        MediaDescription myAudioDescription = (MediaDescription) recMediaDescriptionVector.elementAt(0);
        Media myAudio = myAudioDescription.getMedia();
        int myAudioPort = myAudio.getMediaPort();
        Vector<?> audioFormats = myAudio.getMediaFormats(false);

        int myAudioMediaFormat = Integer.parseInt(audioFormats.elementAt(0).toString());

        //int myVideoPort=-1;//comentei
        //int myVideoMediaFormat=-1;//comentei

        if (recMediaDescriptionVector.size()>1) {
          //MediaDescription myVideoDescription = (MediaDescription)
        	recMediaDescriptionVector.elementAt(1);
          //Media myVideo = myVideoDescription.getMedia();
          //myVideoPort = myVideo.getMediaPort();
          //Vector<?> videoFormats = myVideo.getMediaFormats(false);
          //myVideoMediaFormat =  Integer.parseInt(videoFormats.elementAt(0).toString());
        }

      mySdpInfo=new SdpInfo();
      mySdpInfo.IpAddress=myPeerIp;
      mySdpInfo.aport=myAudioPort;
      mySdpInfo.aformat=myAudioMediaFormat;
    }catch(Exception e){ e.printStackTrace();  }

    return mySdpInfo;
  }
}
