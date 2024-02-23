import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EditableBufferedReader extends BufferedReader {

    InputStreamReader inputStreamReader;
    public static final int RIGHT = 1;
    public static final int LEFT = 2;
    public static final int HOME = 3;
    public static final int END = 4;
    public static final int INS = 5;
    public static final int DEL = 6;


    public EditableBufferedReader(InputStreamReader inputStreamReader){

        super(inputStreamReader);
        this.inputStreamReader = inputStreamReader;
    }

    public static void setRaw(){
        try{
            Runtime.getRuntime().exec(new String [] { "/bin/sh", "-c", "stty echo raw </dev/tty" });
        } catch (IOException e){
            e.printStackTrace();
        }

    }
    public static void unsetRaw(){
        try{
            Runtime.getRuntime().exec(new String [] { "/bin/sh", "-c", "stty echo cooked </dev/tty" });
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public int read() throws IOException{
       int a;

       switch(a = super.read()){
        case '\033':
          super.read();
          switch(a = super.read()){
            case '2':
            super.read();
            return INS;
            case '3':
            super.read();
            return DEL;
            case 'C':
            return RIGHT;
            case 'D':
            return LEFT;
            case 'H':
            return HOME;
            case 'F':
            return END;
          }
          default:
            return a;
       }



    }
