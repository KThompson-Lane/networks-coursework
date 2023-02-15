public class ThreadedAudioDuplex {
    public static void main(String[] args) {
        int portNum = 55555;
        String ipAddr = "192.168.0.1";
        ThreadedAudioReceiver receiver = new ThreadedAudioReceiver(portNum);
        ThreadedAudioSender sender = new ThreadedAudioSender(portNum, ipAddr);
        sender.Start();
        receiver.Start();
    }
}
