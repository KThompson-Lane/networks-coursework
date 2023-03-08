package Security;

import java.io.IOException;
import java.net.*;

public class Impostor  implements Runnable{
    private final int port;
    private boolean running;
    private final InetAddress destinationAddress;
    private DatagramSocket sendingSocket;

    public Impostor() {
        //  Set port number to argument
        this.port = 55555;
        //  Try and setup client IP from argument
        destinationAddress = InetAddress.getLoopbackAddress();

        //  Try and create socket for sending from
        try{
            this.sendingSocket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: Impostor: Could not open UDP socket to send from.");
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
            Attack();
        }
        //  Close socket then terminates thread
        sendingSocket.close();
    }
    public void Attack()
    {
        //  Generate imposter packet
        byte[] message = "Nobody expects the Spanish Inquisition!".getBytes();
        DatagramPacket packet = new DatagramPacket(message, message.length, destinationAddress, port);

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
