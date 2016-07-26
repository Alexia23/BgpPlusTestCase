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
public class BGP_TestAdapter_3TC extends TestAdapter implements TriCommunicationSA,TriPlatformPA, TciEncoding 
{
	private final int MSG_BUF_SIZE = 1600; // default data size
	
	private Object threadlock = new Object();
	private Thread tcpReceiverThread_pco1 = null;
	private boolean runThread_pco1 = false;
	private Socket rxSocket_pco1; 
	
	private Thread tcpReceiverThread_pco2 = null;
	private boolean runThread_pco2 = false;
	private Socket rxSocket_pco2; 
	
	private Thread tcpReceiverThread_pco3 = null;
	private boolean runThread_pco3 = false;
	private Socket rxSocket_pco3; 
	
	public BGP_TestAdapter_3TC()
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

		 if (encodingName.equals("BaseCodec"))
		 {// if with encoding is not set
			 codec = new BaseCodec(RB);
			 //codecs.put(encodingName, codec);
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
	 public void triCancel()	//when the testcase execute the "stop" statement, then this function done
	 {
	    
		     synchronized (threadlock)
		     {
			    if(runThread_pco1||runThread_pco2||runThread_pco3)
			    {
				     if(runThread_pco1)
					     {runThread_pco1 = false;}
				     if(runThread_pco2)
					     {runThread_pco2 = false;}
				     if(runThread_pco3)
					     {runThread_pco3 = false;}
			    }
			    else
				    return;
		     }
		     
		     if(tcpReceiverThread_pco1!=null)
		     {
			     while (tcpReceiverThread_pco1.isAlive())
			     {
				     try
				     { sleep(10); }
				     catch (InterruptedException ie) 
				     {
					     RB.logging.logDatapackets("\ntriCanceltcpReceiverThread_pco1"+ie.toString()); 
				     }
			     }
		
			     if (rxSocket_pco1 != null)
			     {
				     try 
				     {
					     rxSocket_pco1.close();
					     rxSocket_pco1 = null;
				     }
				     catch (IOException sio)
				     {
					     RB.logging.logDatapackets("\n"+sio.toString());
				     }
			     }
		     }				     
	     
	    	    if(tcpReceiverThread_pco2!=null)
		    {
			    while (tcpReceiverThread_pco2.isAlive())
			     {
				     try
				     { sleep(10); }
				     catch (InterruptedException ie) 
				     {
					     RB.logging.logDatapackets("\ntcpReceiverThread_pco2"+ie.toString()); 
				     }
			     }
		
			      if (rxSocket_pco2 != null)
			     {
				    try 
				     {
					     rxSocket_pco2.close();
					     rxSocket_pco2 = null;
				     }
				     catch (IOException sio)
				     {
					     RB.logging.logDatapackets("\n"+sio.toString());
				     }
			     }
		    }		    
	     
		    if(tcpReceiverThread_pco3!=null)
		    {
			     while (tcpReceiverThread_pco3.isAlive())
			     {
				     try
				     { sleep(10); }
				     catch (InterruptedException ie) 
				     {
					     RB.logging.logDatapackets("\ntcpReceiverThread_pco3"+ie.toString()); 
				     }
			     }
		
			     if (rxSocket_pco3 != null)
			     {
				     try 
				     {
					     rxSocket_pco3.close();
					     rxSocket_pco3 = null;
				     }
				     catch (IOException sio)
				     {
					     RB.logging.logDatapackets("\n"+sio.toString());
				     }
			     }
		    }
	 } //end public void triCancel()

   
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
		//Do Some Pre_work;
		TriStatus mapStatus = CsaDef.triMap(compPortId, tsiPortId);        
        
		if (mapStatus.getStatus() != TriStatus.TRI_OK)
		{ return mapStatus; }

		if (tsiPortId.getPortName().equals("BGP_Interface_Port"))
		{
			RB.logging.logDatapackets("\n Current port:"+tsiPortId.getPortName()); 
			// prepare to be ready to communicate           
			try
			{
				InetAddress	router_addr = InetAddress.getByName("202.101.1.1");
				int	router_port = 179;
				//InetAddress	host1_addr = InetAddress.getByName("192.168.1.195");
				//int	host1_port = 11111;
				//rxSocket_pco1 = new Socket(router_addr,router_port,host1_addr,host1_port);
				rxSocket_pco1 = new Socket(router_addr,router_port);
				runThread_pco1 = true;
				tcpReceiverThread_pco1 = new Thread()
				{
					public void run() 
					{
						boolean mylock = runThread_pco1;
						try 
						{
							rxSocket_pco1.setSoTimeout(1000);			  
							while (mylock) 
							{
								byte[] msg_rec = new byte[MSG_BUF_SIZE];
								try 
								{
									int length = rxSocket_pco1.getInputStream().read(msg_rec);
									if(length == -1)
									{
										throw new Exception("TCP connection finished.");
									}							
									if (length > 0) 
									{
										byte[] Con = new byte[length];
										for (int i=0;i<length;i++) 
											Con[i]=msg_rec[i];
										
										//print the receive message
										RB.logging.logDatapackets("\n in the BGP_Interface_Port,Receiving  Length= "+Con.length+"bytes Packet :");
										for (int j =0;j<Con.length;j++)
										{
											if(j%16==0)
												RB.logging.logDatapackets("\n");
											StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((Con[j]+256)%256)));
											if(hexvalue.length()==1)
												hexvalue=hexvalue.insert(0,"0");
											RB.logging.logDatapackets(hexvalue+" ");
										}
										
										/*handle the notificiation frame*/
										if(Con[18]==3&&Con.length>21)
										{
											Con = HandleNotification(Con);
											RB.logging.logDatapackets("\n"+Con.length+"bytes Packet :");
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
										
										RB.logging.logDatapackets("\n In the BGP_Interface_Port, the Received Thread:received message length:"+ rcvMessage.getEncodedMessage().length);
										synchronized (threadlock) 
										{
											if (runThread_pco1) 
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
									if (rxSocket_pco1 != null) 
									{
										rxSocket_pco1.close();
										rxSocket_pco1 = null;
									}
									return;
								}
								catch (Exception ex) 
								{
									RB.logging.logDatapackets("\n"+ex.toString());
									if (rxSocket_pco1 != null) 
									{
										rxSocket_pco1.close();
										rxSocket_pco1 = null;
									}
									return;
								}
								synchronized (threadlock) 
								{
									mylock = runThread_pco1;
								}
							}
							
							/*
							if (rxSocket_pco1 != null) 
							{
								rxSocket_pco1.close();
								rxSocket_pco1 = null;
							}*/
						}
						catch (IOException se) 
						{
							RB.logging.logDatapackets("\n"+"MyTestAdapter: the BGP_Interface_Port: SocketException "+se.toString());
						}
					}
				};
				tcpReceiverThread_pco1.start(); 
				return new TriStatusImpl();
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
		}//end if (tsiPortId.getPortName().equals("BGP_Interface_Port"))
		
		if(tsiPortId.getPortName().equals("BGP_Interface_Port2"))
		{
			RB.logging.logDatapackets2("\n Current port:"+tsiPortId.getPortName()); 
			// prepare to be ready to communicate           
			try
			{
				InetAddress	router2_addr = InetAddress.getByName("202.101.3.1");
				int	router2_port = 179;
				//InetAddress	host2_addr = InetAddress.getByName("192.168.2.117");
				//int	host2_port = 11111;
				//rxSocket_pco2 = new Socket(router_addr,router_port,host2_addr,host2_port);
				rxSocket_pco2 = new Socket(router2_addr,router2_port);
				
				runThread_pco2 = true;
				tcpReceiverThread_pco2 = new Thread()
				{
					public void run() 
					{
						boolean mylock2 = runThread_pco2;
						try 
						{
							rxSocket_pco2.setSoTimeout(1000);			  
							while (mylock2) 
							{
								byte[] msg2_rec = new byte[MSG_BUF_SIZE];
								try 
								{
									int length = rxSocket_pco2.getInputStream().read(msg2_rec);
									if(length == -1)
									{
										throw new Exception("TCP connection finished.");
									}							
									if (length > 0) 
									{
										byte[] Con2 = new byte[length];
										for (int i=0;i<length;i++) 
											Con2[i]=msg2_rec[i];
										
										//print the receive message
										RB.logging.logDatapackets2("\n In the BGP_Interface_Port2: Receiving  Length= "+Con2.length+"bytes Packet :");
										for (int j =0;j<Con2.length;j++)
										{
											if(j%16==0)
												RB.logging.logDatapackets2("\n");
											StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((Con2[j]+256)%256)));
											if(hexvalue.length()==1)
												hexvalue=hexvalue.insert(0,"0");
											RB.logging.logDatapackets2(hexvalue+" ");
										}
										
										/*handle the notificiation frame*/
										if(Con2[18]==3&&Con2.length>21)
										{
											Con2 = HandleNotification(Con2);
											RB.logging.logDatapackets2("\nAfter HandleNotification in the BGP_Interface_Port2, the frame: "+Con2.length+"bytes Packet :");
											for (int j =0;j<Con2.length;j++)
											{
												if(j%16==0)
													RB.logging.logDatapackets2("\n");
												StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((Con2[j]+256)%256)));
												if(hexvalue.length()==1)
													hexvalue=hexvalue.insert(0,"0");
												RB.logging.logDatapackets2(hexvalue+" ");
											}
										}																				
										//end print the receive message										
                              
										TriMessage rcvMessage = new TriMessageImpl(Con2);
										
										RB.logging.logDatapackets2("\n In the BGP_Interface_Port2,Received Thread: received message length:"+ rcvMessage.getEncodedMessage().length);
										synchronized (threadlock) 
										{
											if (runThread_pco2) 
											{
												Cte.triEnqueueMsg(tsiPortId, null, compPortId.getComponent(),rcvMessage);
											}
										}
									}
								}
								catch (InterruptedIOException iioex) 
								{
									RB.logging.logDatapackets2("\n"+iioex.toString());
								}
								catch (IOException ioex) 
								{
									RB.logging.logDatapackets2("\n"+ioex.toString());
									if (rxSocket_pco2 != null) 
									{
										rxSocket_pco2.close();
										rxSocket_pco2 = null;
									}
									return;
								}
								catch (Exception ex) 
								{
									RB.logging.logDatapackets2("\n"+ex.toString());
									if (rxSocket_pco2 != null) 
									{
										rxSocket_pco2.close();
										rxSocket_pco2 = null;
									}
									return;
								}
								synchronized (threadlock) 
								{
									mylock2 = runThread_pco2;
								}
							}
							/*
							if (rxSocket_pco2 != null) 
							{
								rxSocket_pco2.close();
								rxSocket_pco2 = null;
							}*/
						}
						catch (IOException se) 
						{
							RB.logging.logDatapackets2("\n"+"MyTestAdapter: the BGP_Interface_Port2: SocketException "+se.toString());
						}
					}
				};
				tcpReceiverThread_pco2.start(); 
				return new TriStatusImpl();
			}
			catch (SocketException sex)
			{
				RB.logging.logDatapackets2("\n"+sex.toString());
				return new TriStatusImpl("Unable to open socket for TSI Port: " +tsiPortId.getPortName());
			}
			catch (IOException sio)
			{
				RB.logging.logDatapackets2("\n"+sio.toString());	
			}   
		}//end if(tsiPortId.getPortName().equals("BGP_Interface_Port2"))
		
		if(tsiPortId.getPortName().equals("BGP_Interface_Port3"))
		{
			RB.logging.logDatapackets3("\n Current port:"+tsiPortId.getPortName()); 
			// prepare to be ready to communicate           
			try
			{
				InetAddress	router3_addr = InetAddress.getByName("121.101.5.1");
				int	router3_port = 179;
				
				rxSocket_pco3 = new Socket(router3_addr,router3_port);
				
				runThread_pco3 = true;
				tcpReceiverThread_pco3 = new Thread()
				{
					public void run() 
					{
						boolean mylock3 = runThread_pco3;
						try 
						{
							rxSocket_pco3.setSoTimeout(1000);			  
							while (mylock3) 
							{
								byte[] msg3_rec = new byte[MSG_BUF_SIZE];
								try 
								{
									int length = rxSocket_pco3.getInputStream().read(msg3_rec);
									if(length == -1)
									{
										throw new Exception("TCP connection finished.");
									}							
									if (length > 0) 
									{
										byte[] Con3 = new byte[length];
										for (int i=0;i<length;i++) 
											Con3[i]=msg3_rec[i];
										
										//print the receive message
										RB.logging.logDatapackets3("\n In the BGP_Interface_Port3: Receiving  Length= "+Con3.length+"bytes Packet :");
										for (int j =0;j<Con3.length;j++)
										{
											if(j%16==0)
												RB.logging.logDatapackets3("\n");
											StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((Con3[j]+256)%256)));
											if(hexvalue.length()==1)
												hexvalue=hexvalue.insert(0,"0");
											RB.logging.logDatapackets3(hexvalue+" ");
										}
										
										/*handle the notificiation frame*/
										if(Con3[18]==3&&Con3.length>21)
										{
											Con3 = HandleNotification(Con3);
											RB.logging.logDatapackets3("\nAfter HandleNotification in the BGP_Interface_Port3, the frame: "+Con3.length+"bytes Packet :");
											for (int j =0;j<Con3.length;j++)
											{
												if(j%16==0)
													RB.logging.logDatapackets3("\n");
												StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((Con3[j]+256)%256)));
												if(hexvalue.length()==1)
													hexvalue=hexvalue.insert(0,"0");
												RB.logging.logDatapackets3(hexvalue+" ");
											}
										}																				
										//end print the receive message										
                              
										TriMessage rcvMessage = new TriMessageImpl(Con3);
										
										RB.logging.logDatapackets3("\n In the BGP_Interface_Port3,Received Thread: received message length:"+ rcvMessage.getEncodedMessage().length);
										synchronized (threadlock) 
										{
											if (runThread_pco3) 
											{
												Cte.triEnqueueMsg(tsiPortId, null, compPortId.getComponent(),rcvMessage);
											}
										}
									}
								}
								catch (InterruptedIOException iioex) 
								{
									RB.logging.logDatapackets3("\n"+iioex.toString());
								}
								catch (IOException ioex) 
								{
									RB.logging.logDatapackets3("\n"+ioex.toString());
									if (rxSocket_pco3 != null) 
									{
										rxSocket_pco3.close();
										rxSocket_pco3 = null;
									}
									return;
								}
								catch (Exception ex) 
								{
									RB.logging.logDatapackets3("\n"+ex.toString());
									if (rxSocket_pco3 != null) 
									{
										rxSocket_pco3.close();
										rxSocket_pco3 = null;
									}
									return;
								}
								synchronized (threadlock) 
								{
									mylock3 = runThread_pco3;
								}
							}
							/*
							if (rxSocket_pco3 != null) 
							{
								rxSocket_pco3.close();
								rxSocket_pco3 = null;
							}*/
						}
						catch (IOException se) 
						{
							RB.logging.logDatapackets3("\n"+"MyTestAdapter: the BGP_Interface_Port3: SocketException "+se.toString());
						}
					}
				};
				tcpReceiverThread_pco3.start(); 
				return new TriStatusImpl();
			}
			catch (SocketException sex)
			{
				RB.logging.logDatapackets3("\n"+sex.toString());
				return new TriStatusImpl("Unable to open socket for TSI Port: " +tsiPortId.getPortName());
			}
			catch (IOException sio)
			{
				RB.logging.logDatapackets3("\n"+sio.toString());	
			}   
		}//end if(tsiPortId.getPortName().equals("BGP_Interface_Port3"))
			
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
		RB.logging.logDatapackets("\nBegin the triSend():");
		
		if (tsiPortId.getPortName().equals("BGP_Interface_Port"))
		{				
			try
			{
				byte[] msg_send = sendMessage.getEncodedMessage();
				rxSocket_pco1.getOutputStream().write(msg_send);
				
				//print the send packet
				RB.logging.logDatapackets("\n In the BGP_Interface_Port: Sending  Length= "+msg_send.length+"bytes Packet :");
				for (int j =0;j<msg_send.length;j++)
				{
					if(j%16==0)
						RB.logging.logDatapackets("\n");
					StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((msg_send[j]+256)%256)));
					if(hexvalue.length()==1)
						{ hexvalue=hexvalue.insert(0,"0"); }
					RB.logging.logDatapackets(hexvalue+" ");
				} //print the packet of sending
				return new TriStatusImpl();
			}
			catch (Exception ioex)
			{
				RB.logging.logDatapackets("\n"+ioex.toString());
				return new TriStatusImpl(ioex.getMessage());
			}
		}//end if (tsiPortId.getPortName().equals("BGP_Interface_Port"))
		
		if (tsiPortId.getPortName().equals("BGP_Interface_Port2"))
		{				
			try
			{
				byte[] msg_send = sendMessage.getEncodedMessage();
				rxSocket_pco2.getOutputStream().write(msg_send);
				
				//print the send packet
				RB.logging.logDatapackets2("\n In the BGP_Interface_Port2: Sending  Length= "+msg_send.length+"bytes Packet :");
				for (int j =0;j<msg_send.length;j++)
				{
					if(j%16==0)
						RB.logging.logDatapackets2("\n");
					StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((msg_send[j]+256)%256)));
					if(hexvalue.length()==1)
						{ hexvalue=hexvalue.insert(0,"0"); }
					RB.logging.logDatapackets2(hexvalue+" ");
				} //print the packet of sending
				return new TriStatusImpl();
			}
			catch (Exception ioex)
			{
				RB.logging.logDatapackets2("\n"+ioex.toString());
				return new TriStatusImpl(ioex.getMessage());
			}
		}//end if (tsiPortId.getPortName().equals("BGP_Interface_Port2"))
		
		if (tsiPortId.getPortName().equals("BGP_Interface_Port3"))
		{				
			try
			{
				byte[] msg_send = sendMessage.getEncodedMessage();
				rxSocket_pco3.getOutputStream().write(msg_send);
				
				//print the send packet
				RB.logging.logDatapackets3("\n In the BGP_Interface_Port3: Sending  Length= "+msg_send.length+"bytes Packet :");
				for (int j =0;j<msg_send.length;j++)
				{
					if(j%16==0)
						RB.logging.logDatapackets3("\n");
					StringBuffer hexvalue=new StringBuffer(Integer.toHexString((int)((msg_send[j]+256)%256)));
					if(hexvalue.length()==1)
						{ hexvalue=hexvalue.insert(0,"0"); }
					RB.logging.logDatapackets3(hexvalue+" ");
				} //print the packet of sending
				return new TriStatusImpl();
			}
			catch (Exception ioex)
			{
				RB.logging.logDatapackets3("\n"+ioex.toString());
				return new TriStatusImpl(ioex.getMessage());
			}
		}//end if (tsiPortId.getPortName().equals("BGP_Interface_Port3"))
		
		else
		{
			return new TriStatusImpl("triSend: (from: " + componentId +", to: " + tsiPortId + ") not implemented");
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
				if (!runThread_pco1)
				{ return new TriStatusImpl(); }
				runThread_pco1 = false;
			}

			while (tcpReceiverThread_pco1.isAlive())
			{
				try
				{ sleep(10); }
				catch (InterruptedException ie) 
				{
					RB.logging.logDatapackets("\ntcpReceiverThread_pco1"+ie.toString());
				}
			}
			// close the sender socket
			if (rxSocket_pco1 != null)
			{
				try
				{
					rxSocket_pco1.close();
					rxSocket_pco1 = null;
				}
				catch (IOException sio)
				{
					RB.logging.logDatapackets("\n"+sio.toString());
				}
			}
			return new TriStatusImpl();
		}//end  if (tsiPortId.getPortName().equals("BGP_Interface_Port"))
		
		if (tsiPortId.getPortName().equals("BGP_Interface_Port2"))
		{// stop listening
			synchronized (threadlock)
			{
				if (!runThread_pco2)
				{ return new TriStatusImpl(); }
				runThread_pco2 = false;
			}

			while (tcpReceiverThread_pco2.isAlive())
			{
				try
				{ sleep(10); }
				catch (InterruptedException ie) 
				{
					RB.logging.logDatapackets("\ntcpReceiverThread_pco2"+ie.toString());
				}
			}
			// close the sender socket
			if (rxSocket_pco2 != null)
			{
				try
				{
					rxSocket_pco2.close();
					rxSocket_pco2 = null;
				}
				catch (IOException sio)
				{
					RB.logging.logDatapackets("\n"+sio.toString());
				}
			}
			return new TriStatusImpl();
		}//end  if (tsiPortId.getPortName().equals("BGP_Interface_Port2"))
		
		if (tsiPortId.getPortName().equals("BGP_Interface_Port3"))
		{// stop listening
			synchronized (threadlock)
			{
				if (!runThread_pco3)
				{ return new TriStatusImpl(); }
				runThread_pco3 = false;
			}

			while (tcpReceiverThread_pco3.isAlive())
			{
				try
				{ sleep(10); }
				catch (InterruptedException ie) 
				{
					RB.logging.logDatapackets("\ntcpReceiverThread_pco3"+ie.toString());
				}
			}
			// close the sender socket
			if (rxSocket_pco3 != null)
			{
				try
				{
					rxSocket_pco3.close();
					rxSocket_pco3 = null;
				}
				catch (IOException sio)
				{
					RB.logging.logDatapackets("\n"+sio.toString());
				}
			}
			return new TriStatusImpl();
		}//end  if (tsiPortId.getPortName().equals("BGP_Interface_Port3"))
		
		else
		{
			// Indicates an error. Attention: NOT TRI CONFORM !	
			// TRI Conformant would be return new TriStatusImpl(TRI_ERROR); // no description
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
		   byte[] temp = new byte[21];
		   for(int i=0;i<21;i++)
			   temp[i] = buf[i];
		   return temp;
	    }
	    
}// end public class BGP_STATETestAdapter
