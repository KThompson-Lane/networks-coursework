package Security;
import java.nio.ByteBuffer;

public class Authenticator {
    private final int token;
    public Authenticator(int token) {
        this.token = token;
    }

    //SignPacket takes an encrypted packet and appends an authentication token
    public byte[] SignPacket(byte[] encryptedPacket)
    {
        //  Allocate a buffer which can hold our packet and authentication token which is a 4 byte int
        ByteBuffer buffer = ByteBuffer.allocate(encryptedPacket.length + 4);
        buffer.putInt(token);
        buffer.put(encryptedPacket);
        return buffer.array();
    }

    //Authenticate takes a signed packet verifies its authenticity
    public boolean Authenticate(byte[] signedPacket)
    {
        // Wrap the signed packet in a ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(signedPacket);
        //  Get the authentication token
        int packetToken = buffer.getInt();
        //  Check our token against the packet token
        return packetToken == token;
    }

    public static void main(String[] args)
    {
        int establishedToken = 4000;
        int imposterToken = 300;
        Authenticator sender = new Authenticator(establishedToken);
        Authenticator receiver = new Authenticator(establishedToken);
        Authenticator imposter = new Authenticator(imposterToken);

        String message = "hello friend!";

        byte[] authedPacket = sender.SignPacket(message.getBytes());
        System.out.println("Is senders an authentic packet: "+ receiver.Authenticate(authedPacket));
        byte[] imposterPacket = imposter.SignPacket(message.getBytes());
        System.out.println("Is imposters an authentic packet: "+ receiver.Authenticate(imposterPacket));

    }
}