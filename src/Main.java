public class Main {
    public static void main(String[] args) {
        //TODO:  Take in port, IP and host as Args maybe?
        int portNum = 55555;
        String ipAddr = "192.168.0.18";
        boolean Host = false;
        Connector connector = new Connector(portNum, ipAddr);

        //1: Set up our connection as either host or client
        //TODO: Make the connect as host return security information
        if(Host)
            connector.ConnectAsHost();
        else
            connector.ConnectAsClient();

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