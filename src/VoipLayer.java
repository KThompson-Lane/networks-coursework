import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VoipLayer {

    private AudioRecorder recorder;
    private AudioPlayer player;

    //Global variables for interleaving
    short sentPackets = 0;
    byte[][] interleaverBuffer = new byte[9][];


    //For processing from security layer
    private List<Integer> packetNums;
    private int receivedPackets = 0; //counts number of packets to be de-interleaved


    //For interleaving/de-interleaving
    private ByteBuffer[] packetBlock;
    private ByteBuffer[] interleavedPackets = new ByteBuffer[9];
    private ByteBuffer[] unInterleavedPackets = new ByteBuffer[9];
    private int countRow = 0;
    private int countColumn = 0;

    private int count = 0; //Keeps track of which packets have been interleaved/de-interleaved

    //private Boolean listener;

    public VoipLayer(Boolean listener) {
        packetNums = new ArrayList<>();
        packetBlock = new ByteBuffer[9]; //todo - change

        try {
            //if(listener)
                player = new AudioPlayer();
            //else
                recorder = new AudioRecorder();

        } catch (LineUnavailableException e) {
            System.out.println("ERROR: Speaker: Could not start audio recorder.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    //Method for getting a numbered audio block
    public void receiveFromAudio(int index)
    {
        //Receive Audio
        byte[] audioBlock = null;
        try {
            audioBlock = recorder.getBlock();
        } catch (IOException e) {
            System.out.println("ERROR: Speaker: Some random IO error occurred!");
            e.printStackTrace();
            return;
        }
        //  Create new packet
        ByteBuffer numberedPacket = ByteBuffer.allocate(514);
        short packetNum = (short) (sentPackets + index);
        numberedPacket.putShort(packetNum);
        numberedPacket.put(audioBlock);
        interleaverBuffer[index] = numberedPacket.array();
    }

    //Method for getting interleaved voip block
    public byte[] getVoipBlock() {
        //packetIndex ranges from 0-8
        short packetIndex = (short) (sentPackets % 9);

        //If we get to 9 sent packets, refill buffer and interleave
        if(packetIndex == 0)
        {
            //  Fill InterleaverBuffer w/ 9 audio blocks
            for(int i = 0; i < 9; i++)
            {
                receiveFromAudio(i);
            }
            //  Interleave all 9 audio blocks
            interleave(interleaverBuffer, 3);
        }

        //Return audio block at position packetIndex
        sentPackets++;
        return interleaverBuffer[packetIndex];
    }

    public void receiveFromSecurity(byte[] bytes) {
        ByteBuffer packetBuffer = ByteBuffer.wrap(bytes); //todo - do we lose this packet if we go striaght to process? Could this happen?
        //System.out.println("Packet Received: " + packetBuffer.getShort(0));
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
        VoipLayer test = new VoipLayer(true);
        //for (int i = 0; i < 9; i++) {
        //    //TEST - Ensure interleaver working correctly
        //    ByteBuffer testBuff = ByteBuffer.wrap(test.getVoipBlock());

        //    int packetNum = testBuff.getShort();
        //    System.out.println(packetNum);
        //}

        for (int i = 0; i < 18; i++) {
            //TEST - Ensure unInterleaver working correctly
            test.receiveFromSecurity(test.getVoipBlock());
        }
    }


    //todo - find a way to do all of this with less loops!!!!
    public ByteBuffer[] interleave(byte[][] packets, int blockSize) {
        // packets loaded into d * d blocks e.g 3 * 3 for 9 packets
        int count = 0;
        //Add to block as long as it isn't full
        ByteBuffer[][] block = new ByteBuffer[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                block[i][j] = ByteBuffer.wrap(packets[count]);
                count++;
            }
        }

        ByteBuffer[][] rotatedBlock = new ByteBuffer[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                rotatedBlock[blockSize - 1 - j][i] = block[i][j];
            }
        }

        ByteBuffer[] interleavedPackets = new ByteBuffer[9];
        count = 0;
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                interleavedPackets[count] = rotatedBlock[i][j];
                count++;
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
            }
        }

        ByteBuffer[][] rotatedBlock = new ByteBuffer[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                rotatedBlock[j][blockSize - 1 - i] = block[i][j];
            }
        }

        ByteBuffer[] unInterleavedPackets = new ByteBuffer[9];
        count = 0;
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                unInterleavedPackets[count] = rotatedBlock[i][j];
                count++;
            }
        }
        return unInterleavedPackets;
    }
}
