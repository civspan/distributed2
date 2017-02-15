import java.util.*;

public class SortObject implements Comparable<SortObject> {   
   
   int sender;
   int time;
   
   public SortObject(int sender, int time) {
      this.time = time;
      this.sender = sender;
   }
   
   public int getTime() {
      return time;
   }
   
   public int getSender() {
      return sender;
   }
   
   public int compareTo(SortObject obj) {
        if( obj.time == this.time ) {
            if(obj.sender < this.sender) 
              return 1;
            else if(obj.sender > this.sender)
                    return -1;      
                 else
                    return 0; // If timestamp and messages are the same, the objects are equal 
        }
        if(obj.time < this.time)
            return 1;
        else
            return -1;
    }
    public static void main(String[] args) {
      List<SortObject> ls = new ArrayList<>();

      SortObject obj = new SortObject(1,1);
      
      System.out.println("\ntest1: " + obj.sender + ", " + obj.time);
      System.out.println("test2: " + obj.getSender() + ", " + obj.getTime());
      
      ls.add(obj);    
      obj = new SortObject(1,2);
      ls.add(obj);
      obj = new SortObject(2,1);
      ls.add(obj);
      obj = new SortObject(2,2);
      ls.add(obj);
      obj = new SortObject(1,3);
      ls.add(obj);
      obj = new SortObject(3,2);
      ls.add(obj);
      obj = new SortObject(2,1);
      ls.add(obj);
      obj = new SortObject(2,2);
      ls.add(obj);
      Collections.sort(ls);      
      
      System.out.println("Sorted objects(sender, time):");
      for(SortObject ob : ls) {
          System.out.println(ob.sender + ", " + ob.time);
      }

   } 

}
