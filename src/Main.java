import Security.KeyExchanger;

public class Main {
    public static void main(String[] args) {
        //TODO:  Take in port, IP and host as Args maybe?
        int portNum = 55555;
        String ipAddr = "192.168.0.18";
        boolean Host = false;

        //TODO: Consider merging connector and KeyExchanger class
        Connector connector = new Connector(portNum, ipAddr);
        KeyExchanger keyExchanger = new KeyExchanger(portNum, ipAddr);

        long secretKey;

        //1: Set up our connection as either host or client and establish a shared secret key
        if(Host)
        {
            connector.ConnectAsHost();
            //  After establishing connection, exchange keys
            secretKey = keyExchanger.ExchangeAsHost();
        }
        else{
            connector.ConnectAsClient();
            //  After establishing connection, exchange keys
            secretKey = keyExchanger.ExchangeAsClient();
        }

        //2: Set up our speak and listener threads
        //  New thread for listen, passing our port number
        Listener listener = new Listener(portNum);
        //  New thread for speak, passing our IP address and port
        Speaker speaker = new Speaker(portNum, ipAddr);

        //  Run these threads in a loop
        listener.Start();
        speaker.Start();

        //3: Tear down connection
        //  Kill speak and listen threads
        //  ??
    }
}