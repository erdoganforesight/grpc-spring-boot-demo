package demo.client;

public class SyncClientDemo {

    public static void main(String[] args) {
        String address = "dns:///52.40.110.233:9099"; // t2.micro
        //String address = "dns:///127.0.0.1:9099"; // ts small without print
        DataSenderSync sender = new DataSenderSync(address, 1, 1000);
        sender.export();
    }
}
