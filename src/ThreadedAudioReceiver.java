import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

public class ThreadedAudioReceiver extends ThreadedReceiver{

    private AudioPlayer player;

    public ThreadedAudioReceiver(int PORT){
        super(PORT);
        try{
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            System.out.println("ERROR: AudioSender: Could not start audio player.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void ReceivePayload() {
        try{
            //Receive a DatagramPacket
            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, 0, 512);

            receivingSocket.receive(packet);

            //Play Audio
            player.playBlock(buffer);

        }catch (SocketTimeoutException e) {
            //System.out.println(".");
        }catch (IOException e){
            System.out.println("ERROR: AudioReceiver: Some random IO error occured!");
            e.printStackTrace();
        }
    }
}