package Security;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class KeyExchanger {
    private InetAddress clientIP;
    private int port;
    private DatagramSocket sendingSocket;
    private DatagramSocket receivingSocket;

    public KeyExchanger(int port, String ipAddr) {
        this.port = port;

        //Set up Sending Socket
        //  Try and setup client IP from argument
        try {
            clientIP = InetAddress.getByName(ipAddr);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: KeyExchanger: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        //  Try and create socket for sending from
        try{
            this.sendingSocket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: KeyExchanger: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        //Set up Receiving Socket
        try{
            this.receivingSocket = new DatagramSocket(port);
            receivingSocket.setSoTimeout(1000);
        } catch (SocketException e){
            System.out.println("ERROR: KeyExchanger: Could not open UDP socket to listen on.");
            e.printStackTrace();
            System.exit(0);
        }
    }
    public long ExchangeAsHost() {
        // Generate our public key and send to client waiting for theirs in response
        DHKey key = new DHKey();
        long otherKey;
        while (true)
        {
            //  Send key
            SendKey(key.GetPublicKey());
            //  Try and receive response key
            try{
                otherKey = ReceiveKey();
                //  Once received, send acknowledgement
                Ack();
                //  Then break loop and close sockets
                break;
            }catch (SocketTimeoutException ignored)
            {
                //  If timeout, send again
                System.out.println("HOST: timed out, resending");
            }
            catch (IOException e)
            {
                System.out.println("ERROR: KeyExchanger: Some random IO error occured!");
                e.printStackTrace();
            }
        }

        long secretKey = key.GetSecretKey(otherKey);
        System.out.println("Received other key, shared secret is: " + secretKey);
        receivingSocket.close();
        sendingSocket.close();
        return secretKey;
    }

    public long ExchangeAsClient(){
        // Generate our public key and wait for hosts sending ours in response
        DHKey key = new DHKey();
        //  Wait for host key
        long otherKey;
        while (true)
        {
            //  Try and receive response key
            try{
                otherKey = ReceiveKey();
                break;
            }catch (SocketTimeoutException ignored)
            {
                //  If timeout, send again
                System.out.println("CLIENT: timed out");
            }
            catch (IOException e)
            {
                System.out.println("ERROR: KeyExchanger: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        while (true)
        {
            //  When received, send our key
            try{
                SendKey(key.GetPublicKey());
                //  Then wait for acknowledgement
                boolean received = ReceiveAck();
                //  Then break loop and close sockets
                if (received)
                    break;
            }catch (SocketTimeoutException ignored)
            {
                //  If timeout, send again
            }
            catch (IOException e)
            {
                System.out.println("ERROR: KeyExchanger: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        long secretKey = key.GetSecretKey(otherKey);
        System.out.println("Received other key, shared secret is: " + secretKey);
        receivingSocket.close();
        sendingSocket.close();
        return secretKey;
    }

    public void SendKey(long key)
    {
        //Send the key
        try{
            ByteBuffer buffer = ByteBuffer.allocate(64);
            buffer.putLong(key);

            //  Make a DatagramPacket containing key, with client address and port number
            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.remaining(), clientIP, port);

            //  Send it
            sendingSocket.send(packet);
        }catch (IOException e){
            System.out.println("ERROR: Send: Some random IO error occured!");
            e.printStackTrace();
        }
    }
    public long ReceiveKey() throws IOException {
        //  Allocate 64 bit buffer for key
        ByteBuffer buffer = ByteBuffer.allocate(64);
        //  Receive packet
        DatagramPacket packet = new DatagramPacket(buffer.array(), 0, buffer.capacity());
        receivingSocket.receive(packet);
        //  Retrieve key from buffer
        return buffer.getLong();
    }
    public void Ack()
    {
        //Send the key
        try{
            byte[] buff = "ack".getBytes();
            //  Make a DatagramPacket containing ack, with client address and port number
            DatagramPacket packet = new DatagramPacket(buff, buff.length, clientIP, port);
            //  Send it
            sendingSocket.send(packet);
        }catch (IOException e){
            System.out.println("ERROR: Send: Some random IO error occured!");
            e.printStackTrace();
        }
    }
    public boolean ReceiveAck() throws IOException {
        //  Allocate 64 bit buffer for key
        ByteBuffer buffer = ByteBuffer.allocate(64);
        //  Receive packet
        DatagramPacket packet = new DatagramPacket(buffer.array(), 0, buffer.capacity());
        receivingSocket.receive(packet);
        //  Retrieve key from buffer
        String response = new String(buffer.array());
        return response.contains("ack");
    }
}

