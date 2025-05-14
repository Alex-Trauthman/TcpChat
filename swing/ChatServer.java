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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class Main {
    private JFrame frame;
    private static JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private ServerSocket serverSocket;

    private static final int PORT = 99;
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public Main() {
        openLoggerGUI();
        new Thread(this::startServer).start();
    }

    @SuppressWarnings ("resource")
    private void startServer() {
        try {
            this.serverSocket = new ServerSocket(PORT);
            appendLog("Servidor iniciado na porta " + PORT);

            while (true) {
                Socket socket = this.serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                appendLog("Cliente " + socket.getRemoteSocketAddress() + " conectado.");
                new Thread(handler).start();
            }
        } catch (IOException e) {
            appendLog("Sistema: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    private void openLoggerGUI() {
        this.frame = new JFrame("Servidor TCP");
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setSize(500, 400);

        Main.chatArea = new JTextArea();
        Main.chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(Main.chatArea);

        scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));

        this.inputField = new JTextField();
        this.sendButton = new JButton("Enviar");

        this.inputField.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));
        this.sendButton.setMaximumSize(new java.awt.Dimension(100, 30));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.add(this.inputField);
        inputPanel.add(this.sendButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(scrollPane);
        mainPanel.add(inputPanel);

        this.sendButton.addActionListener(e -> {
            String message = this.inputField.getText().trim();
            if (message.isEmpty()) return;
            broadcast("Servidor: " + message, null);
            appendLog("Servidor: " + message);
            this.inputField.setText("");

        });

        this.inputField.addActionListener(e -> this.sendButton.doClick());

        this.frame.setContentPane(mainPanel);
        this.frame.setVisible(true);
    }

    private static void appendLog(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    static void broadcast(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                    client.out.flush();
                }
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(socket.getOutputStream(), true);
                broadcast("Sistema: Cliente " + socket.getPort() + " conectou.", this);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        public void run() {
            try {
                String message;
                this.out.println("Sistema: Conectado ao servidor.");
                while ((message = this.in.readLine()) != null) {
                    System.out.println("Mensagem: " + message);
                    appendLog("[" + this.socket.getRemoteSocketAddress() + "] - " + message);
                    broadcast(message, this);
                }
            } catch (IOException e) {
                System.out.println("Cliente " + this.socket.getRemoteSocketAddress() + " desconectado, motivo: " + e.getMessage());
                appendLog("Cliente " + this.socket.getRemoteSocketAddress() + " desconectado, motivo: " + e.getMessage());
            } finally {
                try {
                    this.socket.close();
                    clients.remove(this);
                } catch (@SuppressWarnings ("unused") IOException e) {
                    broadcast("Sistema: " + this.socket.getRemoteSocketAddress() + " saiu.", this);
                }
            }
        }

        void sendMessage(String message) {
            this.out.println(message);
        }
    }
}
