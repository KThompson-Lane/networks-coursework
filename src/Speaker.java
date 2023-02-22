import java.net.*;

public class Speaker implements Runnable {
    private boolean running;
    protected int port;
    protected InetAddress destinationAddress;
    protected DatagramSocket sendingSocket;

    public Speaker(int portNum, String destAddress) {
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
            this.sendingSocket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: Speaker: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
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
        //  Then process audio block with the VOIP layer (i.e. numbering)
        //  Then pass packet to SecurityLayer to encrypt/authenticate
        //  Finally send the encrypted packet to the other client
    }
    public void Terminate()
    {
        running = false;
    }
}
