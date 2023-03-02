package Security;

import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Impostor  implements Runnable{
    private final int port;
    private boolean running;
    private short packetCount;
    private InetAddress destinationAddress;
    private DatagramSocket sendingSocket;
    private final SecurityLayer securityLayer;

    public Impostor(int portNum, String destAddress, long key) {
        //  Set port number to argument
        this.port = portNum;
        //  Try and setup client IP from argument
        try {
            destinationAddress = InetAddress.getByName(destAddress);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: Impostor: Invalid destination address");
            e.printStackTrace();
            System.exit(0);
        }

        //  Try and create socket for sending from
        try{
            this.sendingSocket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: Impostor: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        //  Set up security layer
        securityLayer = new SecurityLayer(key, true);
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
        while(running)
        {
            //  Send payload in here
            Attack();
        }
        //  Close socket then terminates thread
        sendingSocket.close();
    }
    public void Attack()
    {
        //  First receive audio block from recorder
        //  Returns 32 ms (512 byte) audio blocks
        byte[] message = "Nobody expects the Spanish Inquisition!".getBytes();

        //  Then process audio block with the VOIP layer (i.e. numbering)
        ByteBuffer numberedPacket = ByteBuffer.allocate(message.length + 2);
        packetCount++;
        short packetNum = packetCount;
        numberedPacket.putShort(packetNum);

        //  Then pass packet to SecurityLayer to encrypt/authenticate
        message = securityLayer.EncryptAndSign(message);
        numberedPacket.put(message);

        //  Finally send the encrypted packet to the other client
        //  Make a DatagramPacket with client address and port number
        DatagramPacket packet = new DatagramPacket(numberedPacket.array(), message.length + 2, destinationAddress, port);
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
