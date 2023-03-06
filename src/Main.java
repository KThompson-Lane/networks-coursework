import Security.KeyExchanger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        InetAddress destinationAddress;
        int destinationPort = 55555;
        boolean Host;

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));

        //  Get user to choose whether to run as the host or client
        //  TODO: Get user to choose to enable encryption or not
        try {
            System.out.println("Please enter 'host' to run as the host device, or 'client' to run as the client device...");
            switch (reader.readLine().toLowerCase())
            {
                case "host":
                    System.out.println("Running as host!");
                    Host = true;
                    break;
                default:
                    System.out.println("Running as client!");
                    Host = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //  Get user to enter destination address
        while(true)
        {
            try{
                System.out.println("Please enter the address of the device you want to connect to...");
                String ipAddr =  reader.readLine();
                destinationAddress = InetAddress.getByName(ipAddr);
                System.out.println("Attempting connection with '"+ipAddr+"'...");
                break;
            }catch (IOException e) {
                System.out.println("Invalid destination address...");
            }
        }

        Connector connector = new Connector(destinationPort, destinationAddress.getHostName());

        //1: Set up our connection as either host or client
        if(Host)
            connector.ConnectAsHost();
        else
            connector.ConnectAsClient();

        //  TODO: Consider merging connector and KeyExchanger class
        //  After establishing connection, determine shared secret key
        KeyExchanger keyExchanger = new KeyExchanger(destinationPort, destinationAddress.getHostName());
        long secretKey;
        if(Host)
            secretKey = keyExchanger.ExchangeAsHost();
        else
            secretKey = keyExchanger.ExchangeAsClient();

        //2: Set up our speak and listener threads
        //  New thread for listen, passing our port number and secret key
        Listener listener = new Listener(destinationPort, secretKey);
        //  New thread for speak, passing our IP address and port along with our secret key
        Speaker speaker = new Speaker(destinationPort, destinationAddress.getHostName(), secretKey);

        //  Run these threads in a loop
        listener.Start();
        speaker.Start();

        //3: Tear down connection
        //  Kill speak and listen threads
        //  ??
    }
}