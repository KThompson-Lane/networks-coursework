public class ThreadedTextDuplex {
    public static void main(String[] args) throws InterruptedException {
        int portNum = 55555;
        String ipAddr = "192.168.0.1";
        ThreadedTextReceiver receiver = new ThreadedTextReceiver(portNum);
        ThreadedTextSender sender = new ThreadedTextSender(portNum, ipAddr);
        sender.Start();
        receiver.Start();
        while (sender.IsRunning())
        {
            //  Do nothing
            Thread.sleep(1000);
        }
        receiver.Terminate();
    }
}
