import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import CMPC3M06.AudioPlayer;


/**
 * CMPC3M06 Audio Test
 *
 *  This class is designed to test the audio player and recorder.
 *
 * @author Philip Harding
 */
public class AudioReceiver {

    static DatagramSocket receiving_socket;

    public static void main(String args[]) throws Exception {
        //***************************************************
        //Port to open socket on
        int PORT = 55555;
        //***************************************************

        //***************************************************
        //Open a socket to receive from on port PORT

        try {
            receiving_socket = new DatagramSocket(PORT);
            receiving_socket.setSoTimeout(500);
        } catch (SocketException e) {
            System.out.println("ERROR: AudioReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        AudioPlayer player = new AudioPlayer();

        boolean running = true;

        while (running) {

            try {
                //Receive a DatagramPacket
                byte[] buffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 512);

                receiving_socket.receive(packet);

                //Play Audio
                player.playBlock(buffer);

            } catch (SocketTimeoutException e) {
                System.out.println(".");
            } catch (IOException e) {
                System.out.println("ERROR: AudioReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }

        //Close audio output
        player.close();
    }
}