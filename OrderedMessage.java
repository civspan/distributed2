
import mcgui.*;
import java.sql.Timestamp;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class OrderedMessage extends Message implements Comparable<OrderedMessage> {
        
    String text;
    Timestamp timestamp;
    boolean ack;
    int orgId;
    boolean[] ackArray;
     
     
    /**
     *  Constructor for message carrying object of class OrderedMessage
     *
     */    
    public OrderedMessage(int sender, Timestamp ts, boolean[] ackArr, String text) {
        super(sender);
        this.text = text;
        ack = false;
        timestamp = ts;
        ackArray = ackArr;
    }

    /**
     *  Constructor for acknowledgement object of class OrderedMessage
     *
     */    
    public OrderedMessage(int sender, Timestamp originalTimestamp, int originalSender) {
        super(sender);
        orgId = originalSender;
        timestamp = originalTimestamp;
        ack = true;
    }
    
    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }
    
    public void isAck(){
        return ack;
    }
    
    /**
     * Checks if a message is acknowledged by all hosts.
     * Returns true if that is the case, otherwise false.
     */
    public boolean isAllAcked(){
      for(boolean ack : ackArray) {
        if(!ack)
          return false;
      }
      return true;
    }

    public int getSender() {
        return sender;
    }
    
    public int getOriginalSender(){
        return orgId;
    }

    public Timestamp getTimeStamp(){
      return timestamp;
    }

    /**
     * Compares two OrderedMessage objects. Breaks ties
     * in timestamp by id. Lower id first.
     */
    @Override
    public int compareTo(OrderedMessage msg) {
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
