import java.io.*;
import java.net.*;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyExchanger {
    static int PORT = 55555;
    static String destAddress = "192.168.0.1";

    public void ExchangeAsHost()
    {
        KeyPairGenerator kpg;
        KeyPair kp = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            //  initialize our keypair generator to a size of 2048 bits
            kpg.initialize(2048);
            kp = kpg.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("ERROR: SecureSender: Could not create an RSA key pair generator.");
            e.printStackTrace();
            System.exit(0);
        }
        Key publicKey = kp.getPublic();
        Key privateKey = kp.getPrivate();

        SecureSender sender = new SecureSender(PORT, destAddress, publicKey);
        SecureReceiver receiver = new SecureReceiver(PORT);
        String response;
        while(true)
        {
            sender.SendPublicKey();
            try{
                response = receiver.ReceiveResponse();
                break;
            }catch (SocketTimeoutException e)
            {
                System.out.println("Timeout waiting to receive response, resending public key...");
            }catch (IOException e){
                System.out.println("ERROR: SecureSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        System.out.print("We received the response: ");
        System.out.println(response);
    }

    public void ExchangeAsClient()
    {
        SecureReceiver receiver = new SecureReceiver(PORT);
        byte[] test;
        while(true)
        {
            try{
                test = receiver.ReceivePublicKey();
                break;
            }catch (SocketTimeoutException e)
            {
                System.out.println("Timeout waiting to receive public key, waiting...");
            } catch (IOException e){
                System.out.println("ERROR: SecureSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        System.out.println("We received the key, sending response!");
        //  TODO: Change null to the received key
        SecureSender sender = new SecureSender(PORT, destAddress, null);
        sender.SendResponse();
    }


    private class SecureSender
    {
        private Key publicKey;
        private int PORT;
        private InetAddress clientIP;
        private DatagramSocket sendingSocket;

        public SecureSender(int PORT, String clientAddress, Key pub) {
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
            this.publicKey = pub;
        }

        public void SendPublicKey() {
            try{
                //  TODO: We should append some sort of header to inform the receiver that this is the public key
                //  Convert the public key to an array of bytes
                byte[] buffer = publicKey.getEncoded();

                //  Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
                //  Send it
                sendingSocket.send(packet);

            }catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        public void SendResponse()
        {
            try{
                //  TODO: Use the public key to encrypt response
                //  Send a response saying we got the key
                String response = "got the key!";
                byte[] buffer = response.getBytes();
                //  Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
                //  Send it
                sendingSocket.send(packet);
            }catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
    }
    private class SecureReceiver
    {
        private Key publicKey;
        private int PORT;
        private DatagramSocket receivingSocket;

        public SecureReceiver(int PORT) {
            this.PORT = PORT;
            //  Try and create socket for sending from
            try{
                this.receivingSocket = new DatagramSocket(PORT);
                receivingSocket.setSoTimeout(1000);
            } catch (SocketException e){
                System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
                e.printStackTrace();
                System.exit(0);
            }
        }

        protected byte[] ReceivePublicKey() throws IOException {
            //Receive a DatagramPacket (note that the string cant be more than 80 chars)
            byte[] buffer = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buffer, 0, 1000);
            receivingSocket.receive(packet);
            return buffer;
        }
        protected String ReceiveResponse() throws IOException {
            //  Receive a DatagramPacket (note that the string cant be more than 80 chars)
            byte[] buffer = new byte[80];
            DatagramPacket packet = new DatagramPacket(buffer, 0, 80);

            receivingSocket.receive(packet);

            //Get a string from the byte buffer
            String str = new String(buffer);
            //  Return it
            return str;
        }
    }
}
