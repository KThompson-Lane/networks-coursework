import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Connector {
    private final InetAddress clientIP;
    private final int port;
    private DatagramSocket sendingSocket;

    private DatagramSocket receivingSocket;


    public Connector(int portNum, InetAddress ipAddr) {
        port = portNum;
        //Set up Sending Socket
        //  Setup client IP from argument
        clientIP = ipAddr;

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
            this.receivingSocket = new DatagramSocket(port);
            receivingSocket.setSoTimeout(1000);
        } catch (SocketException e){
            System.out.println("ERROR: SecureReceiver: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
    }


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
        sendingSocket.close();
        receivingSocket.close();
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
        receivingSocket.close();
        sendingSocket.close();
    }

    public void Send(String message) //todo - might want more descriptive name
    {
        byte[] buffer = message.getBytes();

        //Invite Client
        try{
            //  Make a DatagramPacket from it, with client address and port number
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, port);
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

        receivingSocket.receive(packet);
        return new String(buffer); //todo - might not need to return anything
    }
}
