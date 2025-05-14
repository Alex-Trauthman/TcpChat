package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ChatServer {
    private static final int PORT = 99;
    private static final List<ClientHandler> clients =
        Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }

    private void start() {
        // Thread para ler comandos do servidor via terminal
        new Thread(this::handleConsoleInput).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Servidor iniciado na porta " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                log("Cliente " + socket.getRemoteSocketAddress() + " conectado.");
                new Thread(handler).start();
            }
        } catch (IOException e) {
            log("Erro no servidor: " + e.getMessage());
        }
    }

    private void handleConsoleInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (!scanner.hasNextLine()) continue;
            String message = scanner.nextLine().trim();
            if (message.isEmpty()) continue;
            ChatServer.broadcast("Servidor: " + message, null);
            log("Servidor (broadcast): " + message);
        }
    }

    private static void broadcast(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
            sendMessage("Sistema: Conectado ao servidor.");
            ChatServer.broadcast("Sistema: Cliente " + socket.getPort() + " entrou.", this);
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    log("[" + socket.getRemoteSocketAddress() + "] " + message);
                    ChatServer.broadcast(message, this);
                }
            } catch (IOException e) {
                log("Cliente " + socket.getRemoteSocketAddress()
                    + " desconectado: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                clients.remove(this);
                ChatServer.broadcast("Sistema: Cliente "
                    + socket.getPort() + " saiu.", this);
            }
        }

        void sendMessage(String message) {
            out.println(message);
        }
    }
}
