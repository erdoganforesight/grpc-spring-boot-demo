package demo.client;

public class SyncClientDemo {

    public static void main(String[] args) {
        // String address = "dns:///52.36.31.138:9099"; // t2.micro
        String address = "dns:///service.erdoganyesil.link:9099"; // ts small without print
        DataSenderSync sender = new DataSenderSync(address, 1, 1000);
        sender.export();
    }
}
