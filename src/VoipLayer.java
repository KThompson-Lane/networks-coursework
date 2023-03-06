import CMPC3M06.AudioPlayer;
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
    private AudioPlayer player;

    //For processing from transport layer
    private List<Integer> packetNums;

    //For interleaving
    private ByteBuffer[] packetBlock;
    ByteBuffer[] interleavedPackets = new ByteBuffer[9];
    ByteBuffer[] unInterleavedPackets = new ByteBuffer[9];
    int countRow = 0;
    int countColumn = 0;

    private int count = 0; //todo - explain

    int sentPackets = 0; //todo - explain
    int receivedPackets = 0;


    public VoipLayer() {
        packetNums = new ArrayList<>();
        packetBlock = new ByteBuffer[9]; //todo - change

        try {
            recorder = new AudioRecorder();
            player = new AudioPlayer();
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

    public void receiveFromSecurity(byte[] bytes) { //todo - make void
        ByteBuffer packetBuffer = ByteBuffer.wrap(bytes);
        if(receivedPackets < 9) {
            interleavedPackets[receivedPackets] = packetBuffer;
            receivedPackets++;
        }

        if(receivedPackets == 9){
           process();
        }
    }

    public void process(){
        if (count == 9 || count == 0) {
            //Un-interleave
            unInterleavedPackets = (unInterleave(interleavedPackets, 3));
            count = 0;
        }
        // Remove numbered header
        for (int i = 0; i < 9; i++) {
            int packetNum = unInterleavedPackets[count].getShort(0);
            packetNums.add(packetNum); //todo - sort this
            System.out.println("Packet Received: " + packetNum);

            // Send audio to Audio Layer
            byte[] audio = new byte[512];
            unInterleavedPackets[count].get(0, audio);
            count++;

            //  Finally output the processed audio block to the speaker
            try {
                player.playBlock(audio);
            } catch (IOException e) {
                System.out.println("ERROR: Listener: Some random IO error occurred!");
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        VoipLayer test = new VoipLayer();
        //for (int i = 0; i < 9; i++) {
        //    //TEST - Ensure interleaver working correctly
        //    ByteBuffer testBuff = ByteBuffer.wrap(test.getVoipBlock());
//
        //    int packetNum = testBuff.getShort();
        //    System.out.println(packetNum);
        //}

        for (int i = 0; i < 18; i++) {
            //TEST - Ensure unInterleaver working correctly
            //ByteBuffer testBuff = ByteBuffer.wrap(test.getVoipBlock());
            test.receiveFromSecurity(test.getVoipBlock());
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

    public ByteBuffer[] unInterleave(ByteBuffer[] packets, int blockSize) {
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
                rotatedBlock[j][blockSize - 1 - i] = block[i][j];
            }
        }

        ByteBuffer[] unInterleavedPackets = new ByteBuffer[9];
        int count2 = 0; //todo - change
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                unInterleavedPackets[count2] = rotatedBlock[i][j];
                count2++;
            }
        }
        return unInterleavedPackets;
    }
}
