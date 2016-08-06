/*
 * ----------------------------------------------------------------------------
 *  (C) Copyright CNPT in Tsinghua University, 2005.  All Rights Reserved.
 *  AUTHOR:      Tian bei hang
 *  DATE:        April,2005
 *
 * -----------------------------------------------------------------------------
 */
//package OspfTestAdapter;
import java.lang.IndexOutOfBoundsException;
import java.lang.ArrayStoreException;
import java.lang.NullPointerException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Calendar;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import cn.edu.tsinghua.cs.cnpt.ttcn.tci.TciEncoding;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TestAdapter;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriMessageImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriParameterImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriStatusImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.runtime.AbstractBaseCodec;
import  cn.edu.tsinghua.cs.cnpt.ttcn.runtime.BaseCodec;

import org.etsi.ttcn.tci.TciCDProvided;
import org.etsi.ttcn.tri.*;

import jpcap.*;

/**
 * This is an example implementation of a TestAdapter that implements the TRI
 * Operational interface as defined by ETSI. MyTestAdapter extends a generic
 * TestAdapter provided by Testing Technologies. However, the internal
 * functionality of this generic TestAdapter has just internal relevants and
 * should therefore be not ommited. For example TestAdapter implements also
 * the TriOperational interface and provides already an implementation of
 * timers. Therefore the timer operations of the TRI interfaces specification
 * are not implemented by MyTestAdapter.
 * 
 * <P></p>
 *
 * @author Tian bei hang
 * @version 0.1
 */
public class HELLO_PACKETTestAdapter extends TestAdapter implements TriCommunicationSA,
    TriPlatformPA, TciEncoding
{
//private QuickNotepad quicknotepad;

    private final int MSG_BUF_SIZE = 1600; // default data size
    private Object threadlock = new Object();
    private Thread udpReceiverThread = null;
    private boolean runThread = false;
	private JpcapSender sender=null;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private DatagramSocket rxSocket; 
    private DatagramSocket txSocket; 
private boolean nomacaddr=true;
DatagramPacket packet;
private byte[] euiaddr=new byte[16];
    // invoke the super constructor
    public HELLO_PACKETTestAdapter()
    {
       super();
//	     quicknotepad=notepad;

    }

	/**
	 * Returns a codec being capable of coding according to encodingName.
	 *
	 * @param encodingName the encoding name as described by the <code>with
	 *        encoding</code> attribute in the TTCN-3 module.
	 *
	 * @return TciCDProvided A codec implementing the <code>TciCDProvided</code> interface.
	 *
	 */
    public TciCDProvided getCodec(String encodingName)
    {
        if ((encodingName == null) || encodingName.equals(""))
        {
            //encodingName = "Sync";
            encodingName = "BaseCodec";
        }

        TciCDProvided codec = super.getCodec(encodingName);

        if (codec != null)
        {
            return codec;
        }

	if (encodingName.equals("BaseCodec"))
		{
            // if with encoding is not set.
            codec = new BaseCodec(RB);
            codecs.put(encodingName, codec);
        }
        else
        { // the encoding is unknown

            RB.getTciTMProvided().tciError("Unknown decoding " + encodingName);
        }

        // endif encodingName
        return codec;
    }

    /**
     * Execution of the test case will be cancelled by the test manager.
     */
    public void triCancel()
    {
        synchronized (threadlock)
        {
            if (!runThread)
            {
                return;
            }

            runThread = false;
        }

        while (udpReceiverThread.isAlive())
        {
            try
            {
                sleep(10);
            }
            catch (InterruptedException ie) {
	                   //quicknotepad.textArea.append("\n"+ie.toString());
			RB.logging.logDatapackets(ie.toString());                   
	    }
        }
    }

    /**
     * This operation is called by the TE when it executes a TTCN-3 call
     * operation on a <code>componentPort</code> that is mapped on a
     * <code>tsiPort</code>. On invocation of this operation the SA will
     * initiate the procedure call indicated by <code>signatureId</code> at
     * the <code>tsiPort</code>. The <code>componentPort</code> shall identify
     * the sender, e.g. in one-to-many connections to the
     * <code>tsiPort</code>. <code>sutAddress</code> denotes the in TTCN-3
     * optional address parameter. Procedure call parameters are enumerated in
     * <code>parameterList</code>. The <code>triCall</code> operation shall
     * return without waiting for the return of the issued procedure call. The
     * return value is 0 if the procedure call could be successfully
     * initiated, 1 otherwise.
     *
     * @param componentId identifier of the test component issuing the
     *        procedure call
     * @param tsiPortId identifier of the test system interface port via which
     *        the procedure call is sent to the SUT Adapter
     * @param sutAddress (optional) destination address within the SUT
     * @param signatureId identifier of the signature of the procedure call
     * @param parameterList a list of encoded parameters which are part of the
     *        indicated signature. The parameters in parameterList are ordered
     *        as they appear in the TTCN-3 signature declaration.
     *
     * @return The return status of the triCall operation. The return status
     *         indicates the local success (TRI_OK) or failure (TRI_Error) of
     *         the operation.
     */
    public TriStatus triCall(TriComponentId componentId, TriPortId tsiPortId,
        TriAddress sutAddress, TriSignatureId signatureId,
        TriParameterList parameterList)
    {
            return new TriStatusImpl("triCall: (from: " + componentId +
                ", to: " + tsiPortId + ",SUT address: " + sutAddress +
                ", signature: " + signatureId + ", with paramaters: " +
                parameterList + ") could not be initiated");
    }

    /**
     * This operation is called by the TE immediately before the execution of
     * any test case. The test case that is going to be executed is indicated
     * by the testCaseId.
     *
     * @param testcase identifier of the test case that is going to be executed
     * @param tsiList a list of test system interface ports defined for the
     *        test system
     *
     * @return The return status of the triExecuteTestcase operation. The
     *         return status indicates the local success (TRI_OK) or failure
     *         (TRI_Error) of the operation.
     */
    public TriStatus triExecuteTestcase(TriTestCaseId testcase,
        TriPortIdList tsiList)
    {

        return new TriStatusImpl();
    }


    /**
     * Maps a test component port to a system port.
     *
     * @param compPortId a port reference to the component port
     * @param tsiPortId a port reference to the system port.
     *
     * @return <code>TRI_OK</code> if <code>triMap</code> could be executed
     *         sucessfully <code>TRI_ERROR</code>, otherwise
     */
     public TriStatus triMap(final TriPortId compPortId,
	     final TriPortId tsiPortId)
     {
	     //stop previous thread .
	     cancel();
	     
	     //get mac address;
	     if(nomacaddr)
	     {
		     try
		     {		    
			     txSocket = new DatagramSocket();
				     byte[] mesgformac={(byte)0x00,(byte)0x00}; //任意给一段数据。
				     //InetAddress addr = InetAddress.getLocalHost();
				     InetAddress routeraddr = InetAddress.getByName("192.168.1.252");//任意给一个IP地址和端口。				     
				     packet = new DatagramPacket(mesgformac, mesgformac.length, routeraddr,
					     19000);
		     }
		     
		     catch (java.net.UnknownHostException sex)
		     {
			     RB.logging.logError(new Date().getTime(), this,"Unable to open socket for TSI Port: " +
				     tsiPortId.getPortName());
			     return new TriStatusImpl("UnknownHostException: " +
				     tsiPortId.getPortName());
		     }
		     catch (SocketException sex)
		     {
			     RB.logging.logError(new Date().getTime(), this,"Unable to open socket for TSI Port: " +
				     tsiPortId.getPortName());
			     return new TriStatusImpl("SocketException: " +
				     tsiPortId.getPortName());
		     }
	     }
	     
	     TriStatus mapStatus = CsaDef.triMap(compPortId, tsiPortId);
	     if (mapStatus.getStatus() != TriStatus.TRI_OK)
	     {
		     return mapStatus;
	     }
	     
	     // the port specified in the ATS
	     if (tsiPortId.getPortName().equals("SystemPort1"))//require the systemport must be systemport1or systemport2;
	     {
		     // prepare to be ready to communicate
		     String[] lists=Jpcap.getDeviceDescription();
		     
		     try{
			     RB.logging.logDatapackets("\nStart capturing on "+lists[1]+"\n");
			     final Jpcap jpcap=Jpcap.openDevice(Jpcap.getDeviceList()[1],1000,true,20);
			     
			     if(nomacaddr)
			     {
				     txSocket.send(packet);
					 txSocket.close();
					txSocket = null;
				     //RB.logging.logDatapackets("\nmac addr:");					
			     }
			     runThread = true;
			     udpReceiverThread = new Thread()
			     {
				     public void run()
				     {
					     try{
						     boolean mylock = runThread;
						     while (mylock)
						     {
							     Packet ttpacket=jpcap.getPacket();
								try{ 
							     if(nomacaddr)
							     {
							     //假设前提是收到的第一个包是自己发出去的包。
								     out.reset();
								     out.write(ttpacket.header, 6,6);//取得包的源mac地址。
									System.arraycopy(out.toByteArray(),0,euiaddr,8,3);
									System.arraycopy(out.toByteArray(),3,euiaddr,13,3);

									byte[] euipatch1={(byte)0xff,(byte)0xfe};
									byte[] euipatch2={(byte)0xfe,(byte)0x80,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};									

									System.arraycopy(euipatch1,0,euiaddr,11,2);
									System.arraycopy(euipatch2,0,euiaddr,0,8);
									euiaddr[8]|=0x02;
								     RB.logging.logDatapackets("\nmac addr:\n");
								     for (int j =0;j<euiaddr.length;j++)
								     {
									     RB.logging.logDatapackets(Integer.toHexString((int)((euiaddr[j]+256)%256)));
										 if((j%2!=0)&&(j!=euiaddr.length-1))
										 	{RB.logging.logDatapackets(":");}
								     }
								     RB.logging.logDatapackets("\n");
								     
								     nomacaddr=false;	
								     continue;
							     }
								 }
								catch(Exception e)
									{
										jpcap.closeDevice();	
								     		RB.logging.logDatapackets("create mac address error:"+e.toString()+"\n");
									     return ;									
									}
							     
							     if((ttpacket instanceof IPPacket)&&(((IPPacket)ttpacket).protocol==89)&&triMatchSourceAddr(euiaddr,((IPPacket)ttpacket).src_ip.getAddress()))
								     {
									     RB.logging.logDatapackets("\nEnqueue Packet Length:"+ttpacket.data.length);
									     RB.logging.logDatapackets("\nEnqueue Packet:\n"+ttpacket.toString());
									    // RB.logging.logDatapackets("\nEnqueue Packet2:\n"+((IPPacket)ttpacket).protocol);										 
									     out.reset();
									     out.write(ttpacket.data, 0,ttpacket.data.length);

										 for (int j =0;j<out.toByteArray().length;j++)
									     {
										     if(j%16==0)
										     {
											     RB.logging.logDatapackets("\n");
										     }
										     StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((out.toByteArray()[j]+256)%256)));
										     if(hexvalue.length()==1){hexvalue.insert(0,"0");}
										     RB.logging.logDatapackets(hexvalue+" ");
									     }
									     RB.logging.logDatapackets("\n");
									    
									     TriMessage rcvMessage = new TriMessageImpl(out.toByteArray());
									     
									     synchronized (threadlock)
									     {
										     if (runThread)
										     {
											     Cte.triEnqueueMsg(tsiPortId,null,compPortId.getComponent(),rcvMessage);
										     }
									     }
								     }
								     synchronized (threadlock)
								     {
									     mylock = runThread;
								     }
						     }
						     jpcap.closeDevice();	
						     out.close();
					     }
					     catch(Exception e){ 
						     jpcap.closeDevice();	
						     //quicknotepad.textArea.append("\n"+e.toString());
						     RB.logging.logDatapackets("\n"+e.toString());
						     return ;
					     }
				     }
			     };
			     
			     udpReceiverThread.start();
		     }
		     catch(java.io.IOException e)
		     {
			     //quicknotepad.textArea.append("\n"+e.toString());
			     RB.logging.logDatapackets("\n"+e.toString());
			     return new TriStatusImpl(e.getMessage());
		     }
	     }
	     else
	     {
		     // Indicates an error. Attention: NOT TRI CONFORM !
		     return new TriStatusImpl("triMap: (from: " + compPortId + ", to: " +
				     tsiPortId + ") not implemented");
	     }
	     
	     return new TriStatusImpl();
     }
     
     
     /**
     * This operation is called by the TE when it executes a TTCN-3 send
     * operation on a component port, which has been mapped to a TSI port.
     * This operation is called by the TE for all TTCN-3 send operations if no
     * system component has been specified for a test case, i.e., only a MTC
     * test component is created for a test case.  The encoding of sendMessage
     * has to be done in the TE prior to this TRI operation call.
     *
     * @param componentId identifier of the sending test component
     * @param tsiPortId identifier of the test system interface port via which
     *        the message is sent to the SUT Adapter
     * @param address (optional) destination address within the SUT
     * @param sendMessage the encoded message to be send
     *
     * @return The return status of the triSend operation. The return status
     *         indicates the local success (TRI_OK) or failure (TRI_Error) of
     *         the operation.
     */
    public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId,
        TriAddress address, TriMessage sendMessage)
    {
    
        try
        {
		//RB.logging.logDatapackets("\nSending Packet :\n");

            byte[] mesg = sendMessage.getEncodedMessage();
			
		String[] lists=Jpcap.getDeviceDescription();
		//JpcapSender sender=JpcapSender.openDevice("");
		JpcapSender sender=JpcapSender.openDevice2("");
		
		IPPacket p=new IPPacket();
		
		RB.logging.logDatapackets("\nSending  Length= "+mesg.length+"bytes Packet :");
		
		for (int j =0;j<mesg.length;j++)
			{
				if(j%16==0)
					{
						RB.logging.logDatapackets("\n");
					}
			StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((mesg[j]+256)%256)));
			if(hexvalue.length()==1){
				//hexvalue=hexvalue.insert(0,"0");
				hexvalue.insert(0,"0");
				}
						RB.logging.logDatapackets(hexvalue+" ");
			}
		
		RB.logging.logDatapackets("\n");
		
		 ByteArrayOutputStream ipout = new ByteArrayOutputStream();
			//ipout.reset();
			//ipout.write(mesg, 0, 20);//源，offset,length
			//p.header=ipout.toByteArray();
			//ipout.reset();
			//ipout.write(mesg,20,mesg.length-20);
			//p.data=ipout.toByteArray();
		ipout.reset();
		ipout.write(mesg,0,mesg.length);
		p.data=ipout.toByteArray();			
		sender.sendOspfv3Packet(p,euiaddr);	
		
		ipout.close();
		sender.closeDevice();

            return new TriStatusImpl();
        }
        catch (Exception ioex)
        {
              // quicknotepad.textArea.append("\n"+ioex.toString());
		RB.logging.logDatapackets("\n"+ioex.toString());

		sender.closeDevice();

            	//quicknotepad.textArea.append("\nError: TestAdapter cannot sending packet\n");
		RB.logging.logDatapackets("\nError: TestAdapter cannot sending packet\n");
		return new TriStatusImpl(ioex.getMessage());
        }
    }

    /**
     * Unmaps a test component port from a system port.
     *
     * @param compPortId A port reference to the component port
     * @param tsiPortId A port reference to the system port
     *
     * @return TRI_Error in case a connection could not be closed successfully
     *         or no such connection has been established previously, TRI_OK
     *         otherwise. The operation returns TRI_OK in case no dynamic
     *         connections have to be established by the test system.
     */
    public TriStatus triUnmap(TriPortId compPortId, TriPortId tsiPortId)
    {

        // the port specified in the ATS
	if (tsiPortId.getPortName().equals("SystemPort1"))
        {
            // stop listening
            synchronized (threadlock)
            {
                if (!runThread)
                {
                    return new TriStatusImpl();
                }

                runThread = false;
            }

            while (udpReceiverThread.isAlive())
            {
                try
                {
                    sleep(10);
                }
                catch (InterruptedException ie) {
		               //quicknotepad.textArea.append("\n"+ie.toString());
		               RB.logging.logDatapackets("\n"+ie.toString());
		}
            }

	 TriStatus mapStatus = CsaDef.triUnmap(compPortId, tsiPortId);

        if (mapStatus.getStatus() != TriStatus.TRI_OK)
        {
            return mapStatus;
        }

            return new TriStatusImpl();
        }
        else
        {
            // Indicates an error. Attention: NOT TRI CONFORM !	
            // TRI Conformant would be 
            // return new TriStatusImpl(TRI_ERROR); // no description
            return new TriStatusImpl("triUnmap: (from: " + compPortId +
                ", to: " + tsiPortId + ") not implemented");
        }

        //return new TriStatusImpl();
    }
	
    public boolean  triMatchSourceAddr(byte[]eui,byte[]capaddr)
    	{
		 for (int j =0;j<eui.length;j++)
		 	{
		 	if(eui[j]!=capaddr[j])
		 		{
		 			return true;
		 		}				
		 	}
		 return false;
    	}


}
