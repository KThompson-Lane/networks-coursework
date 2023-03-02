import CMPC3M06.AudioRecorder;
import Security.SecurityLayer;
import Security.SimpleEncryption;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

public class Speaker implements Runnable {
    private static boolean encrypt = true;
    private boolean running;
    private final int port;
    private InetAddress destinationAddress;
    private DatagramSocket sendingSocket;
    private AudioRecorder recorder;
    private short packetCount;
    private final SecurityLayer securityLayer;

    private final VoipLayer voipLayer;
    
    public Speaker(int portNum, String destAddress, long key, int socketNum) {
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
            // Set up Datagram Socket
            switch(socketNum)
            {
                case 1 :
                    this.sendingSocket = new DatagramSocket();
                    break;
                case 2 :
                    this.sendingSocket = new DatagramSocket2();
                    break;
                case 3 :
                    this.sendingSocket = new DatagramSocket3();
                    break;
                case 4 :
                    this.sendingSocket = new DatagramSocket4();
                    break;
                default :
                    //todo - error
            }

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

        //  Set up VOIP layer
        voipLayer = new VoipLayer(key);

        //  Set up security layer
        securityLayer = new SecurityLayer(key, encrypt); //todo - move to voip
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

        // todo - move to voip
        //  Then process audio block with the VOIP layer (i.e. numbering)
        //ByteBuffer numberedPacket = ByteBuffer.allocate(514);
        //packetCount++;
        //short packetNum = packetCount;
        //numberedPacket.putShort(packetNum);



        //todo - move to voip
        //  Then pass packet to SecurityLayer to encrypt/authenticate
        //audioBlock = securityLayer.EncryptAndAuth(audioBlock);
        //numberedPacket.put(audioBlock);

        //todo - sort
        //  Finally send the encrypted packet to the other client
        //  Make a DatagramPacket with client address and port number

        //todo - should be the security layer sending the packet to the transport layer
        DatagramPacket packet = new DatagramPacket(voipLayer.process(audioBlock).array(), 514, destinationAddress, port);
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
