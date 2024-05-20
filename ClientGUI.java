import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class ChatClientGUI {

    // Variables globales
    private JFrame chatFrame = new JFrame("Chat Application");
    private JPanel inputPanel = new JPanel();
    private JTextField inputTextField = new JTextField(60);
    private JTextArea messageTextArea = new JTextArea(20, 60);
    private DefaultCaret messageCaret = (DefaultCaret) messageTextArea.getCaret();
    private DefaultListModel<String> userListModel = new DefaultListModel<>();
    private JList<String> userJList = new JList<>(userListModel);
    private JButton sendButton = new JButton("Send");
    private JButton changeNickButton = new JButton("Change Nickname");
    private JScrollPane userScrollPane = new JScrollPane(userJList);
    private String nickname;
    private MySocket mySocket;

    // Constructor
    public ChatClientGUI() {
        configureGUIComponents();
        layoutComponents();
        addEventListeners();
    }

    // Métodos para configurar y distribuir componentes
    private void configureGUIComponents() {
        messageTextArea.setEditable(false);
        messageTextArea.setBackground(Color.LIGHT_GRAY);
        messageTextArea.setLineWrap(true);

        userJList.setLayoutOrientation(JList.VERTICAL);
        userJList.setBackground(Color.GRAY);

        sendButton.setPreferredSize(new Dimension(75, 20));
        changeNickButton.setPreferredSize(new Dimension(150, 20));

        messageCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    private void layoutComponents() {
        inputPanel.add(inputTextField, "Center");
        inputPanel.add(sendButton, "East");
        inputPanel.add(changeNickButton, "East");
        inputPanel.setBackground(Color.DARK_GRAY);

        chatFrame.getContentPane().add(inputPanel, "South");
        chatFrame.getContentPane().add(new JScrollPane(messageTextArea), "Center");
        chatFrame.getContentPane().add(userScrollPane, "West");

        chatFrame.pack();
    }

    private void addEventListeners() {
        sendButton.addActionListener(new SendMessageAction());
        changeNickButton.addActionListener(new ChangeNicknameAction());
        inputTextField.addActionListener(new SendMessageAction());
    }

    // Métodos de interacción del usuario
    private String[] promptForServerIP() {
        return JOptionPane.showInputDialog(chatFrame, "Enter server IP", "localhost:8080").split(":");
    }

    private String promptForNickname() {
        return JOptionPane.showInputDialog(chatFrame, "Enter your nickname:", "Nickname");
    }

    // Método para iniciar la conexión y manejar la comunicación
    private void run() throws IOException {
        String[] serverAddress = promptForServerIP();
        mySocket = new MySocket(serverAddress[0], Integer.parseInt(serverAddress[1]));

        inputTextField.requestFocus();

        while (true) {
            String serverMessage = mySocket.readLine();
            handleMessage(serverMessage);
        }
    }

    private void handleMessage(String message) {
        switch (message.charAt(0)) {
            case '\01':
                nickname = promptForNickname();
                mySocket.println('\u0001' + " " + nickname);
                break;
            case '\03':
                messageTextArea.append(message.substring(1) + "\n");
                break;
            case '\04':
                userListModel.addElement(message.substring(1));
                break;
            case '\05':
                userListModel.removeElement(message.substring(1));
                break;
        }
    }

    // Método main
    public static void main(String[] args) throws Exception {
        ChatClientGUI chatClient = new ChatClientGUI();
        chatClient.chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatClient.chatFrame.setVisible(true);
        chatClient.run();
    }

    // Clases internas para manejar acciones
    private class SendMessageAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = inputTextField.getText();
            if (!message.isEmpty()) {
                mySocket.println(message);
                inputTextField.setText("");
            }
        }
    }

    private class ChangeNicknameAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String newNickname = promptForNickname();
            mySocket.println('\u0001' + " " + newNickname);
        }
    }
}
