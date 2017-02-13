
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
    int ackSender;
    boolean[] ackArray;
     
     
    /**
     *  Constructor for message carrying object of class OrderedMessage
     *
     */    
    public OrderedMessage(int sender, Timestamp ts, String text , boolean[] ackArr) {
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
    public OrderedMessage(int sender, Timestamp originalTimestamp, boolean[] ackArr, int ackSender) {
        super(sender);
        this.ackSender = ackSender;
        timestamp = originalTimestamp;
        ack = true;
        ackArray = ackArr;
    }
    
    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }
    
    public boolean isAck(){
        return ack;
    }
    
    /**
     * Checks if a message is acknowledged by all hosts.
     * Returns true if that is the case, otherwise false.
     */
    public boolean isAllAcked(){
      if(ackArray.length() == 0 )
        return false;
      for(boolean ack : ackArray) {
        if(!ack)
          return false;
      }
      return true;
    }

    public int getSender() {
        return sender;
    }

    public int getAckSender(){
        return ackSender;
    }

    public Timestamp getTimeStamp(){
      return timestamp;
    }

    public void setAckIndex(int idx) {
      ackArray[idx] = true;
    }
    public boolean getAckIndex(int idx) {
      return ackArray[idx];
    }

    public boolean[] getAckArray(){
      return ackArray;
    }

    public void setAckArray(boolean[] arr) {
    ackArray = arr.clone();
    }

    

    /**
     * Compares two OrderedMessage objects. Breaks ties
     * in timestamp by id. Lower id first.
     */
    @Override
    public int compareTo(OrderedMessage msg) {
        if( msg.getTimeStamp() == this.timestamp ) {
            if(msg.getSender() < this.sender) 
              return 1;
            else if(msg.getSender() > this.sender)
                    return -1;      
                 else
                    return 0; // If timestamp and messages are the same, the objects are equal 
        }
        if(msg.getTimeStamp().before(this.timestamp))
            return 1;
        else
            return -1;
    }


    
    public static final long serialVersionUID = 0;
}
