import java.io.*;
import java.net.*;

public class ThreadedTextSender extends ThreadedTransmission {

    BufferedReader in;

    public ThreadedTextSender(int PORT, String clientAddress) {
        super(PORT, clientAddress);
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void TransmitPayload() {
        try{
            //Read in a string from the standard input
            String str = in.readLine();

            //Convert it to an array of bytes
            byte[] buffer = str.getBytes();

            //Make a DatagramPacket from it, with client address and port number
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);

            //Send it
            sendingSocket.send(packet);

            //The user can type EXIT to quit
            if (str.equals("EXIT")){
                this.Terminate();
            }
        }catch (IOException e){
            System.out.println("ERROR: TextSender: Some random IO error occured!");
            e.printStackTrace();
        }
    }
}
