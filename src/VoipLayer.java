import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoipLayer {

    private AudioRecorder recorder;
    private AudioPlayer player;

    //Global variables for interleaving
    short sentPackets = 0;
    byte[][] interleaverBuffer = new byte[9][];

    boolean interleave;


    //For processing from security layer
    private List<Integer> packetNums;
    private int receivedPackets = 0; //counts number of packets to be de-interleaved

    private int blockNum = 0;


    //For interleaving/de-interleaving
    private byte[][] interleavedPackets = new byte[9][];
    private byte[][] unInterleavedPackets = new byte[9][];

    private int lastPacketNum = 0;
    private byte[][] outOfOrderBytes = new byte[9][]; //todo - figure out length
    private int outOfOrderCount = 0;

    public VoipLayer(boolean listener, boolean interleaving) {
        packetNums = new ArrayList<>();
        interleave = interleaving;

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

    //////// Speaker ////////

    //Method for getting a numbered audio block
    public void getNumberedAudioBlock(int index)
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

    public byte[] getAudioBlock()
    {
        //Receive Audio
        byte[] audioBlock = null;
        try {
            audioBlock = recorder.getBlock();
        } catch (IOException e) {
            System.out.println("ERROR: Speaker: Some random IO error occurred!");
            e.printStackTrace();
        }
        return audioBlock;
    }

    //Method for getting interleaved voip block
    public ByteBuffer getInterleavedVoipBlock() {
        //packetIndex ranges from 0-8
        short packetIndex = (short) (sentPackets % 9);

        //If we get to 9 sent packets, refill buffer and interleave
        if(packetIndex == 0)
        {
            //  Fill InterleaverBuffer w/ 9 audio blocks

            for(int i = 0; i < 9; i++)
            {
                getNumberedAudioBlock(i);
            }
            //  Interleave all 9 audio blocks
            interleaverBuffer = interleave(interleaverBuffer, 3);
        }

        //Add post-interleave sequence number
        ByteBuffer sequencedPacket = ByteBuffer.allocate(516);
        short packetNum = (short) (sentPackets);
        sequencedPacket.putShort(packetNum);
        sequencedPacket.put(interleaverBuffer[packetIndex]);

        //byte[] audio = new byte[516];
        //sequencedPacket.get(0, audio);
//
        ////Return audio block at position packetIndex
        //sentPackets++;
        //return audio;
        return sequencedPacket;
    }

    public byte[] getVoipBlock() {
        if(!interleave) {
            //Add sequence number
            ByteBuffer sequencedPacket = ByteBuffer.allocate(514);
            short packetNum = (short) (sentPackets);
            sequencedPacket.putShort(packetNum);
            // Gets an audio block and adds it to the packet
            sequencedPacket.put(getAudioBlock());

            //byte[] packet = new byte[514];
            //sequencedPacket.get(0, packet);
//
            //sentPackets++;
            //return packet;
            return sendToSecurityLayer(sequencedPacket, 514);
        }
        //return getInterleavedVoipBlock();
        return sendToSecurityLayer(getInterleavedVoipBlock(), 516);
    }

    public byte[] sendToSecurityLayer(ByteBuffer bytes, int byteLength){ //todo - rename
        byte[] packet = new byte[byteLength];
        bytes.get(0, packet);

        sentPackets++;
        return packet;
    }

    //////// Listener ////////

    public void processNumber(byte[] bytes) {
        //Remove post-interleave sequence numbers
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int packetNum = buffer.getShort(0);

        //Add to array to be de-interleaved and send when array is full
        int index = packetNum % 9;

        // If packet belongs in previous block
        if(packetNum / 9 < blockNum)
        {
            // Discard packet
            return;
        }
        // If packet belongs in next block
        else if(packetNum / 9 > blockNum) {
            // Process
            process();
            blockNum++;

            // Add packet to new block
            interleavedPackets[index] = bytes;
        }
        // If packet belongs in current block
        else {
            // Add to array
            interleavedPackets[index] = bytes;
            //todo - issue here! We don't play the last set of interleaved packets as we won't get a packet from the next block - probably not that much of an issue
        }
    }

    public void process(){ //todo - rename
        //todo - move to after de-interleaved
        // fill in missed packets
        int good = 0; //todo - rename
        for (int i = 0; i < interleavedPackets.length; i++) {
            if(interleavedPackets[i] == null){
                interleavedPackets[i] = interleavedPackets[good];
            }
            else {
                good = i;
            }
        }

        //Un-interleave
        unInterleavedPackets = (unInterleave(interleavedPackets, 3));

        // Remove numbered header
        for (int i = 0; i < 9; i++) {
            byte[] bytes = unInterleavedPackets[i];
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            int packetNum = buffer.getShort(2);
            System.out.println("Packet Received: " + packetNum);

            //// Send audio to Audio Layer
            //byte[] audio = new byte[512];
            //buffer.get(4, audio);
//
            ////  Finally output the processed audio block to the speaker
            //try {
            //    player.playBlock(audio);
            //} catch (IOException e) {
            //    System.out.println("ERROR: Listener: Some random IO error occurred!");
            //    e.printStackTrace();
            //}
            sendToAudioLayer(buffer, 4);
        }

        for (int i = 1; i < interleavedPackets.length; i++) {
            interleavedPackets[i] = null;
        }
        byte[] test = new byte[516];
        Arrays.fill(test, (byte)0);
        interleavedPackets[0] = test;
    }

    public void playAudio(byte[] bytes){
        if(!interleave) {
            // Remove sequence numbers
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            int packetNum = buffer.getShort(0); //todo - use if needed
            System.out.println("Packet Received: " + packetNum);

            //// Get audio
            //byte[] audio = new byte[512];
            //buffer.get(2, audio);
//
            ////  Finally output the processed audio block to the speaker
            //try {
            //    player.playBlock(audio);
            //} catch (IOException e) {
            //    System.out.println("ERROR: Listener: Some random IO error occurred!");
            //    e.printStackTrace();
            //}
            sendToAudioLayer(buffer, 2);
        }
        else {
            processNumber(bytes);
        }
    }

    public void sendToAudioLayer(ByteBuffer buffer, int index){
        // Get audio
        byte[] audio = new byte[512];
        buffer.get(index, audio);

        //  Finally output the processed audio block to the speaker
        try {
            player.playBlock(audio);
        } catch (IOException e) {
            System.out.println("ERROR: Listener: Some random IO error occurred!");
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        VoipLayer test = new VoipLayer(true, false);
        //for (int i = 0; i < 9; i++) {
        //    //TEST - Ensure interleaver working correctly
        //    ByteBuffer testBuff = ByteBuffer.wrap(test.getVoipBlock());
//
        //    int packetNum = testBuff.getShort();
        //    System.out.println(packetNum);
        //}

        for (int i = 0; i < 18; i++) {
            //TEST - Ensure unInterleaver working correctly
            test.playAudio(test.getVoipBlock());
        }
    }


    //todo - find a way to do all of this with less loops!!!!
    public byte[][] interleave(byte[][] packets, int blockSize) {
        // packets loaded into d * d blocks e.g 3 * 3 for 9 packets
        int count = 0;
        //Add to block as long as it isn't full
        byte[][][] block = new byte[blockSize][blockSize][];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                block[i][j] = packets[count];
                count++;
            }
        }

        byte[][][] rotatedBlock = new byte[blockSize][blockSize][];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                rotatedBlock[blockSize - 1 - j][i] = block[i][j];
            }
        }

        byte[][] interleavedPackets = new byte[9][];
        count = 0;
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                interleavedPackets[count] = rotatedBlock[i][j];
                count++;
            }
        }
        return interleavedPackets;
    }

    public byte[][] unInterleave(byte[][] packets, int blockSize) {
        // packets loaded into d * d blocks e.g 3 * 3 for 9 packets
        int count = 0;
        //Add to block as long as it isn't full
        byte[][][] block = new byte[blockSize][blockSize][];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                block[i][j] = packets[count];
                count++;
            }
        }

        byte[][][] rotatedBlock = new byte[blockSize][blockSize][];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                rotatedBlock[j][blockSize - 1 - i] = block[i][j];
            }
        }

        byte[][] unInterleavedPackets = new byte[9][];
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
