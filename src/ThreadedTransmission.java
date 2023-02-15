import java.net.*;
import java.io.*;

public abstract class ThreadedTransmission implements Runnable {

    private boolean running;
    protected int PORT;
    protected InetAddress clientIP;
    protected DatagramSocket sendingSocket;

    public ThreadedTransmission(int PORT, String clientAddress) {
        //  Set port number to argument
        this.PORT = PORT;

        //  Try and setup client IP from argument
        try {
            clientIP = InetAddress.getByName(clientAddress);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }
        //  Try and create socket for sending from
        try{
            this.sendingSocket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
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
    public abstract void TransmitPayload();
    public void Terminate()
    {
        running = false;
        System.out.println("Terminating sender thread");
    }
}
