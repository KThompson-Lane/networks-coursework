
// - Receive audio packets form Audio Layer, send to Security Layer
// - Compensate for problems (hide network issues from Audio Layer)

///todo:
//Task 1 - Set up basic VOIP Layer
//Task 2 - Compensation

//todo -
//take in audio block
//process
//send to processed block security layer


import CMPC3M06.AudioRecorder;
import Security.SecurityLayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoipLayer {

    //For processing from audio layer
    private short packetCount = 0;

    //For processing from transport layer
    private List<Integer> packetNums;

    //For interleaving
    private ByteBuffer[] packetBlock;

    private AudioRecorder recorder;

    ByteBuffer[] interleavedPackets = new ByteBuffer[9];

    private int count = 0;

    int sentPackets = 0;
    int countRow = 0;
    int countColumn = 0;

    public VoipLayer() {
        packetNums = new ArrayList<>();
        packetBlock = new ByteBuffer[9]; //todo - change

        try{
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

        //if(count == 9) {
            // Send buffer of x amount of packets to interleaver
            //ByteBuffer interleavedPackets = ByteBuffer.allocate(514);
            //for (int i = 0; i < 9; i++) {
            //    ByteBuffer packetToInterleave = ByteBuffer.allocate(9;
            //    packetToInterleave.put(i, numberedPacket, 0, 9); //todo - is this always putting the first 9?
            //    i += 8;
            //    interleavedPackets.put(interleave(packetToInterleave, 3)); //todo - check this puts it at the right position
            //}


            //interleavedPackets = (interleave(packetBlock, 3));


            //Send each interleaved packet back to Speaker, one by one

            //count = 0;
        //}
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

    public void compensate() {
    }

    public void receiverBasedComp() {
    }

    public byte[] getVoipBlock(){

        while(count != 9) {
            receiveFromAudio();
        }

        if(sentPackets == 0 || sentPackets == 9) {
            interleavedPackets = (interleave(packetBlock, 3)); //todo - ensure only first 9 are used
            sentPackets = 1;
            count = 0;
            return interleavedPackets[0].array();
        }
        else {
            return interleavedPackets[sentPackets++].array();
        }
    }

    public static void main(String[] args) {
        VoipLayer test = new VoipLayer();
        for(int i = 0; i < 20; i++)
        {
            //test.receiveFromAudio();
            //ByteBuffer testBuff = ByteBuffer.allocate(514);
            //testBuff.put(test.getVoipBlock());
            ByteBuffer testBuff = ByteBuffer.wrap(test.getVoipBlock());

            int packetNum = testBuff.getShort();
            System.out.println(packetNum);
        }
    }

//////////////////////////////////////////////////////////////////////TODO/////////////////////////////////////////////////////////////////////////////////////////////////
    //Todo - Think about how interleaver needs to be used
    //It needs to add enough packets to the block to fill it before it rotates it
    //Afterwards packets need to be sent individually to the security layer


    public ByteBuffer interleave(ByteBuffer packets, int blockSize) { //todo - don't need blockSize, use .length?? There isn't one??
        // packets loaded into d * d blocks e.g 3 * 3 for 9 packets

        //Add to block as long as it isn't full
        byte[][] block = new byte[blockSize][blockSize];
        //if (countColumn != blockSize && countRow != blockSize) { //todo - probably get rid of this now
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                block[i][j] = packets.get(); //todo - check this is getting the next one - should do as it increments position when called
                countRow++;
            }
            countColumn++;
        }
        //} else {
        // rotate block 90 degrees
        byte[][] rotatedBlock = new byte[blockSize][blockSize];
        for (int i = 0; i < blockSize - 1; i++) {
            for (int j = 0; j < blockSize; j++) {
                rotatedBlock[blockSize - 1 - j][i] = block[i][j];
            }
        }

        ByteBuffer interleavedPackets = ByteBuffer.allocate(blockSize * blockSize); //todo - find a way to do all of this with less loops!!!!
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                interleavedPackets.put(rotatedBlock[i][j]);
            }
        }
        return interleavedPackets;
    }

    public ByteBuffer[] interleave(ByteBuffer[] packets, int blockSize) { //todo - don't need blockSize, use .length?? There isn't one??
        // packets loaded into d * d blocks e.g 3 * 3 for 9 packets
        int count = 0;
        //Add to block as long as it isn't full
        ByteBuffer[][] block = new ByteBuffer[blockSize][blockSize];
        //if (countColumn != blockSize && countRow != blockSize) { //todo - probably get rid of this now
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                block[i][j] = packets[count];
                count++;
                countRow++;
            }
            countColumn++;
        }
        //} else {
        // rotate block 90 degrees
        ByteBuffer[][] rotatedBlock = new ByteBuffer[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                rotatedBlock[blockSize - 1 - j][i] = block[i][j];
            }
        }

        //ByteBuffer interleavedPackets = ByteBuffer.allocate(blockSize * blockSize); //todo - find a way to do all of this with less loops!!!!
        ByteBuffer[] interleavedPackets = new ByteBuffer[9];
        int count2 = 0; //todo - change
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                //interleavedPackets.put(rotatedBlock[i][j]);
                interleavedPackets[count2] = rotatedBlock[i][j];
                count2++;
            }
        }
        return interleavedPackets;
    }

    public ByteBuffer interleave3(ByteBuffer[] packets, int blockSize) { //todo - don't need blockSize, use .length?? There isn't one??
        // packets loaded into d * d blocks e.g 3 * 3 for 9 packets
        int count = 0;
        //Add to block as long as it isn't full
        ByteBuffer[][] block = new ByteBuffer[blockSize][blockSize];
        //if (countColumn != blockSize && countRow != blockSize) { //todo - probably get rid of this now
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                block[i][j] = packets[count];
                count++;
                countRow++;
            }
            countColumn++;
        }
        //} else {
        // rotate block 90 degrees
        ByteBuffer[][] rotatedBlock = new ByteBuffer[blockSize][blockSize];
        for (int i = 0; i < blockSize - 1; i++) {
            for (int j = 0; j < blockSize; j++) {
                rotatedBlock[blockSize - 1 - j][i] = block[i][j];
            }
        }

        ByteBuffer interleavedPackets = ByteBuffer.allocate(blockSize * blockSize); //todo - find a way to do all of this with less loops!!!!
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                interleavedPackets.put(rotatedBlock[i][j]);
            }
        }
        return interleavedPackets;
    }
}
