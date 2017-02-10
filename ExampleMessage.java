
import mcgui.*;
import java.sql.Timestamp;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleMessage extends Message implements Comparable<ExampleMessage> {
        
    String text;
    Timestamp timestamp;
    boolean ack;
    enum Status {READY,PENDING};
    Status stat = Status.PENDING;
        
    public ExampleMessage(int sender,String text,Timestamp ts) {
        super(sender);
        this.text = text;
        ack = false;
        timestamp = ts;
        
    }

    public ExampleMessage(int sender,String text, boolean ackn) {
        super(sender);
        this.text = text;
        ack = ackn;
    }
    
    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }
    public void setAck(){
      ack = true;
    }
    public void changeStatus(){
      stat = Status.READY;
    }

    public boolean isAck(){
      return ack;
    }

    public int getSender() {
        return sender;
    }

    public Timestamp getTimeStamp(){
      return timestamp;
    }

    public int compareTo(ExampleMessage msg) {
      if( msg.getTimeStamp() == this.timestamp ) {
        if(msg.getSender() < this.sender) {
          return 1;
        }
        else
          return -1;      
      }
      if(msg.getTimeStamp().before(this.timestamp))
        return 1;
      else
        return -1;
    }


    
    public static final long serialVersionUID = 0;
}
