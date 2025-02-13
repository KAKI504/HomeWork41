package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class EchoWar {
    private final int port;

    private EchoWar(int port) {
        this.port = port;
    }

    public static EchoWar blindToPort(int port) {
        return new EchoWar(port);
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                try (Socket socket = server.accept()) {
                    System.out.println("New client connected!");
                    handle(socket);
                }
            }
        } catch (IOException e) {
            System.out.printf("Вероятное всего порт %s занят.%n", port);
            e.printStackTrace();
        }
    }

    private void handle(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
        PrintWriter writer = new PrintWriter(outputStream, true);

        try (Scanner scanner = new Scanner(isr); writer) {
            while (true) {
                String message = scanner.nextLine().strip();
                System.out.printf("Got: %s%n", message);

                String reversed = new StringBuilder(message).reverse().toString();
                writer.println(reversed);

                if (message.equalsIgnoreCase("bye")) {
                    writer.println("!eyb");
                    System.out.println("Client disconnected");
                    return;
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Client dropped connection");
        }
    }
}