import CMPC3M06.AudioPlayer;
import Security.SecurityLayer;
import Security.UnableToAuthenticateException;
import uk.ac.uea.cmp.voip.*;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Listener implements Runnable {
    //  Calculated in Speaker.java
    static final int TOTAL_PACKET_SIZE = 520;
    private boolean running;
    private DatagramSocket receivingSocket;
    private final SecurityLayer securityLayer;
    private final VoipLayer voipLayer;

    public Listener(int portNum, long key, int socketNum, boolean decrypt) {

        //  Set up Receiving Socket
        try{
            // Set up Datagram Socket
            switch(socketNum)
            {
                case 1 :
                    this.receivingSocket = new DatagramSocket(portNum);
                    break;
                case 2 :
                    this.receivingSocket = new DatagramSocket2(portNum);
                    break;
                case 3 :
                    this.receivingSocket = new DatagramSocket3(portNum);
                    break;
                case 4 :
                    this.receivingSocket = new DatagramSocket4(portNum);
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

        //  Set up security layer
        securityLayer = new SecurityLayer(key, decrypt);
        //  Set up VOIP layer
        voipLayer = new VoipLayer(true);
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
        ByteBuffer packetBuffer = ByteBuffer.allocate(TOTAL_PACKET_SIZE);
        DatagramPacket packet = new DatagramPacket(packetBuffer.array(), 0, TOTAL_PACKET_SIZE);

        try {
            receivingSocket.receive(packet);
        } catch (SocketTimeoutException e) {
            //  Handle socket timeout
            return;
        } catch (IOException e){
            System.out.println("ERROR: Listener: Some random IO error occurred!");
            e.printStackTrace();
            return;
        }

        byte[] audio;
        //  Then pass packet to SecurityLayer to decrypt/authenticate
        try {
            audio = securityLayer.AuthAndDecrypt(packetBuffer.array());
        } catch (UnableToAuthenticateException e) {
            return;
        }

        //  Then process and play decrypted audio packet with the VOIP layer
        voipLayer.receiveFromSecurity(audio); //todo - rename
    }
    public void Terminate()
    {
        running = false;
    }
}
