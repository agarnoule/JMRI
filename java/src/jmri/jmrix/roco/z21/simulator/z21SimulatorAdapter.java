// z21SimulatorAdapter.java
package jmri.jmrix.roco.z21.simulator;

import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.roco.z21.z21Adapter;
import jmri.jmrix.roco.z21.z21Constants;
import jmri.jmrix.roco.z21.z21Message;
import jmri.jmrix.roco.z21.z21Reply;
import jmri.jmrix.roco.z21.z21TrafficController;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * Provide access to a simulated z21 system.
 *
 * Currently, the z21Simulator reacts to commands sent from the user interface
 * with messages an appropriate reply message.
 *
 * NOTE: Some material in this file was modified from other portions of the
 * support infrastructure.
 *
 * @author	Paul Bender, Copyright (C) 2015 
 * @version	$Revision$
 */
public class z21SimulatorAdapter extends z21Adapter implements Runnable {

    private Thread sourceThread;
    private z21XNetSimulatorAdapter xnetadapter = null;

    public z21SimulatorAdapter() {
        super();
        // start a UDP server that we can connect to.  The server will
        // produce the appropriate responses.
        xnetadapter = new z21XNetSimulatorAdapter();
    }

    /**
     * set up all of the other objects to operate with a z21Simulator connected
     * to this port
     */
    public void configure() {
        // connect to a packetizing traffic controller
        z21TrafficController packets = new z21TrafficController();
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setTrafficController(packets);

        sourceThread = new Thread(this);
        sourceThread.start();

        this.getSystemConnectionMemo().configureManagers();

        jmri.jmrix.lenz.ActiveFlag.setActive();
    }

    @Override
    public void connect() throws Exception {
       setHostAddress("localhost"); // always localhost for the simulation.
       super.connect();
    }

    public void run(){
     // The server just opens a DatagramSocket using the specified port number,
     // and then goes into an infinite loop.

     // try connecting to the server
     DatagramSocket s = null;
     try {
         s = new DatagramSocket(COMMUNICATION_UDP_PORT);
     } catch (Exception ex0 ) {
       log.error("Exception opening socket");
     }
     while(true){
       // the server waits for a client to connect, then echos the data sent back.
       byte[] input=new byte[100]; // input from network
       try {

         // to receive the data, we create a packet.
         DatagramPacket receivePacket = new DatagramPacket(input,100);
         // and wait for the data to arrive.
         s.receive(receivePacket);

         z21Message msg = new z21Message(receivePacket.getLength());
         for(int i=0;i< receivePacket.getLength();i++)
            msg.setElement(i,receivePacket.getData()[i]);

         // to echo the data back, we need to find the IP and port to send
         // the data to.
         InetAddress IPAddress = receivePacket.getAddress();
         int port = receivePacket.getPort();

         log.debug("Received packet: {}, message: {}",receivePacket.getData(),msg);

         // and then we create the return packet.
         z21Reply reply = generateReply(msg);
         byte ba[] = jmri.util.StringUtil.bytesFromHexString(reply.toString());
         DatagramPacket sendPacket = new DatagramPacket(ba,ba.length,IPAddress,port);

         // and send it back using our socket
         s.send(sendPacket);

       } catch (Exception ex3) {
         log.debug("IO Exception" );
         ex3.printStackTrace();
       }
       log.debug("Client Disconnect");
     }
    } // end of run.

    // generateReply is the heart of the simulation.  It translates an
    // incoming XNetMessage into an outgoing XNetReply.
    @SuppressWarnings("fallthrough")
    private z21Reply generateReply(z21Message m) {
        z21Reply reply;
        switch (m.getOpCode()) {
             case 0x0010:
                // request for serial number
                reply = getZ21SerialNumberReply();
                break;
             case 0x001a:
             // request for hardware version info.
                reply=getHardwareVersionReply();
                break;
             case 0x0040:
                // XPressNet tunnel message.
                XNetMessage xnm = getXNetMessage(m);
                XNetReply xnr=xnetadapter.generateReply(xnm);
                reply = getZ21ReplyFromXNet(xnr);
                break;
             case 0x0050:
                // set broadcast flags
             case 0x0051:
                // get broadcast flags
             case 0x0060:
                // get loco mode
             case 0x0061:
                // set loco mode
             case 0x0070:
                // get turnout mode
             case 0x0071:
                // set turnout mode
             case 0x0081:
                // get RMBus data
             case 0x0082:
                // program RMBus module
             case 0x0085:
                // get system state
             case 0x0089:
                  // Get Railcom Data
             case 0x00A2:
                  // loconet data from lan
             case 0x00A3:
                  // loconet dispatch address
             case 0x00A4:
                  // get loconet detector status
             case 0x0030:
                // LAN LOGOFF
                // no response expected, so just fall through to default.
             default:
                reply=getXPressNetUnknownCommandReply();
        }
        return reply;
    }

    // canned reply messages;
    private z21Reply getHardwareVersionReply(){
        z21Reply reply = new z21Reply();
        reply.setLength(0x000c);
        reply.setOpCode(0x001a);
        reply.setElement(4,0x00);
        reply.setElement(5,0x02);
        reply.setElement(6,0x00);
        reply.setElement(7,0x00);
        reply.setElement(8,0x20);
        reply.setElement(9,0x01);
        reply.setElement(10,0x00);
        reply.setElement(11,0x00);
        return reply;
    }

    private z21Reply getXPressNetUnknownCommandReply(){
        z21Reply reply = new z21Reply();
        reply.setLength(0x0007);
        reply.setOpCode(0x0040);
        reply.setElement(4,0x61);
        reply.setElement(5,0x82);
        reply.setElement(6,0xE3);
        return reply;
    }

    private z21Reply getZ21SerialNumberReply(){
        z21Reply reply = new z21Reply();
        reply.setLength(0x0004);
        reply.setOpCode(0x0010);
        reply.setElement(4,0x00);
        reply.setElement(5,0x00);
        reply.setElement(6,0x00);
        reply.setElement(7,0x00);
        return reply;
    }

    // utility functions
    private XNetMessage getXNetMessage(z21Message m) {
        if(m==null) throw new java.lang.IllegalArgumentException();
        XNetMessage xnm = new XNetMessage(m.getLength()-4);
        for (int i = 4; i < m.getLength(); i++) {
           xnm.setElement(i - 4, m.getElement(i));
        }
        return xnm;
    }

    private z21Reply getZ21ReplyFromXNet(XNetReply m) {
        if(m==null) throw new java.lang.IllegalArgumentException();
        z21Reply r=new z21Reply();
        r.setLength(m.getNumDataElements() + 4);
        r.setOpCode(0x0040);
        for (int i = 0; i < m.getNumDataElements(); i++) {
            r.setElement(i + 4, m.getElement(i));
        }
        return(r);
    }


    static Logger log = LoggerFactory.getLogger(z21SimulatorAdapter.class.getName());

}