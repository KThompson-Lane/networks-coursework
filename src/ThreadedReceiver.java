import javax.xml.crypto.Data;

import java.net.*;
import java.io.*;

public abstract class ThreadedReceiver implements Runnable {

    private boolean running;
    protected int PORT;
    protected DatagramSocket receivingSocket;

    private Thread thread;

    public ThreadedReceiver(int PORT) {
        //  Set port number to argument
        this.PORT = PORT;

        //  Try and create socket for sending from
        try{
            this.receivingSocket = new DatagramSocket(PORT);
            receivingSocket.setSoTimeout(500);
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void Start()
    {
        //Make a new thread from this class
        thread = new Thread(this);
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
    public abstract void ReceivePayload();
    public void Terminate()
    {
        running = false;
        thread.interrupt();
    }

    public boolean IsRunning()
    {
        return running;
    }
}
