import java.net.*;
import java.io.*;

class MySocket {

    // Variables protegidas
    protected Socket socket;
    protected BufferedReader bufferedReader;
    protected PrintWriter printWriter;

    // Constructor para crear un nuevo socket con dirección y puerto
    public MySocket(String host, int port) {
        try {
            socket = new Socket(host, port);
            initStreams();
        } catch (IOException e) {
            e.printStackTrace(); // Mejor manejo de excepciones
        }
    }

//    Constructor para usar un socket existente (comentado en el original)
//    public MySocket(Socket socket) {
//        this.socket = socket;
//        initStreams();
//    }

    // Inicialización de los flujos de entrada y salida
    private void initStreams() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace(); // Mejor manejo de excepciones
        }
    }

    // Métodos de lectura
    public String readLine() {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace(); // Mejor manejo de excepciones
            return null;
        }
    }

    public int readInt() {
        return Integer.parseInt(readLine());
    }

    public boolean readBoolean() {
        return Boolean.parseBoolean(readLine());
    }

    public char readChar() {
        return readLine().charAt(0);
    }

    // Métodos de escritura
    public void println(String message) {
        printWriter.println(message);
    }

    public void writeInt(int i) {
        println(Integer.toString(i));
    }

    public void writeBoolean(boolean b) {
        println(Boolean.toString(b));
    }

    public void writeChar(char c) {
        println(Character.toString(c));
    }

    // Método para cerrar el socket y los streams
    public void close() {
        try {
            bufferedReader.close();
            printWriter.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace(); // Mejor manejo de excepciones
        }
    }
}
