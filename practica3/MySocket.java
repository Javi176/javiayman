import java.net.*;
import java.io.*;

class MySocket {

    protected Socket socket;
    protected BufferedReader reader;
    protected PrintWriter writer;

    public MySocket(String host, int port) {
        try {
            socket = new Socket(host, port);
            initializeStreams();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeStreams() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void println(String message) {
        writer.println(message);
    }

    public void writeInt(int value) {
        println(Integer.toString(value));
    }

    public void writeBoolean(boolean value) {
        println(Boolean.toString(value));
    }

    public void writeChar(char character) {
        println(Character.toString(character));
    }

    public void close() {
        try {
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
