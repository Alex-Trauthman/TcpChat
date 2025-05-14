package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: java ChatClient <endereco> <porta> <usuario>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String username = args[2];

        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Conectado a " + host + ":" + port);

            // Thread para receber mensagens
            Thread receiveThread = new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Conexão encerrada: " + e.getMessage());
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Loop para enviar mensagens
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if (!userInput.trim().isEmpty()) {
                    out.println(username + ": " + userInput);
                }
            }

        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}