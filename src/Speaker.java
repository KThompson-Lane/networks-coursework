import Security.SecurityLayer;
import java.io.IOException;
import java.net.*;
import uk.ac.uea.cmp.voip.*;


public class Speaker implements Runnable {
    private boolean running;
    private final int port;
    private final InetAddress destinationAddress;
    private DatagramSocket sendingSocket;
    private final VoipLayer voipLayer;
    private final SecurityLayer securityLayer;

    public Speaker(int portNum, InetAddress destAddress, long key, int socketNum, boolean interleaving, boolean compensate, boolean encrypt) {
        //  Set port number to argument
        this.port = portNum;
        //  Setup client IP from argument
        destinationAddress = destAddress;
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
        //  Set up VOIP layer
        voipLayer = new VoipLayer(false, interleaving, compensate);
        //  Set up Security layer
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
        //  First retrieve audio block from the VOIP layer including VOIP header info (512 + 4 bytes)
        byte[] voipPacket = voipLayer.getVoipBlock();

        //  SECURITY LAYER
        //  Then pass packet to SecurityLayer to encrypt/authenticate to produce our 520 byte (516 + 4 byte header) packet
        byte[] securePacket = securityLayer.EncryptAndSign(voipPacket);

        //  NETWORK/TRANSPORT LAYER
        //  Finally send the secure packet to the other client
        DatagramPacket packet = new DatagramPacket(securePacket, securePacket.length, destinationAddress, port);
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
