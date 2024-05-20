import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.IOException;
import java.util.*;

public class Server implements Runnable {
    // Variables privadas
    private final int port;
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(256);
    private final HashMap<SocketChannel, String> userMap;
    private final ByteBuffer welcomeBuffer = ByteBuffer.wrap("Welcome!\n".getBytes());

    // Constructor
    public Server(int port) throws IOException {
        this.port = port;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.userMap = new HashMap<>();
    }

    // Método run para iniciar el servidor
    @Override
    public void run() {
        try {
            System.out.println("Server starting on port " + this.port);
            while (this.serverSocketChannel.isOpen()) {
                selector.select();
                Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) handleAccept(key);
                    if (key.isReadable()) handleRead(key);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException, server on port " + this.port + " terminating. Stack trace:");
            e.printStackTrace();
        }
    }

    // Manejo de nueva conexión aceptada
    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, address);
        socketChannel.write(welcomeBuffer);
        welcomeBuffer.rewind();
        this.userMap.put(socketChannel, address);
        sendConnectedClients(socketChannel);
        broadcast('\u0004' + " " + this.userMap.get(socketChannel) + "\n");
        socketChannel.write(ByteBuffer.wrap(('\u0001' + " \n").getBytes()));
        System.out.println("Accepted connection from: " + this.userMap.get(socketChannel));
    }

    // Manejo de lectura de datos desde el cliente
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        StringBuilder stringBuilder = new StringBuilder();
        buffer.clear();
        int read = 0;
        while ((read = socketChannel.read(buffer)) > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            stringBuilder.append(new String(bytes));
            buffer.clear();
        }

        String message;
        if (read < 0) {
            message = this.userMap.get(socketChannel) + " left the chat.\n";
            socketChannel.close();
            broadcast('\u0005' + " " + this.userMap.get(socketChannel) + "\n");
            this.userMap.remove(socketChannel);
        } else if (stringBuilder.toString().charAt(0) == '\u0001') {
            String oldNick = this.userMap.get(socketChannel);
            handleNickChanges(socketChannel, stringBuilder.toString().substring(1).replace("\n", ""));
            message = oldNick + " has changed nickname to " + this.userMap.get(socketChannel) + "\n";
        } else {
            message = this.userMap.get(socketChannel) + ": " + stringBuilder.toString().replace("\n", "") + "\n";
        }
        System.out.print(message);
        broadcast('\u0003' + " " + message);
    }

    // Manejo de cambio de apodo de usuario
    private void handleNickChanges(SocketChannel socketChannel, String newNick) throws IOException {
        broadcast('\u0005' + " " + this.userMap.get(socketChannel) + "\n");
        this.userMap.remove(socketChannel);
        this.userMap.put(socketChannel, newNick);
        broadcast('\u0004' + " " + this.userMap.get(socketChannel) + "\n");
    }

    // Método para enviar un mensaje a todos los clientes conectados
    private void broadcast(String message) throws IOException {
        ByteBuffer messageBuffer = ByteBuffer.wrap(message.getBytes());
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                socketChannel.write(messageBuffer);
                messageBuffer.rewind();
            }
        }
    }

    // Método para enviar información de clientes conectados a un nuevo cliente
    private void sendConnectedClients(SocketChannel socketChannel) throws IOException {
        this.userMap.forEach((key, value) -> {
            if (!value.equals(this.userMap.get(socketChannel))) {
                ByteBuffer messageBuffer = ByteBuffer.wrap(('\u0004' + " " + value + "\n").getBytes());
                try {
                    socketChannel.write(messageBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error sending connected clients.");
                }
            }
        });
    }

    // Método principal para iniciar el servidor
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("===============================");
            System.err.println("Start the server as follows:");
            System.err.println("  > java Server <port>");
            System.err.println("===============================");
            System.exit(1);
        }

        Server server = new Server(Integer.parseInt(args[0]));
        (new Thread(server)).start();
    }
}
