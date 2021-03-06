/*
 * Created on 2005-3-28
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
 
//package cn.edu.tsinghua.cs.cnpt.ttcn.tools;

import java.io.*;
import java.lang.reflect.Method;

//import java.net.DatagramPacket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
/*
import cn.edu.tsinghua.cs.cnpt.ttcn.tci.TciEncoding;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TestAdapter;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriMessageImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriParameterImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriStatusImpl;
*/
import cn.edu.tsinghua.cs.cnpt.ttcn.tci.TciEncoding;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TestAdapter;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriMessageImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriParameterImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.tri.TriStatusImpl;
import cn.edu.tsinghua.cs.cnpt.ttcn.runtime.AbstractBaseCodec;
import  cn.edu.tsinghua.cs.cnpt.ttcn.runtime.BaseCodec;

import org.etsi.ttcn.tci.TciCDProvided;
import org.etsi.ttcn.tri.*;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BGP_TestAdapter extends TestAdapter implements TriCommunicationSA,TriPlatformPA, TciEncoding 
{
	private final int MSG_BUF_SIZE = 1600; // default data size
	private Object threadlock = new Object();
	private Thread tcpReceiverThread = null;
	private boolean runThread = false;
	private Socket rxSocket; // the receiver UDP socket used for asynchronous communication
	private DatagramSocket  udpSocket;
	private DatagramPacket udpPacket;
	InputStream is;
	OutputStream os;
	boolean tcpFlag = false;
    
	public BGP_TestAdapter()
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
		 /*else if (encodingName.equals("Async"))
		 {
			 codec = new AsyncCodec(RB);
			 codecs.put(encodingName, codec);
		 }
		 else if (encodingName.equals("BGPc"))
		 {
			 codec = new BGPcCodec(RB);
			 codecs.put(encodingName, codec);
		 }*/
		 else
		 { // the encoding is unknown
			 RB.getTciTMProvided().tciError("Unknown decoding " + encodingName);
		 }
		 // endif encodingName
		 return codec;
	 } // end public TciCDProvided getCodec()

	 /**
	 * Execution of the test case will be cancelled by the test manager.
	 */
	 public void triCancel()
	 {
	     // SUT = null;
	     synchronized (threadlock)
	     {
		     if (!runThread)
		     { return; }
	      	     runThread = false;
	     }

	     while (tcpReceiverThread.isAlive())
	     {
		     try
		     { sleep(500); }
		     catch (InterruptedException ie) 
		     {
			     //quicknotepad.textArea.append("\n"+ie.toString());
				 RB.logging.logDatapackets("\n"+ie.toString()); 
		     }
	     }

	     if (rxSocket != null)
	     {
		     try 
		     {
			     rxSocket.close();
			     rxSocket = null;
		     }
		     catch (IOException sio)
		     {
			     //quicknotepad.textArea.append("\n"+sio.toString());
				 RB.logging.logDatapackets("\n"+sio.toString());
		     }
	     }
	 } //end public void cancel()

   
   	public TriStatus triCall(TriComponentId componentId, TriPortId tsiPortId,TriAddress sutAddress, TriSignatureId signatureId,TriParameterList parameterList)
	{
		/*Object callReturnValue;
		// the port specified in the ATS
		if (tsiPortId.getPortName().equals("SystemPort"))
		{
		    Object[] params = null;
				try
		    {
			params = parseInParameterList(parameterList);
		    }
				catch (RuntimeException tex) {
					return new TriStatusImpl("Error in parsing parameter list: " +
					tex.getMessage());
				}
		    Method[] methods = SUT.getClass().getMethods();
	
		    // search the respective method
		    for (int i = methods.length; i-- > 0;)
		    {
			if (methods[i].getName().equals(signatureId.getSignatureName()))
			{
			    try
			    {
				Class returnType = methods[i].getReturnType();
				callReturnValue = methods[i].invoke(SUT, params);
			       
				if (returnType != Void.TYPE)
				{
				    TriParameter encodedReply = new TriParameterImpl(encodeObject(
						callReturnValue),
					    TriParameterPassingMode.TRI_INOUT);
				    Cte.triEnqueueReply(tsiPortId, sutAddress,
					componentId, signatureId, parameterList,
					encodedReply);
				}
				else
				{
				    Cte.triEnqueueReply(tsiPortId, sutAddress,
					componentId, signatureId, parameterList, null);
				}
	
				return new TriStatusImpl();
			    }
			    catch (Exception iax)
			    {
				iax.printStackTrace();
	
				return new TriStatusImpl("Access violation: " +
				    iax.getMessage());
			    }
			}
		    }*/

		return new TriStatusImpl("triCall: (from: " + componentId +", to: " + tsiPortId + ",SUT address: " + sutAddress +", signature: " + signatureId + ", with paramaters: " +parameterList + ") could not be initiated");	    
		/*}
		else
		{
			return new TriStatusImpl("triCall: (from: " + componentId +", to: " + tsiPortId + ",SUT address: " + sutAddress +", signature: " + signatureId + ", with paramaters: " + parameterList + ") not implemented");
		}*/
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
		//quicknotepad.textArea.append("naive6 CsaDef:"+CsaDef.toString());
		//RB.logging.logDatapackets("naive6 CsaDef:"+CsaDef.toString());
		//CsaDef: TACReflection;triMap->tciMapReq->tciMap;;
		//Do Some Pre_work;
		TriStatus mapStatus = CsaDef.triMap(compPortId, tsiPortId);        
        
		if (mapStatus.getStatus() != TriStatus.TRI_OK)
		{ return mapStatus; }

        	// the port specified in the ATS        
		//quicknotepad.textArea.append("naive6\t"+tsiPortId.getPortName());
		//RB.logging.logDatapackets("naive6\t"+tsiPortId.getPortName());
		String ipAddr = "";
		if (tsiPortId.getPortName().equals("BGP_Interface_PortA"))
		{
			ipAddr = "2001:0:2::6";
		}
		else if (tsiPortId.getPortName().equals("BGP_Interface_PortB"))
		{
			ipAddr = "2001:0:3::6";
		}
		else if (tsiPortId.getPortName().equals("BGP_Interface_PortC"))
		{
			ipAddr = "2001:0:1::6";
		}
		else if (tsiPortId.getPortName().equals("BGP_Interface_PortAData"))
		{
			ipAddr = "2001:0:2::6";
		}
		else
		{
			// Indicates an error. Attention: NOT TRI CONFORM !	
			// TRI Conformant would be 
			// return new TriStatusImpl(TRI_ERROR); // no description
			return new TriStatusImpl("triUnmap: (from: " + compPortId +", to: " + tsiPortId + ") not implemented");
		}
		
		if (tsiPortId.getPortName().equals("BGP_Interface_PortA"))
		{
			// prepare to be ready to communicate           
			try
			{
				InetAddress routeraddr = InetAddress.getByName(ipAddr);
				int BGPport = 179;
				try
				{
					rxSocket = new Socket(routeraddr,BGPport);
					RB.logging.logDatapackets("port open");
				} catch(Exception e)
				{
					RB.logging.logDatapackets("port not open");
				}
				
            	
				is = rxSocket.getInputStream();
				os = rxSocket.getOutputStream();
				runThread = true;
				tcpReceiverThread = new Thread()
				{
					public void run() 
					{
						boolean mylock = runThread;
						try 
						{
							rxSocket.setSoTimeout(10000);			  
							while (mylock) 
							{
								byte[] msg = new byte[MSG_BUF_SIZE];
								try 
								{
									//int length = rxSocket.getInputStream().read(msg);
									int length = is.read(msg);
									if(length == -1)
									{
										throw new Exception("TCP connection finished.");
									}							
									if (length > 0) 
									{
										//quicknotepad.textArea.append("\nMyBGPTestAdapter: Received ( " + length + " bytes)\n----------------\n" + new String(msg, 0, length));
										RB.logging.logDatapackets("\n\nMyBGPTestAdapter: Received ( " + length + " bytes)" );
										// It is a asynchronous message
										
										
										
										byte[] Con = new byte[length];
										for (int i=0;i<length;i++) Con[i]=msg[i];
										
										//print the receive message
										RB.logging.logDatapackets("\nReceiving  Length= "+Con.length+"bytes Packet :");
										for (int j =0;j<Con.length;j++)
										{
											if(j%16==0)
												RB.logging.logDatapackets("\n");
											StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((Con[j]+256)%256)));
											if(hexvalue.length()==1)
												hexvalue=hexvalue.insert(0,"0");
											RB.logging.logDatapackets(hexvalue+" ");
										}
										
										if(Con[18]==3&&Con.length>21)
										{
											Con = HandleNotification(Con);
											RB.logging.logDatapackets("\nAfter HandleNotification the frame: "+Con.length+"bytes Packet :");
											for (int j =0;j<Con.length;j++)
											{
												if(j%16==0)
													RB.logging.logDatapackets("\n");
												StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((Con[j]+256)%256)));
												if(hexvalue.length()==1)
													hexvalue=hexvalue.insert(0,"0");
												RB.logging.logDatapackets(hexvalue+" ");
											}
										}
																				
										//end print the receive message										
                              
										TriMessage rcvMessage = new TriMessageImpl(Con);
										
										RB.logging.logDatapackets("\nIn the Received Thread:\t received message length:"+ rcvMessage.getEncodedMessage().length);
										synchronized (threadlock) 
										{
											if (runThread) 
											{
												Cte.triEnqueueMsg(tsiPortId, null, compPortId.getComponent(),rcvMessage);
											}
										}
									}
								}
								catch (InterruptedIOException iioex) 
								{
									RB.logging.logDatapackets("\n"+iioex.toString());
								}
								catch (IOException ioex) 
								{
									RB.logging.logDatapackets("\n"+ioex.toString());
									if (rxSocket != null) 
									{
										rxSocket.close();
										rxSocket = null;
									}
									return;
								}
								catch (Exception ex) 
								{
									RB.logging.logDatapackets("\n"+ex.toString());
									if (rxSocket != null) 
									{
										rxSocket.close();
										rxSocket = null;
									}
									return;
								}
								synchronized (threadlock) 
								{
									mylock = runThread;
								}
							}

							if (rxSocket != null) 
							{
								rxSocket.close();
								rxSocket = null;
							}
						}
						catch (IOException se) 
						{
							RB.logging.logDatapackets("\n"+"MyTestAdapter: SocketException "+se.toString());
						}
					}
				};
				tcpReceiverThread.start(); 
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
			// listen on the receiver socket            
		}
		else if (tsiPortId.getPortName().equals("BGP_Interface_PortB")) 
		{
			try {
				RB.logging.logDatapackets("\n new  udp socket\t\n");
				udpSocket = new DatagramSocket();
				RB.logging.logDatapackets("\n new  successful udp socket\t\n");
			} catch (Exception ex) {
				RB.logging.logDatapackets("\n"+ex.toString());
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
		RB.logging.logDatapackets("\nBegin the triSend():\t\n");
		if (tsiPortId.getPortName().equals("BGP_Interface_PortA"))
		{
			try
			{
				byte[] mesg = sendMessage.getEncodedMessage();
				RB.logging.logDatapackets("MyBGPTestAdapter: Sending (to:Router )"+ mesg.length+" Btyes" );
				os.write(mesg);
				//os.flush();
				//print the send messege
				RB.logging.logDatapackets("\nSending  Length= "+mesg.length+"bytes Packet :");
				for (int j =0;j<mesg.length;j++)
				{
					if(j%16==0)
						RB.logging.logDatapackets("\n");
					StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((mesg[j]+256)%256)));
					if(hexvalue.length()==1)
					{ hexvalue=hexvalue.insert(0,"0"); }
					RB.logging.logDatapackets(hexvalue+" ");
				} //print the packet of sending
				return new TriStatusImpl();
			}
			catch (Exception ioex)
			{
				RB.logging.logDatapackets("\ntcp ioex"+ioex.toString());
				return new TriStatusImpl(ioex.getMessage());
			}
		} 
		else 
		{
			RB.logging.logDatapackets("\n Send Data By Udp\t\n");
			byte[] buf = "UDP Demo".getBytes();
		
			byte[] addr = new byte[]{32, 1, 0, 0, 0, 7, 0, 0,
			                          0, 0, 0, 0, 0, 0, 0, 1};   //2001:0:5::1  over127 need(byte) : (byte)129

			//byte[] addr = new byte[]{32, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6}; //2001:0:3::6
 			InetAddress udpDestIP;
			try {
				udpDestIP = InetAddress.getByAddress(addr);
				RB.logging.logDatapackets("\n udpDestIP:" + udpDestIP.toString() + "\t\n");
				udpPacket = new DatagramPacket(buf,buf.length, udpDestIP, 20087);//10000为定义的端口	
				RB.logging.logDatapackets("\n DatagramPacket:" + udpPacket.toString() + "\t\n");
			} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
				RB.logging.logDatapackets("\n" + e.toString() + " packet error\t\n");
			}
			try 
			{
				RB.logging.logDatapackets("\n udpSocket will start to send \t\n");
				udpSocket.send(udpPacket);
				RB.logging.logDatapackets("\nsend succeed!\t\n");
				return new TriStatusImpl();
			}
			catch (Exception ex)
			{
				RB.logging.logDatapackets("\nioex"+ex.toString() + "send error\t\n");
				return new TriStatusImpl(ex.getMessage());
			}
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
		TriStatus mapStatus = CsaDef.triUnmap(compPortId, tsiPortId);

		if (mapStatus.getStatus() != TriStatus.TRI_OK)
		{ return mapStatus; }
	        if (tsiPortId.getPortName().equals("BGP_Interface_Port"))
		{// stop listening
			synchronized (threadlock)
			{
				if (!runThread)
				{ return new TriStatusImpl(); }
				runThread = false;
			}

			while (tcpReceiverThread.isAlive())
			{
				try
				{ sleep(500); }
				catch (InterruptedException ie) 
				{
					//quicknotepad.textArea.append("\n"+ie.toString());
					RB.logging.logDatapackets("\n"+ie.toString());
				}
			}
			// close the sender socket
			if (rxSocket != null)
			{
				try
				{
					rxSocket.close();
					rxSocket = null;
				}
				catch (IOException sio)
				{
					//quicknotepad.textArea.append("\n"+sio.toString());
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

	    /* 
	    private Object decodeEncodedParameter(byte[] parameter) throws RuntimeException
	    {
		return ((SyncCodec) getCodec("")).deserializeObject(parameter);
	    }
	
	    private byte[] encodeObject(Object returnValue) throws IOException
	    {
		return ((SyncCodec) getCodec("")).serializeObject(returnValue);
	    }
	
	    private Object[] parseInParameterList(TriParameterList params) throws RuntimeException
		//throws TciException
	    {
		// temporary storage
		Vector objectList = new Vector();
	
		// find IN/INOUT parameters and decode them
		for (int i = 0; i < params.size(); i++)
		{
		    TriParameter param = params.get(i);
	
		    if (param.getParameterPassingMode() == TriParameterPassingMode.TRI_OUT)
		    {
			continue;
		    }
	
		    objectList.addElement(decodeEncodedParameter(
			    param.getEncodedParameter()));
		}
	
		// Convert vector to array.
		Object[] returnList = new Object[objectList.size()];
	
		for (int i = objectList.size(); i-- > 0;)
		{
		    returnList[i] = objectList.get(i);
		}
	
		// return the in parameters as objects
		return returnList;
	    }
	    */
	    
	    byte[]  HandleNotification(byte[] buf)
	    {
		   // if (buf[18]==3&&buf.length>21)
		    {
			   byte[] temp = new byte[21];
			   for(int i=0;i<21;i++)
				   temp[i] = buf[i];
			   
			   return temp;
		    }
		    //else
			   // return buf;
	    }
	    
}// end public class BGP_STATETestAdapter
