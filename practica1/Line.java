import java.util.ArrayList;

public class Line {

    boolean ins;
    ArrayList<Character> line;
    int pos;
    
    public line () {
        line = new ArrayList<>();
        insert = false;
        position = 0;
    }

    public void addCharacter(char character) {
        if(!ins || pos >= line.size()){
            line.add(pos, character);
            System.out.print("\033[@");
        }
        else{
            line.set(pos, character);
        }
        System.out.print(character);
        pos++;
    }

    public void home() {
        if pos > 0) {
            System.out.print("\033[" + pos + "D");
            position = 0;
        }
    }

    public void end() {
        if(pos < line.size()) {
            System.out.print("\033[" + (line.size() - pos) + "C");
            pos = line.size();
        }
    }

    public void right() {
        if (pos < line.size()){
            pos++;
            System.out.print("\033[C");
        }
    }
    public void left() {
        if (pos > 0){
            pos--;
            System.out.print("\033[D");
        }
    }

    public void delete() {
        if(position < line.size()) {
            line.remove(pos);
            System.out.print("\033[P");
        }
    }

    public void backSpace() {
        if (pos > 0){
            pos--;
            line.remove(pos):
            System.out.print("\033[D");
            System.out.print("\033[P");

        }        
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (char s : line)
        str = str.append(s);
        return str.toString();
        kjhfukgyuigu
        


    }

}