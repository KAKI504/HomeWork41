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

public class EchoWar {
    private final int port;
    private final Map<String, Command> commands = new HashMap<>();

    private EchoWar(int port) {
        this.port = port;
        initCommands();
    }

    private void initCommands() {
        commands.put("Дата", new Command() {
            @Override
            public String execute(String message) {
                return LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            }
        });

        commands.put("время", new Command() {
            @Override
            public String execute(String message) {
                return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            }
        });

        commands.put("реверс", new Command() {
            @Override
            public String execute(String message) {
                String[] parts = message.split(" ", 2);
                return parts.length > 1 ? new StringBuilder(parts[1]).reverse().toString() : "";
            }
        });

        commands.put("верхний", new Command() {
            @Override
            public String execute(String message) {
                String[] parts = message.split(" ", 2);
                return parts.length > 1 ? parts[1].toUpperCase() : "";
            }
        });

        commands.put("пока", new Command() {
            @Override
            public String execute(String message) {
                return "До свидания!";
            }
        });
    }

    public static EchoWar blindToPort(int port) {
        return new EchoWar(port);
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            System.out.println("Доступные команды: Дата, время, реверс, верхний, пока");

            while (true) {
                try (Socket socket = server.accept()) {
                    System.out.println("Новый клиент подключился!");
                    handle(socket);
                }
            }
        } catch (IOException e) {
            System.out.printf("Вероятнее всего порт %s занят.%n", port);
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
                System.out.printf("Получено: %s%n", message);

                String[] parts = message.split(" ", 2);
                String command = parts[0];

                String response;
                if (commands.containsKey(command)) {
                    response = commands.get(command).execute(message);
                    if (command.equals("пока")) {
                        writer.println(response);
                        System.out.println("Клиент отключился");
                        return;
                    }
                } else {
                    response = new StringBuilder(message).reverse().toString();
                }

                writer.println(response);
            }
        } catch (NoSuchElementException e) {
            System.out.println("Клиент прервал соединение");
        }
    }
}