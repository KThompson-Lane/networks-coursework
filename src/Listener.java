import CMPC3M06.AudioPlayer;
import Security.SecurityLayer;
import Security.UnableToAuthenticateException;

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
    List<Integer> packetNums;

    public Listener(int portNum, long key) {
        port = portNum;
        packetNums = new ArrayList<>();

        //  Set up Receiving Socket

        try{
            this.receivingSocket = new DatagramSocket(port);
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
        ByteBuffer packetBuffer = ByteBuffer.allocate(518);
        DatagramPacket packet = new DatagramPacket(packetBuffer.array(), 0, 518);

        byte[] audio = new byte[516];

        try {
            receivingSocket.receive(packet);
            int packetNum = packetBuffer.getShort();
            packetNums.add(packetNum); //todo - sort this
            //System.out.println("Packet Received: " + packetNum);

            packetBuffer.get(audio);
        } catch (SocketTimeoutException e) {
            //  Handle socket timeout
        } catch (IOException e){
            System.out.println("ERROR: Listener: Some random IO error occurred!");
            e.printStackTrace();
            return;
        }

        //  Then pass packet to SecurityLayer to decrypt/authenticate
        byte[] test;
        try{
            test = securityLayer.AuthAndDecrypt(audio);
        }catch (UnableToAuthenticateException e)
        {
            //TODO: Do something when we receive an unauthentic packet
            return;
        }

        //  Then process decrypted audio packet with the VOIP layer

        //  Finally output the processed audio block to the speaker
        try {
            player.playBlock(test);
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
