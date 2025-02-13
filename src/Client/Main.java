package Client;

public class Main {
    public static void main(String[] args) {
        EchoClient.connnectTo(9090).run();
    }
}