package swing;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JButton connectButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean isConnected = false;

    public Main() {
        this.frame = new JFrame("Chat TCP");
        this.frame.setLayout(new BoxLayout(this.frame, BoxLayout.Y_AXIS));
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setSize(400, 220);
        this.frame.setLayout(new BorderLayout());

        JPanel connectPanel = new JPanel();
        connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.Y_AXIS));
        connectPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.hostField = new JTextField("localhost");
        this.portField = new JTextField("99");
        this.userField = new JTextField("user");
        this.connectButton = new JButton("Conectar");

        connectPanel.add(Box.createVerticalStrut(5));
        connectPanel.add(new JLabel("Endereço:"));
        connectPanel.add(this.hostField);

        connectPanel.add(Box.createVerticalStrut(5));
        connectPanel.add(new JLabel("Porta:"));
        connectPanel.add(this.portField);

        connectPanel.add(Box.createVerticalStrut(5));
        connectPanel.add(new JLabel("Nome de usuário:"));
        connectPanel.add(this.userField);

        connectPanel.add(Box.createVerticalStrut(5));
        connectPanel.add(new JLabel());
        connectPanel.add(this.connectButton);

        this.frame.getContentPane().add(connectPanel, BorderLayout.CENTER);

        this.connectButton.addActionListener(e -> {
            String host = this.hostField.getText();
            int port;
            try {
                port = Integer.parseInt(this.portField.getText());
                connect(host, port);
            } catch (@SuppressWarnings ("unused") NumberFormatException f) {
                showError("A porta já está sendo usada ou é inválida.");
            }
        });
        this.frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    private void openChatGUI() {
        this.frame.getContentPane().removeAll();
        this.frame.setSize(400, 300);

        Main.chatArea = new JTextArea();
        Main.chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(Main.chatArea);

        this.inputField = new JTextField();
        this.sendButton = new JButton("Enviar");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(this.inputField, BorderLayout.CENTER);
        bottomPanel.add(this.sendButton, BorderLayout.EAST);

        this.frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        this.sendButton.addActionListener(e -> sendMessage());
        this.inputField.addActionListener(e -> sendMessage());

        this.frame.revalidate();
        this.frame.repaint();
    }

    private void connect(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.isConnected = true;
            openChatGUI();
            appendChat("Sistema: Conectado no servidor \"" + host + ":" + port + "\"");
            appendChat("Seu ID: " + this.socket.getLocalPort());
            Thread receiveThread = new Thread(() -> {
                String msg;
                try {
                    while ((msg = this.in.readLine()) != null) {
                        appendChat(msg);
                    }
                } catch (IOException e) {
                    appendChat("Sistema: Conexão fechada: " + e.getMessage());
                }
            });
            receiveThread.start();
        } catch (IOException e) {
            showError("Não foi possível se conectar ao servidor: " + e.getMessage());
        }
    }

    private void sendMessage() {
        if (!this.isConnected) return;
        String message = this.inputField.getText().trim();
        if (message.isEmpty()) return;
        this.out.println(this.userField.getText() + ": " + message);
        this.appendChat(this.userField.getText() + ": " + message);
        this.inputField.setText("");
    }

    private void appendChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
        });
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this.frame, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
