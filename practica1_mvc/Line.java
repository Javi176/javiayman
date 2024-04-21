import java.util.ArrayList;
import java.util.Observable;

public class Line extends Observable {
  
  private int pos;
  private boolean insert;
  private ArrayList<Character> line;
  private Console console;

  public Line (){
    line = new ArrayList<>();
    insert = false;
    pos = 0;
    console = new Console();
    this.addObserver(console);
  }

  public void addCharacter(char character){
    if (!insert || pos >= line.size()) {
      line.add(pos, character);      
      System.out.print("\033[@");
    }
    else {
      line.set(pos, character);
    }
    pos++;
    this.setChanged();
    this.notifyObservers(character);     
  }

  public void home(){
    while(pos>0){
      this.left();
    }     
  }

  public void end(){
    while(pos<line.size()){
      this.right();
    }
  }

  public void right(){
    if (pos < line.size()){
      pos++;
      this.setChanged();
      this.notifyObservers("\033[C");
    } 
  }

  public void left(){
    if (pos > 0){
      pos--;
      this.setChanged();
      this.notifyObservers("\033[D");
    } 
  }

  public void insert(){    
    insert = !insert;
  }

  public void delete(){
    if(pos < line.size())
    {
      line.remove(pos);
      this.setChanged();
      this.notifyObservers("\033[P");
    }
  }

  public void backSpace(){
    if(pos > 0)
    {
      pos--;      
      line.remove(pos);
      this.left();
      this.delete();
    }    
  }

  @Override
  public String toString(){
    StringBuilder str = new StringBuilder();
    for (char s : line) 
      str = str.append(s);
    return str.toString();
  }
}