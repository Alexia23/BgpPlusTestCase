//package cn.edu.tsinghua.cs.cnpt.ttcn.tools;
import java.lang.IndexOutOfBoundsException;
import java.lang.ArrayStoreException;
import java.lang.NullPointerException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.Calendar;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

//import java.net.DatagramPacket;
import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;

import cn.edu.tsinghua.cs.cnpt.ttcn.tci.TciEncoding;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TestAdapter;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriMessageImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriParameterImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriStatusImpl;
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
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BGP_TestAdapter_jpcap extends TestAdapter implements TriCommunicationSA,TriPlatformPA, TciEncoding 
{
	private final int MSG_BUF_SIZE = 1600; // default data size
	private Object threadlock = new Object();
	private Thread udpReceiverThread = null;
	private boolean runThread = false;
	private JpcapSender sender=null;
  	final ByteArrayOutputStream out = new ByteArrayOutputStream();
   
	private Socket txSocket; // the transmitter UDP socket used for asynchronous communication
	OutputStream os;
   // invoke the super constructor 
	public BGP_TestAdapter_jpcap()
	{
		super();
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
		 {// if with encoding is not set
			 codec = new BaseCodec(RB);
			 codecs.put(encodingName, codec);
		 }
		 else
		 { // the encoding is unknown
			 RB.getTciTMProvided().tciError("Unknown decoding " + encodingName);
		 }// endif encodingName
		 
		 return codec;
	 } // end public TciCDProvided getCodec()

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
		     		sleep(500); 
		     	}
		     catch (InterruptedException ie) 
		     {
			     RB.logging.logDatapackets("\n"+ie.toString()); 
		     }
	     }
	 } //end public void cancel()

   
   public TriStatus triCall(TriComponentId componentId, TriPortId tsiPortId,TriAddress sutAddress, TriSignatureId signatureId,TriParameterList parameterList)
	{
		return new TriStatusImpl("triCall: (from: " + componentId +", to: " + tsiPortId + ",SUT address: " + sutAddress +", signature: " + signatureId + ", with paramaters: " +parameterList + ") could not be initiated");	    
		
	} //end public TriStatus triCall()

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
	public TriStatus triExecuteTestcase(TriTestCaseId testcase,TriPortIdList tsiList)
	{
		return new TriStatusImpl();
		
	} // end public TriStatus triExecuteTestcase()

	/**
	* Maps a test component port to a system port.
	*
	* @param compPortId a port reference to the component port
	* @param tsiPortId a port reference to the system port.
	*
	* @return <code>TRI_OK</code> if <code>triMap</code> could be executed
	*         sucessfully <code>TRI_ERROR</code>, otherwise
	*/
	public TriStatus triMap(final TriPortId compPortId,final TriPortId tsiPortId) //throws IOException
	{
		try
		{
			InetAddress routeraddr = InetAddress.getByName("192.168.1.165");
			int BGPport = 179;
			txSocket = new Socket(routeraddr,BGPport);
			os = txSocket.getOutputStream();
		}
		catch (SocketException sex)
		{
			RB.logging.logDatapackets("\n"+sex.toString());
			return new TriStatusImpl("Unable to open socket for TSI Port: " +tsiPortId.getPortName());
		}
		catch (IOException sio)
		{
			RB.logging.logDatapackets("\n"+sio.toString());	
		}
		
		
		TriStatus mapStatus = CsaDef.triMap(compPortId, tsiPortId);        
        
		if (mapStatus.getStatus() != TriStatus.TRI_OK)
		{ 
			return mapStatus; 
		}

		// the port specified in the ATS        
		if (tsiPortId.getPortName().equals("BGP_Interface_Port"))
		{
			// prepare to be ready to communicate
			String[] lists=Jpcap.getDeviceDescription();//get the net devices list
			try
			{
				RB.logging.logDatapackets("\nStart capturing on "+lists[1]+"\n");
				final Jpcap jpcap=Jpcap.openDevice(Jpcap.getDeviceList()[1],1000,true,100);

				runThread = true;
				udpReceiverThread = new Thread()
				{
					public void run() 
					{
						try 
						{
							boolean mylock = runThread;
							while (mylock) 
							{
								Packet ttpacket=jpcap.getPacket();
								if(ttpacket instanceof IPPacket)
								{//filter IP packet
									if(((IPPacket)ttpacket).src_ip.getHostAddress().equalsIgnoreCase("192.168.1.165"))
									{//filter192.168.1.165 packet
										if(ttpacket instanceof TCPPacket)
										{//filterTCP packet
											if(((TCPPacket)ttpacket).src_port == 179)
											{//filter 179 port packet
												out.reset();
												out.write(ttpacket.data, 0,ttpacket.data.length);
												//only print the packet being enqueued
												if(out.toByteArray().length > 0)
												{
													RB.logging.logDatapackets("\nReceiving  Length= "+out.toByteArray().length+"bytes Packet :");
													for(int j =0;j<out.toByteArray().length;j++)
													{
														if(j%16==0)
														{
															RB.logging.logDatapackets("\n");
														}
														StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((out.toByteArray()[j]+256)%256)));
														if(hexvalue.length()==1)
														{
															hexvalue=hexvalue.insert(0,"0");
														}
														RB.logging.logDatapackets(hexvalue+" ");
													}
													RB.logging.logDatapackets("\n");
													TriMessage rcvMessage = new TriMessageImpl(out.toByteArray());
													
													synchronized (threadlock)
													{
														RB.logging.logDatapackets("\n threadlock");
														if (runThread)
														{
															RB.logging.logDatapackets("\n runThread");
															Cte.triEnqueueMsg(tsiPortId,null,compPortId.getComponent(),rcvMessage);
														}
													}
												}
											}
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
						catch(Exception e)
						{ 
							jpcap.closeDevice();
							RB.logging.logDatapackets("\n"+e.toString());
							return ;
						}
					}
				};
				udpReceiverThread.start(); 
			}
			catch(java.io.IOException e)
			{
				RB.logging.logDatapackets("\n"+e.toString());
				return new TriStatusImpl(e.getMessage());
			}
		}
		else
		{// Indicates an error. Attention: NOT TRI CONFORM !
			return new TriStatusImpl("triMap: (from: " + compPortId + ", to: " +tsiPortId + ") not implemented");
		}
		return new TriStatusImpl();
	} // end public TriStatus triMap()

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
	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId,TriAddress address, TriMessage sendMessage)
	{
		RB.logging.logDatapackets("Begin the triSend():\t");
		try
		{
			byte[] mesg = sendMessage.getEncodedMessage();
			RB.logging.logDatapackets("MyBGPTestAdapter: Sending (to:Router )"+ mesg.length+" Btyes\n" );
			os.write(mesg);
			//os.flush();
			//print the send messege
			RB.logging.logDatapackets("\nSending  Length= "+mesg.length+"bytes Packet :");
			for (int j =0;j<mesg.length;j++)
			{
				if(j%16==0)
				{ 
					RB.logging.logDatapackets("\n");
				}
				StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((mesg[j]+256)%256)));
				if(hexvalue.length()==1)
				{
					hexvalue=hexvalue.insert(0,"0"); 
				}
				RB.logging.logDatapackets(hexvalue+" ");
			} //print the packet of sending
			return new TriStatusImpl();
		}
		catch (Exception ioex)
		{
			RB.logging.logDatapackets("\n"+ioex.toString());
			return new TriStatusImpl(ioex.getMessage());
		}
	} // end public TriStatus triSend()

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
		if (tsiPortId.getPortName().equals("BGP_Interface_Port"))
		{// stop listening
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
					sleep(500); 
				}
				catch (InterruptedException ie) 
				{
					RB.logging.logDatapackets("\n"+ie.toString());
				}
			}
			
			TriStatus mapStatus = CsaDef.triUnmap(compPortId, tsiPortId);
			if (mapStatus.getStatus() != TriStatus.TRI_OK)
			{ 
				return mapStatus; 
			}
			// close the sender socket
			if (txSocket != null)
			{
				try
				{
					txSocket.close();
					txSocket = null;
				}
				catch (IOException sio)
				{
					RB.logging.logDatapackets("\n"+sio.toString());
				}
			}
			return new TriStatusImpl();
		}
		else
		{
			// Indicates an error. Attention: NOT TRI CONFORM !	
			// TRI Conformant would be 
			// return new TriStatusImpl(TRI_ERROR); // no description
			return new TriStatusImpl("triUnmap: (from: " + compPortId +", to: " + tsiPortId + ") not implemented");
		}
	} //end public TriStatus triUnmap()
	    
}// end public class BGP_TestAdapter_jpcap
