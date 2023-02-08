import CMPC3M06.AudioRecorder;
import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;

public class VoiceSender {

    //  Static socket for sending data
    static DatagramSocket sending_socket;
    
    public static void main (String[] args) throws LineUnavailableException {
     
        //***************************************************
        //Port to send to
        int PORT = 55555;
        ///IP ADDRESS to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
                    System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
                    System.exit(0);
        }
        //***************************************************
        
        //***************************************************
        //Open a socket to send from
        //We don't need to know its port number as we never send anything to it.
        //We need the try and catch block to make sure no errors occur.

        try{
            sending_socket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Get a handle to the Standard Input (console) so we can read user input
        //BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //***************************************************

        //***************************************************
        //  Send message to say connection established (Not yet implemented)
        /*
        try{
            byte[] buffer = "Connection established, transmitting audio".getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
            sending_socket.send(packet);
        } catch (IOException e) {
            System.out.println("ERROR: TextSender: Some random IO error occurred!");
            e.printStackTrace();
        }*/
        //***************************************************

        //***************************************************
        //  Begin recording audio
        AudioRecorder recorder = new AudioRecorder();
        //***************************************************

        //***************************************************
        //  Main loop.
        boolean running = true;
        while (running){
            try{
                //  Get audio input
                //  Returns 32 ms (512 byte) audio blocks
                byte[] audioPacket = recorder.getBlock();

                //Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(audioPacket, 512, clientIP, PORT);

                //Send it
                sending_socket.send(packet);
            } catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occurred!");
                e.printStackTrace();
            }
        }
        //Close the socket
        sending_socket.close();
        //***************************************************
    }
} 
