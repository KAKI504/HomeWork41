package Client;

import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class EchoClient {
    private final int port;
    private final String host;

    private EchoClient(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public static EchoClient connnectTo(int port) {
        return new EchoClient(port, "localhost");
    }

    public void run() {
        System.out.println("для выхода напиши 'bye'");
        System.out.println("Доступные команды: date, time, reverse, upper, bye");

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected to server on port " + port);

            Scanner userInput = new Scanner(System.in, "UTF-8");
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8")
            );

            try (userInput; writer; reader) {
                while (true) {
                    System.out.print("Enter command: ");
                    String message = userInput.nextLine();
                    writer.println(message);

                    String response = reader.readLine();
                    if (response != null) {
                        System.out.println("Server response: " + response);
                    }

                    if (message.equalsIgnoreCase("bye")) {
                        return;
                    }
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Connection dropped!");
        } catch (IOException e) {
            System.out.printf("Can't connect to %s:%s%n", host, port);
            e.printStackTrace();
        }
    }
}
