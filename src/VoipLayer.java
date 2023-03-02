
// - Receive audio packets form Audio Layer, send to Security Layer
// - Compensate for problems (hide network issues from Audio Layer)

///todo:
//Task 1 - Set up basic VOIP Layer
//Task 2 - Compensation

//todo -
//take in audio block
//process
//send to processed block security layer
//package?


import Security.SecurityLayer;

import java.nio.ByteBuffer;

public class VoipLayer {

    private short packetCount = 0;

    private final SecurityLayer securityLayer;

    public VoipLayer(long secretKey) {
        securityLayer = new SecurityLayer(secretKey, true);
    }

    public ByteBuffer process(byte[] block){
        ByteBuffer numberedPacket = ByteBuffer.allocate(514);
        packetCount++;
        short packetNum = packetCount;
        numberedPacket.putShort(packetNum);

        block = securityLayer.EncryptAndAuth(block);
        numberedPacket.put(block); //todo - don't think it should come back here after going to Security Layer
        return numberedPacket;
    }

    public void number(){
        ByteBuffer numberedPacket = ByteBuffer.allocate(514);
        packetCount++;
        short packetNum = packetCount;
        numberedPacket.putShort(packetNum);
    }

    public void compensate(){}



}
