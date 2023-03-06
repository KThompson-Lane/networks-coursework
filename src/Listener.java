import CMPC3M06.AudioPlayer;
import Security.SecurityLayer;
import Security.SimpleEncryption;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Listener implements Runnable {
    private static boolean decrypt = true;
    private final int port;
    private boolean running;
    private DatagramSocket receivingSocket;
    private AudioPlayer player;
    private final SecurityLayer securityLayer;
    private final VoipLayer voipLayer; //todo - move to security
    
    public Listener(int portNum, long key, int socketNum) {
        port = portNum;

        //  Set up Receiving Socket
        try{
            // Set up Datagram Socket
            switch(socketNum)
            {
                case 1 :
                    this.receivingSocket = new DatagramSocket(port);
                    break;
                case 2 :
                    this.receivingSocket = new DatagramSocket2(port);
                    break;
                case 3 :
                    this.receivingSocket = new DatagramSocket3(port);
                    break;
                case 4 :
                    this.receivingSocket = new DatagramSocket4(port);
                    break;
                default:
                    //todo - error
            }

            //TODO: Investigate what timeout we should have
            receivingSocket.setSoTimeout(1000);
        } catch (SocketException e){
            System.out.println("ERROR: Listener: Could not open UDP socket to listen on.");
            e.printStackTrace();
            System.exit(0);
        }
        //  Set up audio player
        try{
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            System.out.println("ERROR: Listener: Could not start audio player.");
            e.printStackTrace();
            System.exit(0);
        }
        //  Set up security layer
        securityLayer = new SecurityLayer(key, decrypt);
        voipLayer = new VoipLayer(); //todo - move to security
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
            //  Receive payload in here
            ReceivePayload();
        }
        //  Close socket then terminates thread
        receivingSocket.close();
    }
    public void ReceivePayload()
    {
        //  First receive packet on UDP socket
        ByteBuffer packetBuffer = ByteBuffer.allocate(514);
        DatagramPacket packet = new DatagramPacket(packetBuffer.array(), 0, 514);

        byte[] audio = new byte[512];

        try {
            receivingSocket.receive(packet);
            // todo - send to security layer
            //todo - receive from voip layer
        } catch (SocketTimeoutException e) {
            //  Handle socket timeout
        } catch (IOException e){
            System.out.println("ERROR: Listener: Some random IO error occurred!");
            e.printStackTrace();
            return;
        }

        //  Then pass packet to SecurityLayer to decrypt/authenticate
        audio = voipLayer.receiveFromSecurity(packetBuffer);
        audio = securityLayer.DecryptAndAuth(audio); //todo - move before voip

        //  Then process decrypted audio packet with the VOIP layer

        //  Finally output the processed audio block to the speaker
        try {
            player.playBlock(audio);
        } catch (IOException e) {
            System.out.println("ERROR: Listener: Some random IO error occurred!");
            e.printStackTrace();
        }
    }
    public void Terminate()
    {
        running = false;
    }
}
