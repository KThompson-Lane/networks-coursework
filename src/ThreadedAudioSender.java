import CMPC3M06.AudioRecorder;
import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;

public class ThreadedAudioSender extends ThreadedTransmission
{
    private AudioRecorder recorder;
    public ThreadedAudioSender(int PORT, String clientAddress) {
        super(PORT, clientAddress);
        try{
            recorder = new AudioRecorder();
        } catch (LineUnavailableException e) {
            System.out.println("ERROR: AudioSender: Could not start audio recorder.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void TransmitPayload() {
        try{
            //  Get audio input
            //  Returns 32 ms (512 byte) audio blocks
            byte[] audioPacket = recorder.getBlock();

            //Make a DatagramPacket from it, with client address and port number
            DatagramPacket packet = new DatagramPacket(audioPacket, 512, clientIP, PORT);
            //Send it
            sendingSocket.send(packet);
        }catch (IOException e){
            System.out.println("ERROR: TextSender: Some random IO error occured!");
            e.printStackTrace();
        }
    }
}
