package Security;

import java.nio.ByteBuffer;

public class PacketAuth {
    private final int token;

    public PacketAuth(int token) {
        this.token = token;
    }

    //  Method takes in an un-authed packet and appends an authentication token header
    public byte[] Authenticate(byte[] unauthedPacket)
    {
        //  Allocate a buffer which can hold our packet and authID which is a 4 byte int
        ByteBuffer buffer = ByteBuffer.allocate(unauthedPacket.length + 4);
        buffer.putInt(token);
        buffer.put(unauthedPacket);
        return buffer.array();
    }
    public boolean CheckAuth(byte[] packet)
    {
        //  Allocate a buffer which can hold our packet and authID which is a 4 byte int
        ByteBuffer buffer = ByteBuffer.wrap(packet);
        int packetToken = buffer.getInt();
        return packetToken == token;
    }
    public static void main(String[] args)
    {
        int establishedToken = 4000;
        int imposterToken = 300;
        PacketAuth sender = new PacketAuth(establishedToken);
        PacketAuth receiver = new PacketAuth(establishedToken);
        PacketAuth imposter = new PacketAuth(imposterToken);

        String message = "hello friend!";

        byte[] authedPacket = sender.Authenticate(message.getBytes());
        System.out.println("Is senders an authentic packet: "+ receiver.CheckAuth(authedPacket));
        byte[] imposterPacket = imposter.Authenticate(message.getBytes());
        System.out.println("Is imposters an authentic packet: "+ receiver.CheckAuth(imposterPacket));

    }
}
