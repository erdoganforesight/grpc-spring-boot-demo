package demo.client;

public class ClientDemo {

    public static void main(String[] args) {
        String address = "dns:///52.36.31.138:9099"; // t2.micro
        DataSender sender = new DataSender(address, 10);
        for (int i = 0; i < 1; i++) {
            sender.export();
        }
        sender.close();
    }
}
