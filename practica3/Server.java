import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.IOException;
import java.util.*;

public class Server implements Runnable {
    private final int serverPort;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(256);
    private HashMap<SocketChannel, String> connectedUsers;

    private final ByteBuffer welcomeBuffer = ByteBuffer.wrap("Welcome!\n".getBytes());

    Server(int port) throws IOException {
        this.serverPort = port;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.connectedUsers = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            System.out.println("Server starting on port " + this.serverPort);
            while (this.serverSocketChannel.isOpen()) {
                selector.select();
                Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) handleAccept(key);
                    if (key.isReadable()) handleRead(key);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException, server on port " + this.serverPort + " terminating. Stack trace:");
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = socketChannel.socket().getInetAddress().toString() + ":" + socketChannel.socket().getPort();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, address);
        socketChannel.write(welcomeBuffer);
        welcomeBuffer.rewind();
        this.connectedUsers.put(socketChannel, address);

        sendConnectedClients(socketChannel);
        broadcastMessage('\u0004' + " " + this.connectedUsers.get(socketChannel) + "\n");
        socketChannel.write(ByteBuffer.wrap(('\u0001' + " \n").getBytes()));

        System.out.println("Accepted connection from: " + this.connectedUsers.get(socketChannel));
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        StringBuilder messageBuilder = new StringBuilder();
        buffer.clear();
        int bytesRead;
        while ((bytesRead = socketChannel.read(buffer)) > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            messageBuilder.append(new String(bytes));
            buffer.clear();
        }
        String message;
        if (bytesRead < 0) {
            message = this.connectedUsers.get(socketChannel) + " left the chat.\n";
            socketChannel.close();
            broadcastMessage('\u0005' + " " + this.connectedUsers.get(socketChannel) + "\n");
            this.connectedUsers.remove(socketChannel);
        } else if (messageBuilder.toString().charAt(0) == '\u0001') {
            String oldNick = this.connectedUsers.get(socketChannel);
            handleNicknameChange(socketChannel, messageBuilder.toString().substring(1).replace("\n", ""));
            message = oldNick + " has changed nickname to " + this.connectedUsers.get(socketChannel) + "\n";
        } else {
            message = this.connectedUsers.get(socketChannel) + ": " + messageBuilder.toString().replace("\n", "") + "\n";
        }
        System.out.print(message);
        broadcastMessage('\u0003' + " " + message);
    }

    private void handleNicknameChange(SocketChannel socketChannel, String newNick) throws IOException {
        broadcastMessage('\u0005' + " " + this.connectedUsers.get(socketChannel) + "\n");
        this.connectedUsers.remove(socketChannel);
        this.connectedUsers.put(socketChannel, newNick);
        broadcastMessage('\u0004' + " " + this.connectedUsers.get(socketChannel) + "\n");
    }

    private void broadcastMessage(String message) throws IOException {
        ByteBuffer messageBuffer = ByteBuffer.wrap(message.getBytes());
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                socketChannel.write(messageBuffer);
                messageBuffer.rewind();
            }
        }
    }

    private void sendConnectedClients(SocketChannel socketChannel) throws IOException {
        this.connectedUsers.forEach((key, value) -> {
            if (!value.equals(this.connectedUsers.get(socketChannel))) {
                ByteBuffer messageBuffer = ByteBuffer.wrap(('\u0004' + " " + value + "\n").getBytes());
                try {
                    socketChannel.write(messageBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("===============================");
            System.err.println("Start the server like this:");
            System.err.println("  > java Server <port>");
            System.err.println("===============================");
            System.exit(1);
        }

        Server server = new Server(Integer.parseInt(args[0]));
        (new Thread(server)).start();
