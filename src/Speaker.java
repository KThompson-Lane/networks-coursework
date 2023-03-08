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
    private static boolean encrypt = false;
    private boolean running;
    private final int port;
    private InetAddress destinationAddress;
    private DatagramSocket sendingSocket;
    private AudioRecorder recorder;
    private short packetCount;

    private final VoipLayer voipLayer;

    private final SecurityLayer securityLayer;
    
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
        //try{
        //    recorder = new AudioRecorder();
        //} catch (LineUnavailableException e) {
        //    System.out.println("ERROR: Speaker: Could not start audio recorder.");
        //    e.printStackTrace();
        //    System.exit(0);
        //}

        //  Set up VOIP layer
        voipLayer = new VoipLayer(false);
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
        // VOIP LAYER
        //voipLayer.receiveFromAudio(); //todo - rename
        //DatagramPacket packet = new DatagramPacket(voipLayer.getVoipBlock(), 514, destinationAddress, port);

        // SECURITY LAYER HERE
        DatagramPacket packet = new DatagramPacket (securityLayer.EncryptAndAuth(voipLayer.getVoipBlock()), 516, destinationAddress, port); //todo - comment this


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
