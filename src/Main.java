import Security.Impostor;
import Security.KeyExchanger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        final boolean TestImpostor = false;

        InetAddress destinationAddress;
        int destinationPort = 55555;
        boolean Host, Encrypt = false, Decrypt = false;
        int socketNum = 1;
        boolean interleaving = false;
        boolean compensate = false;

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
                case "2" -> {
                    socketNum = 2;
                    interleaving = true;
                }
                case "3" -> {
                    socketNum = 3;
                    interleaving = true;
                }
                case "4" -> {
                    socketNum = 4;
                    compensate = true;
                }
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

            // Interleaving
            //System.out.println("Choose one of the following options for interleaving, enter 1 for just interleaving enabled, enter 2 for interleaving disabled");
            //if (reader.readLine().equalsIgnoreCase("1")) {
            //    System.out.println("Enabled interleaving!");
            //    interleaving = true;
            //} else {
            //    System.out.println("Disabled interleaving!");
            //    interleaving = false;
            //}
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
        Listener listener = new Listener(destinationPort, secretKey, socketNum, interleaving, compensate, Decrypt);
        //  New thread for speak, passing our IP address and port along with our secret key
        Speaker speaker = new Speaker(destinationPort, destinationAddress, secretKey, socketNum, interleaving, compensate, Encrypt);

        //  Run these threads in a loop
        listener.Start();
        speaker.Start();

        if(TestImpostor)
            new Impostor().Start();
        
        //3: Tear down connection
        //  Kill speak and listen threads
        //  ??
    }
}