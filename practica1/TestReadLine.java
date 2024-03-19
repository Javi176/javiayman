import java.io.*;
class TestReadLine {
  public static void main(String[] args) {
    InputStreamReader input=new InputStreamReader(System.in);
    EditableBufferedReader in = new EditableBufferedReader(input);
    String str = null ;
    try {
      str = in.readLine();
    } catch (IOException e) { e.printStackTrace(); }
    System.out.println("\nline is: " + str);
  }
}
