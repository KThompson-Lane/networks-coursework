import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

public class AnalysisListener implements Runnable{
    private final int port;
    private boolean running;
    private DatagramSocket3 receivingSocket;

    //private int PacketCount = 0;
    List<Integer> packetNums;

    List<Integer> lossBursts;

    public AnalysisListener(int portNum) {
        packetNums = new ArrayList<>();
        port = portNum;
        //  Set up Receiving Socket
        try{
            this.receivingSocket = new DatagramSocket3(port);
            //TODO: Investigate what timeout we should have
            receivingSocket.setSoTimeout(1000);
        } catch (SocketException e){
            System.out.println("ERROR: Listener: Could not open UDP socket to listen on.");
            e.printStackTrace();
            System.exit(0);
        }
        //REMOVED audio player
    }
    public void Start()
    {
        //Make a new thread from this class
        Thread thread = new Thread(this);

        //Start the thread
        running = true;
        thread.start();
    }

    @Override
    public void run() {
        while(running)
        {
            //  Receive payload in here
            ReceivePayload();
        }
        //  Close socket then terminates thread
        receivingSocket.close();
    }
    public void ReceivePayload()
    {
        //  First receive packet on UDP socket
        ByteBuffer packetBuffer = ByteBuffer.allocate(514);
        DatagramPacket packet = new DatagramPacket(packetBuffer.array(), 0, 514);

        try {
            receivingSocket.receive(packet);
            int packetNum = packetBuffer.getShort();
            packetNums.add(packetNum);

            System.out.println("Packet Received: " + packetNum);
        } catch (SocketTimeoutException e) {
            //  Handle socket timeout
        } catch (IOException e){
            System.out.println("ERROR: Listener: Some random IO error occurred!");
            e.printStackTrace();
        }

        //System.out.println(packetNums);
        PacketLossRate(); //todo - call this once at the end
    }

    public void averageBurstCount() //WRONG
    {
        int burst = 0;
        int count = 0;
        for (int i = 1; i < packetNums.size(); i++) {
            if(packetNums.get(count) + 1 != packetNums.get(i))
            {
                //
            }
            count++;
        }
    }

    public void PacketLossRate()
    {
        //lost packets / total packets sent (assume 100 sent for now) todo - find way to see total packets sent? Or use last packetNum as total
        int count = 1;
        int lostPackets = 0;
        //for (int num:packetNums) { //todo - change! This doesn't work if packets arrive out of order!
        //    if (num != count) {
        //        lostPackets++;
        //    }
        //    count++;
        //}
        //Collections.sort(packetNums);
        for (int i = 1; i <= 100; i++)
        {
            try {
                if (packetNums.get(i) != count) {
                    lostPackets++;
                    //i--;
                }
                count++;
            }
            catch(IndexOutOfBoundsException e){
                break;
            }
        }

        int lossRate = lostPackets;
        System.out.println(packetNums);
        System.out.println("Loss Rate: " + lossRate + "%");
    }
    public void Terminate()
    {
        running = false;
    }
}
