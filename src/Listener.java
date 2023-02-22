import java.net.*;

public class Listener implements Runnable {
    private final int port;
    private boolean running;
    private DatagramSocket receivingSocket;

    public Listener(int portNum) {
        port = portNum;
        //Set up Receiving Socket
        try{
            this.receivingSocket = new DatagramSocket(port);
            //TODO: Investigate what timeout we should have
            receivingSocket.setSoTimeout(1000);
        } catch (SocketException e){
            System.out.println("ERROR: Listener: Could not open UDP socket to listen on.");
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
            //  Receive payload in here
            ReceivePayload();
        }
        //  Close socket then terminates thread
        receivingSocket.close();
    }
    public void ReceivePayload()
    {
        //  First receive packet on UDP socket
        //  Then pass packet to SecurityLayer to decrypt/authenticate
        //  Then process decrypted audio packet with the VOIP layer
        //  Finally output the processed audio block to the speaker
    }
    public void Terminate()
    {
        running = false;
    }
}
