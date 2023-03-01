import CMPC3M06.AudioRecorder;
import Security.SecurityLayer;
import Security.SimpleEncryption;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Speaker implements Runnable {
    private static boolean encrypt = true;
    private boolean running;
    private final int port;
    private InetAddress destinationAddress;
    private DatagramSocket sendingSocket;
    private AudioRecorder recorder;
    private short packetCount;
    private final SecurityLayer securityLayer;
    
    public Speaker(int portNum, String destAddress, long key) {
        //  Set port number to argument
        this.port = portNum;
        packetCount = 0;
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
            this.sendingSocket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: Speaker: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        //  Try and create recorder
        try{
            recorder = new AudioRecorder();
        } catch (LineUnavailableException e) {
            System.out.println("ERROR: Speaker: Could not start audio recorder.");
            e.printStackTrace();
            System.exit(0);
        }
        //  Set up security layer
        securityLayer = new SecurityLayer(key, encrypt);
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
            TransmitPayload();
        }
        //  Close socket then terminates thread
        sendingSocket.close();
    }
    public void TransmitPayload()
    {
        //  First receive audio block from recorder
        //  Returns 32 ms (512 byte) audio blocks
        byte[] audioBlock = null;
        try {
            audioBlock = recorder.getBlock();
        } catch (IOException e) {
            System.out.println("ERROR: Speaker: Some random IO error occurred!");
            e.printStackTrace();
            return;
        }

        //  Then process audio block with the VOIP layer (i.e. numbering)
        ByteBuffer numberedPacket = ByteBuffer.allocate(514);
        packetCount++;
        short packetNum = packetCount;
        numberedPacket.putShort(packetNum);
        numberedPacket.put(audioBlock);

        //  Then pass packet to SecurityLayer to encrypt/authenticate
        audioBlock = securityLayer.EncryptAndAuth(audioBlock);

        //  Finally send the encrypted packet to the other client
        //  Make a DatagramPacket with client address and port number
        DatagramPacket packet = new DatagramPacket(numberedPacket.array(), 514, destinationAddress, port);
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
