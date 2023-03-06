import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VoipLayer {

    //For processing from audio layer
    private short packetCount = 0; //todo - explain
    private AudioRecorder recorder;

    //For processing from transport layer
    private List<Integer> packetNums;

    //For interleaving
    private ByteBuffer[] packetBlock;
    ByteBuffer[] interleavedPackets = new ByteBuffer[9];
    int countRow = 0;
    int countColumn = 0;

    private int count = 0; //todo - explain

    int sentPackets = 0; //todo - explain


    public VoipLayer() {
        packetNums = new ArrayList<>();
        packetBlock = new ByteBuffer[9]; //todo - change

        try {
            recorder = new AudioRecorder();
        } catch (LineUnavailableException e) {
            System.out.println("ERROR: Speaker: Could not start audio recorder.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void receiveFromAudio() { //todo - change name
        //Receive Audio
        byte[] audioBlock = null;
        try {
            audioBlock = recorder.getBlock();
        } catch (IOException e) {
            System.out.println("ERROR: Speaker: Some random IO error occurred!");
            e.printStackTrace();
            return;
        }

        ByteBuffer numberedPacket = ByteBuffer.allocate(514);
        // Add numbered header
        packetCount++;
        short packetNum = packetCount;
        numberedPacket.putShort(packetNum);
        numberedPacket.put(audioBlock);
        packetBlock[count] = numberedPacket;
        count++;
    }

    public byte[] receiveFromSecurity(ByteBuffer packetBuffer) {
        // Remove numbered header
        int packetNum = packetBuffer.getShort();
        packetNums.add(packetNum); //todo - sort this
        //System.out.println("Packet Received: " + packetNum);

        // Send audio to Audio Layer
        byte[] audio = new byte[512];
        packetBuffer.get(audio);

        return audio;
    }

    public byte[] getVoipBlock() {

        while (count != 9) {
            receiveFromAudio();
        }

        if (sentPackets == 0 || sentPackets == 9) {
            interleavedPackets = (interleave(packetBlock, 3));
            sentPackets = 1;
            count = 0;
            return interleavedPackets[0].array();
        } else {
            return interleavedPackets[sentPackets++].array();
        }
    }

    public static void main(String[] args) {
        VoipLayer test = new VoipLayer();
        for (int i = 0; i < 20; i++) {
            //TEST - Ensure interleaver working correctly
            ByteBuffer testBuff = ByteBuffer.wrap(test.getVoipBlock());

            int packetNum = testBuff.getShort();
            System.out.println(packetNum);
        }
    }

    public ByteBuffer interleave(ByteBuffer packets, int blockSize) { //todo - REMOVE
        // packets loaded into d * d blocks e.g 3 * 3 for 9 packets

        //Add to block as long as it isn't full
        byte[][] block = new byte[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                block[i][j] = packets.get();
                countRow++;
            }
            countColumn++;
        }

        byte[][] rotatedBlock = new byte[blockSize][blockSize];
        for (int i = 0; i < blockSize - 1; i++) {
            for (int j = 0; j < blockSize; j++) {
                rotatedBlock[blockSize - 1 - j][i] = block[i][j];
            }
        }

        ByteBuffer interleavedPackets = ByteBuffer.allocate(blockSize * blockSize);
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                interleavedPackets.put(rotatedBlock[i][j]);
            }
        }
        return interleavedPackets;
    }

    //todo - find a way to do all of this with less loops!!!!
    public ByteBuffer[] interleave(ByteBuffer[] packets, int blockSize) {
        // packets loaded into d * d blocks e.g 3 * 3 for 9 packets
        int count = 0;
        //Add to block as long as it isn't full
        ByteBuffer[][] block = new ByteBuffer[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                block[i][j] = packets[count];
                count++;
                countRow++;
            }
            countColumn++;
        }

        ByteBuffer[][] rotatedBlock = new ByteBuffer[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                rotatedBlock[blockSize - 1 - j][i] = block[i][j];
            }
        }

        ByteBuffer[] interleavedPackets = new ByteBuffer[9];
        int count2 = 0; //todo - change
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                interleavedPackets[count2] = rotatedBlock[i][j];
                count2++;
            }
        }
        return interleavedPackets;
    }
}
