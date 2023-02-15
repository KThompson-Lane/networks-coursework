import java.io.*;
import java.net.*;
public class ThreadedTextReceiver extends ThreadedReceiver{

    public ThreadedTextReceiver(int PORT) {
        super(PORT);
    }

    @Override
    public void ReceivePayload() {
        try{
            //Receive a DatagramPacket (note that the string cant be more than 80 chars)
            byte[] buffer = new byte[80];
            DatagramPacket packet = new DatagramPacket(buffer, 0, 80);

            receivingSocket.receive(packet);

            //Get a string from the byte buffer
            String str = new String(buffer);
            //Display it
            System.out.print(str + "\n");

            //The user can type EXIT to quit
            if (str.substring(0,4).equals("EXIT")){
                Terminate();
            }
        }  catch (SocketTimeoutException e) {
            //System.out.println(".");
        } catch (IOException e){
            System.out.println("ERROR: TextSender: Some random IO error occured!");
            e.printStackTrace();
        }
    }
}
