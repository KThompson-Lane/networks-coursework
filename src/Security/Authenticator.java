package Security;
import java.nio.ByteBuffer;

public class Authenticator {
    private final int token;
    public Authenticator(int token) {
        this.token = token;
    }

    //SignPacket takes an encrypted packet and appends an authentication token
    public byte[] SignPacket(final byte[] encryptedPacket)
    {
        //  We first calculate a hash code for the packet
        int hashCode = calculateHash(encryptedPacket);

        //  We then prepend our hashcode to our original data, returning our now signed packet
        ByteBuffer authBuffer = ByteBuffer.allocate(encryptedPacket.length + 4);
        authBuffer.putInt(hashCode);
        authBuffer.put(encryptedPacket);
        return authBuffer.array();
    }

    //Authenticate takes a signed packet verifies its authenticity
    public byte[] Authenticate(final byte[] signedPacket) throws UnableToAuthenticateException
    {

        //  Try and authenticate, catching any exception and throwing our own
        try{
            // Wrap the signed packet in a ByteBuffer
            ByteBuffer buffer = ByteBuffer.wrap(signedPacket);
            //  Get the packet signature
            int packetSignature = buffer.getInt();
            byte[] packetData = new byte[signedPacket.length - 4];
            buffer.get(4, packetData);

            //  Recalculate our hash code
            int hashCode = calculateHash(packetData);

            //  Check our calculated hash code against the signature
            if(packetSignature == hashCode)
                return packetData;
        } catch (Exception ignored) {
        }
        throw new UnableToAuthenticateException("Unable to verify packet authenticity!");
    }

    private int calculateHash(final byte[] data)
    {
        ByteBuffer hashBuffer = ByteBuffer.allocate(data.length + 4);
        hashBuffer.put(data);
        hashBuffer.putInt(token);
        hashBuffer.rewind();
        return hashBuffer.hashCode();
    }

    public static void main(String[] args)
    {
        int establishedToken = 4000;
        int impostorToken = 4001;
        Authenticator sender = new Authenticator(establishedToken);
        Authenticator receiver = new Authenticator(establishedToken);
        Authenticator impostor = new Authenticator(impostorToken);

        String message = "hello friend! This is a very long message";

        byte[] authedPacket = sender.SignPacket(message.getBytes());
        System.out.println("Testing authenticity of sender packet: ");
        try {
            receiver.Authenticate(authedPacket);
            System.out.println("Sender packet is authentic!");
        } catch (UnableToAuthenticateException e) {
            System.out.println("Sender packet is unauthentic!");
        }

        byte[] impostorPacket = impostor.SignPacket(message.getBytes());
        System.out.println("Testing authenticity of impostor packet: ");
        try {
            receiver.Authenticate(impostorPacket);
            System.out.println("impostorPacket packet is authentic!");
        } catch (UnableToAuthenticateException e) {
            System.out.println("impostorPacket packet is unauthentic!");
        }
    }
}