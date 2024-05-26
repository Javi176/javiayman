import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class ClientGUI {

    JFrame chatFrame = new JFrame("Chat");
    JPanel inputPanel = new JPanel();
    JTextField inputTextField = new JTextField(60);
    JTextArea chatTextArea = new JTextArea(20, 60);
    DefaultCaret textAreaCaret = (DefaultCaret) chatTextArea.getCaret();
    DefaultListModel<String> userListModel = new DefaultListModel<>();
    JList<String> userList = new JList<>(userListModel);
    JButton sendButton = new JButton("Send");
    JButton changeNickButton = new JButton("Change Nickname");
    JScrollPane userListScrollPane = new JScrollPane(userList);
    String nickname;
    MySocket chatSocket;

    public ClientGUI() {
        chatTextArea.setEditable(false);
        chatTextArea.setBackground(Color.LIGHT_GRAY);
        chatTextArea.setLineWrap(true);

        userList.setLayoutOrientation(JList.VERTICAL);
        userList.setBackground(Color.GRAY);

        sendButton.setPreferredSize(new Dimension(75, 20));
        sendButton.addActionListener(new SendMessage());

        changeNickButton.setPreferredSize(new Dimension(150, 20));
        changeNickButton.addActionListener(new ChangeNickname());

        inputTextField.addActionListener(new SendMessage());

        inputPanel.add(inputTextField, "Center");
        inputPanel.add(sendButton, "East");
        inputPanel.add(changeNickButton, "East");
        inputPanel.setBackground(Color.DARK_GRAY);

        chatFrame.getContentPane().add(inputPanel, "South");
        chatFrame.getContentPane().add(new JScrollPane(chatTextArea), "Center");
        chatFrame.getContentPane().add(userListScrollPane, "West");

        textAreaCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        chatFrame.pack();
    }

    private String[] requestServerIP() {
        return JOptionPane.showInputDialog(chatFrame, "Enter server IP", "localhost:8080").split(":");
    }

    private String requestNickname() {
        return JOptionPane.showInputDialog(chatFrame, "Write your nick:", "Nick");
    }

    private void start() throws IOException {
        String[] serverAddress = requestServerIP();
        chatSocket = new MySocket(serverAddress[0], Integer.parseInt(serverAddress[1]));

        inputTextField.requestFocus();

        while (true) {
            String line = chatSocket.readLine();
            System.out.println(line);

            switch (line.charAt(0)) {
                case '\01':
                    nickname = requestNickname();
                    chatSocket.println('\u0001' + " " + nickname);
                    break;
                case '\03':
                    chatTextArea.append(line.substring(1) + "\n");
                    break;
                case '\04':
                    userListModel.addElement(line.substring(1));
                    System.out.println(userListModel);
                    break;
                case '\05':
                    userListModel.removeElement(line.substring(1));
                    break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ClientGUI client = new ClientGUI();
        client.chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.chatFrame.setVisible(true);
        client.start();
    }

    public class SendMessage implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = inputTextField.getText();
            if (!message.isEmpty()) {
                chatSocket.println(message);
                inputTextField.setText("");
            }
        }
    }

    public class ChangeNickname implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String newNickname = requestNickname();
            chatSocket.println('\u0001' + " " + newNickname);
        }
    }
}
