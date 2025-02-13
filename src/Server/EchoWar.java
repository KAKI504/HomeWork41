package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

interface Command {
    String execute(String message);
}

public class EchoWar {
    private final int port;
    private final Map<String, Command> commands = new HashMap<>();

    private EchoWar(int port) {
        this.port = port;
        initCommands();
    }

    private void initCommands() {
        commands.put("date", msg -> LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        commands.put("time", msg -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        commands.put("reverse", msg -> {
            String[] parts = msg.split(" ", 2);
            return parts.length > 1 ? new StringBuilder(parts[1]).reverse().toString() : "";
        });
        commands.put("upper", msg -> {
            String[] parts = msg.split(" ", 2);
            return parts.length > 1 ? parts[1].toUpperCase() : "";
        });
        commands.put("bye", msg -> "Goodbye!");
    }

    public static EchoWar blindToPort(int port) {
        return new EchoWar(port);
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            System.out.println("Available commands: date, time, reverse, upper, bye");

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

                String[] parts = message.split(" ", 2);
                String command = parts[0].toLowerCase();

                String response;
                if (commands.containsKey(command)) {
                    response = commands.get(command).execute(message);
                    if (command.equals("bye")) {
                        writer.println(response);
                        System.out.println("Client disconnected");
                        return;
                    }
                } else {
                    response = message;
                }

                writer.println(response);
            }
        } catch (NoSuchElementException e) {
            System.out.println("Client dropped connection");
        }
    }
}