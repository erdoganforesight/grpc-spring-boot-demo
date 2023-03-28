package demo.client;

public class ClientDemo {

    public static void main(String[] args) {
        String address = "dns:///service.erdoganyesil.link:9095"; // ts small without print
        DataSender sender = new DataSender(address, 100);
        for (int i = 0; i < 10; i++) {
            sender.export();
        }
        sender.close();
    }
}
