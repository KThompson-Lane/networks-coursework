import javax.crypto.Cipher;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class KeyExchanger {
    static int PORT = 55555;
    static String destAddress = "192.168.0.16";

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
        PrivateKey privateKey = kp.getPrivate();

        SecureSender sender = new SecureSender(destAddress, publicKey);
        SecureReceiver receiver = new SecureReceiver(PORT);
        String response;
        while(true)
        {
            sender.SendPublicKey();
            try{
                response = receiver.ReceiveResponse(privateKey);
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
        PublicKey receivedKey;
        while(true)
        {
            try{
                receivedKey = receiver.ReceivePublicKey();
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
        SecureSender sender = new SecureSender(destAddress, receivedKey);
        sender.SendResponse();
    }


    private class SecureSender
    {
        private final Key publicKey;
        private InetAddress clientIP;
        private DatagramSocket sendingSocket;

        public SecureSender(String clientAddress, Key pub) {

            //  Try and setup client IP from argument
            try {
                clientIP = InetAddress.getByName(clientAddress);
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
                System.out.println("ERROR: SecureSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        public void SendResponse()
        {
            try{
                //  TODO: Use the public key to encrypt response
                //  Send a response saying we got the key
                String response = "got the key!";
                byte[] buffer = EncryptMessage(response.getBytes());
                //  Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
                //  Send it
                sendingSocket.send(packet);
            }catch (IOException e){
                System.out.println("ERROR: SecureSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        public byte[] EncryptMessage(byte[] plainText)
        {
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                return cipher.doFinal(plainText);
            } catch (Exception e) {
                System.out.println("ERROR: SecureSender: Could not encrypt message!");
                e.printStackTrace();
                return null;
            }
        }
    }
    private class SecureReceiver
    {
        private DatagramSocket receivingSocket;

        public SecureReceiver(int PORT) {
            //  Try and create socket for sending from
            try{
                this.receivingSocket = new DatagramSocket(PORT);
                receivingSocket.setSoTimeout(1000);
            } catch (SocketException e){
                System.out.println("ERROR: SecureReceiver: Could not open UDP socket to send from.");
                e.printStackTrace();
                System.exit(0);
            }
        }

        protected PublicKey ReceivePublicKey() throws IOException {
            //  Initialize a buffer to store our public key
            //TODO: Figure out correct size for buffer
            byte[] buffer = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buffer, 0, 1000);
            receivingSocket.receive(packet);

            //  Try and parse a public key from the buffer and store result in publicKey
            PublicKey publicKey = null;
            try {
                publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(buffer));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                System.out.println("ERROR: SecureReceiver: Could not create an RSA key pair generator.");
                e.printStackTrace();
                System.exit(0);
            }
            return publicKey;
        }
        protected String ReceiveResponse(PrivateKey privateKey) throws IOException {
            //  Receive the encrypted response packet (note that the response string cant be more than 80 chars)
            //TODO: Use a header and a bytebuffer to determine the response length, NOT MAGIC NUMBER
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, 0, 256);
            receivingSocket.receive(packet);

            byte[] plainText = DecryptMessage(buffer, privateKey);

            //  Return response string
            return new String(plainText);
        }

        public byte[] DecryptMessage(byte[] cipherText, PrivateKey privateKey) {
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                return cipher.doFinal(cipherText);
            } catch (Exception e) {
                System.out.println("ERROR: SecureSender: Could not encrypt message!");
                e.printStackTrace();
                return null;
            }
        }
    }
}
