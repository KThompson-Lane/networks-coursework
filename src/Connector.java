import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Connector { //todo - think of better name
    boolean isHost = true;
    private InetAddress clientIP;
    private DatagramSocket sendingSocket;

    private DatagramSocket receivingSocket;

    int portNum = 55555;
    String ipAddr = "192.168.0.18";


    public void ConnectAsHost() {

        // Send request to connect with client, wait for response
        while(true)
        {
            Send("Requesting Connection...");
            try{
                //receive response
                String response = ReceiveResponse(); //todo - might not need to store - might want it for testing
                //send ack
                Send("Acknowledged. (Starting connection...)");
                break;
            }catch (SocketTimeoutException e)
            {
                System.out.println("Timeout waiting to receive response, resending request...");
            }catch (IOException e){
                System.out.println("ERROR: ConnectAsHost: Some random IO error occured!");
                e.printStackTrace();
            }
        }
    }

    public void ConnectAsClient(){

        while(true)
        {
            try{
                //wait for invite and accept
                ReceiveResponse();
                break;
            }catch (SocketTimeoutException e)
            {
                System.out.println("Timeout waiting to receive request, waiting...");
            } catch (IOException e){
                System.out.println("ERROR: ConnectAsClient: Some random IO error occured!");
                e.printStackTrace();
            }
        }

        while(true) //todo - put into other while loop?
        {
            try{
                //ack
                Send("Acknowledged...");
                //wait for ack
                ReceiveResponse();
                break;
            }catch (SocketTimeoutException e)
            {
                System.out.println("Timeout waiting to receive acknowledgement, waiting...");
            } catch (IOException e){
                System.out.println("ERROR: ConnectAsClient: Some random IO error occured!");
                e.printStackTrace();
            }
        }
    }

    public void Send(String message) //todo - might want more descriptive name
    {
        String str = message;
        byte[] buffer = str.getBytes();
        int portNum = 55555;

        //Invite Client
        try{
            //  Make a DatagramPacket from it, with client address and port number
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, portNum);
            //  Send it
            sendingSocket.send(packet);
        }catch (IOException e){
            System.out.println("ERROR: Send: Some random IO error occured!");
            e.printStackTrace();
        }
    }

    public String ReceiveResponse() throws IOException{
        byte[] buffer = new byte[80];
        DatagramPacket packet = new DatagramPacket(buffer, 0, 80);

        //try {
            receivingSocket.receive(packet);
            return new String(buffer); //todo - might not need to return anything
        //} catch (IOException e) {
        //    throw new RuntimeException(e);
        //}
    }

    public Connector() {
        //Set up Sending Socket
        //  Try and setup client IP from argument
        try {
            clientIP = InetAddress.getByName(ipAddr);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: SecureSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        //  Try and create socket for sending from
        try{
            this.sendingSocket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: SecureSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        //Set up Receiving Socket
        //  Try and create socket for sending from
        try{
            this.receivingSocket = new DatagramSocket(portNum);
            receivingSocket.setSoTimeout(1000);
        } catch (SocketException e){
            System.out.println("ERROR: SecureReceiver: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
    }
}
