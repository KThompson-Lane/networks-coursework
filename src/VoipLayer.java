
// - Receive audio packets form Audio Layer, send to Security Layer
// - Compensate for problems (hide network issues from Audio Layer)

///todo:
//Task 1 - Set up basic VOIP Layer
//Task 2 - Compensation

//todo -
//take in audio block
//process
//send to processed block security layer


import Security.SecurityLayer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VoipLayer {

    //For processing from audio layer
    private short packetCount = 0;

    //For processing from transport layer
    private List<Integer> packetNums;

    private final SecurityLayer securityLayer;

    public VoipLayer(long secretKey) {
        packetNums = new ArrayList<>();

        securityLayer = new SecurityLayer(secretKey, true);
    }

    public ByteBuffer receiveFromAudio(byte[] block){
        ByteBuffer numberedPacket = ByteBuffer.allocate(514);
        // Add numbered header
        packetCount++;
        short packetNum = packetCount;
        numberedPacket.putShort(packetNum);

        // Send packet to Security Layer
        block = securityLayer.EncryptAndAuth(block);
        numberedPacket.put(block); //todo - don't think it should come back here after going to Security Layer

        return numberedPacket;
    }

    public byte[] receiveFromSecurity(ByteBuffer packetBuffer){
        // Remove numbered header
        int packetNum = packetBuffer.getShort();
        packetNums.add(packetNum); //todo - sort this
        //System.out.println("Packet Received: " + packetNum);

        // Send audio to Audio Layer
        byte[] audio = new byte[512];
        packetBuffer.get(audio);

        return audio;
    }

    public void compensate(){}



}
