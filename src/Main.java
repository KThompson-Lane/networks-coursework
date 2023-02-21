public class Main {
    public static void main(String[] args) {
        //KeyExchanger exchanger = new KeyExchanger();
        //exchanger.ExchangeAsClient();

        int portNum = 55555;
        String ipAddr = "192.168.0.18";
        Connector connector = new Connector(portNum, ipAddr);
    }
}