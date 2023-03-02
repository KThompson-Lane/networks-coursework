import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;


//send dummy packets
//measure packet loss rate and burst lengths
//analyse statistics

public class ChannelAnalysis implements Runnable { //todo - rename if continuing to use - AnalysisSpeaker

    private boolean running;
    private final int port;
    private InetAddress destinationAddress;
    private DatagramSocket3 sendingSocket;

    private short packetCount = 0;

    public ChannelAnalysis(int portNum, String destAddress) {
        //  Set port number to argument
        this.port = portNum;

        //  Try and setup client IP from argument
        try {
            destinationAddress = InetAddress.getByName(destAddress);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: Speaker: Invalid destination address");
            e.printStackTrace();
            System.exit(0);
        }

        //  Try and create socket for sending from
        try{
            this.sendingSocket = new DatagramSocket3();
        } catch (SocketException e){
            System.out.println("ERROR: Speaker: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        // REMOVED - Create recorder
    }

    public void Start()
    {
        //Make a new thread from this class
        Thread thread = new Thread(this);
        //Start the thread
        running = true;
        thread.start();
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++)
        {
            TransmitPayload();
        }

        //  Close socket then terminates thread
        sendingSocket.close();
    }
    public void TransmitPayload()
    {
        //  First set up empty dummy block
        byte[] dummyBlock = new byte[512];

        ByteBuffer dummyPacket = ByteBuffer.allocate(514);

        packetCount++;
        short packetNum = packetCount;

        dummyPacket.putShort(packetNum);
        dummyPacket.put(dummyBlock);
        //  Make a DatagramPacket with client address and port number
        DatagramPacket packet = new DatagramPacket(dummyPacket.array(), 514, destinationAddress, port);
        //Send it
        try {
            sendingSocket.send(packet);
        } catch (IOException e) {
            System.out.println("ERROR: Speaker: Some random IO error occurred!");
            e.printStackTrace();
        }
    }
    public void Terminate()
    {
        running = false;
    }
}
