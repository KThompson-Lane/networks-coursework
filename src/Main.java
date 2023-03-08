import Security.KeyExchanger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        InetAddress destinationAddress;
        int destinationPort = 55555;
        boolean Host, Encrypt = false, Decrypt = false;
        int socketNum = 1;

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));

        //  Get user to choose whether to run as the host or client
        try {
            System.out.println("Please enter 'host' to run as the host device, or 'client' to run as the client device...");
            if (reader.readLine().equalsIgnoreCase("host")) {
                System.out.println("Running as host!");
                Host = true;
            } else {
                System.out.println("Running as client!");
                Host = false;
            }

            System.out.println("Please enter which UDP socket you want to use (1,2,3,4)");
            switch (reader.readLine()) {
                case "2" -> socketNum = 2;
                case "3" -> socketNum = 3;
                case "4" -> socketNum = 4;
                default -> {
                    System.out.println("Choose one of the following options for encryption, enter 1 for just encryption, enter 2 for just decryption, or 3 for both...");
                    switch (reader.readLine().toLowerCase()) {
                        case "1" -> {
                            System.out.println("Enabling only encryption!");
                            Encrypt = true;
                        }
                        case "2" -> {
                            System.out.println("Enabling only decryption!");
                            Decrypt = true;
                        }
                        default -> {
                            System.out.println("Enabling encryption and decryption!");
                            Encrypt = true;
                            Decrypt = true;
                        }
                    }
                }
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

        Connector connector = new Connector(destinationPort, destinationAddress);

        //1: Set up our connection as either host or client
        if(Host)
            connector.ConnectAsHost();
        else
            connector.ConnectAsClient();

        //  TODO: Consider merging connector and KeyExchanger class
        //  After establishing connection, determine shared secret key
        KeyExchanger keyExchanger = new KeyExchanger(destinationPort, destinationAddress);
        long secretKey;
        if(Host)
            secretKey = keyExchanger.ExchangeAsHost();
        else
            secretKey = keyExchanger.ExchangeAsClient();

        //2: Set up our speak and listener threads
        //  New thread for listen, passing our port number and secret key
        Listener listener = new Listener(destinationPort, secretKey, socketNum, Decrypt);
        //  New thread for speak, passing our IP address and port along with our secret key
        Speaker speaker = new Speaker(destinationPort, destinationAddress, secretKey, socketNum, Encrypt);

        //  Run these threads in a loop
        listener.Start();
        speaker.Start();

        //3: Tear down connection
        //  Kill speak and listen threads
        //  ??
    }
}