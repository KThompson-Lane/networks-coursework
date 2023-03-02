import Security.KeyExchanger;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //TODO:  Take in port, IP and host as Args maybe?
        int portNum = 55555;
        String ipAddr = "192.168.0.17";
        boolean Host = true;
        Scanner in = new Scanner(System.in);
        int socketNum = 1;

        Connector connector = new Connector(portNum, ipAddr);

        //1: Set up our connection as either host or client
        if(Host)
            connector.ConnectAsHost();
        else
            connector.ConnectAsClient();

        //  TODO: Consider merging connector and KeyExchanger class
        //  After establishing connection, determine shared secret key
        KeyExchanger keyExchanger = new KeyExchanger(portNum, ipAddr);
        long secretKey;
        //todo - ensure int
        if(Host)
            secretKey = keyExchanger.ExchangeAsHost();

        else
            secretKey = keyExchanger.ExchangeAsClient();

        System.out.println("What channel would you like to use? 1,2,3,4 : ");
        socketNum = Integer.parseInt(in.nextLine()); //todo - ensure int

        in.close();

        //2: Set up our speak and listener threads
        //  New thread for listen, passing our port number and secret key
        Listener listener = new Listener(portNum, secretKey, socketNum);
        //  New thread for speak, passing our IP address and port along with our secret key
        Speaker speaker = new Speaker(portNum, ipAddr, secretKey, socketNum);

        //  Run these threads in a loop
        listener.Start();
        speaker.Start();

        //3: Tear down connection
        //  Kill speak and listen threads
        //  ??
    }
}