public class ThreadedTextDuplex {
    public static void main(String[] args) {
        int portNum = 55555;
        String ipAddr = "192.168.0.1";
        ThreadedTextReceiver receiver = new ThreadedTextReceiver(portNum);
        ThreadedTextSender sender = new ThreadedTextSender(portNum, ipAddr);
        sender.Start();
        receiver.Start();
    }
}
