
import mcgui.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class AckCaster extends Multicaster {
    private OrderedMessage omsg;
    private List<OrderedMessage> deliveryList = new ArrayList<>();
    // private List missingList = new ArrayList<OrderedMessage>();


    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
    }
        
    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
      Timestamp ts = new Timestamp(System.currentTimeMillis());
      boolean[] ackArray = ackArrayInit(hosts);
      ackArray[id] = true;
     
      
      omsg = new OrderedMessage(id, ts, messagetext,ackArray);
      deliveryList.add(omsg);
     // deliveryList.sort();
      Collections.sort(deliveryList);
      
      for(int i=0; i < hosts; i++) {
          /* Sends to everyone except itself */
          if(i != id) {
              bcom.basicsend(i, omsg);
          }
      }
        //mcui.debug("Sent out: \""+messagetext+"\"");
        //mcui.deliver(id, messagetext, "from myself!");
    }
    
    /**
     * On receive: 
     * If  ack: (1) if in list - set acked  
     *            (2) if all acked set status to READY 
     *              (3) if first in list is READY - deliver
     *              (3) if not done            
     *            (2) if not done
     *          (1) if not add to list of expected messages, as acked from sender of ack
     *
     * If original message: 
     *            add to list, sort list, set and send ack for first message in list (if not already sent?)
     */
    public void basicreceive(int peer, Message message) {      
        OrderedMessage ms = (OrderedMessage) message;
        int sender;
        int msgindex = findMessage(ms);
        // On receive, 
        if(ms.isAck()) {
          sender = ms.getSender();
          if(msgindex >= 0) 
            deliveryList.get(msgindex).setAckIndex(sender); 
          else {
            ms.setAckIndex(ms.getAckSender());
            deliveryList.add(ms);
            Collections.sort(deliveryList);
          }
        }
        else{
          if(msgindex >= 0){
            boolean[] arr = deliveryList.remove(msgindex).getAckArray();
            ms.setAckArray(arr);
            deliveryList.add(msgindex,ms);
          }
          else {
            deliveryList.add(ms);
            Collections.sort(deliveryList);
          }     
        }

        if(msgindex == 0) {
          deliveryList.get(0).setAckIndex(id);
          OrderedMessage msg = deliveryList.get(0);
         
          while(msg.isAllAcked() && !deliveryList.isEmpty()){
            msg = deliveryList.remove(0);
            mcui.deliver(peer, msg.getText());
            if(!deliveryList.isEmpty()){
              msg = deliveryList.get(0);
              msg.setAckIndex(id);
            }
          }
          if(!deliveryList.isEmpty() && !msg.isAck() && !msg.getAckIndex(id)) {
            msg.setAckIndex(id);
            for(boolean acked : msg.getAckArray) {
              if(!acked)
                  bcom.basicsend(i, omsg);
            }
          }
        }
       
        //mcui.deliver(peer, ((OrderedMessage)message).text);
    }
    
    public boolean[] ackArrayInit(int hosts) {
        boolean[] tmpArray = new boolean[hosts];
        for(boolean ack : tmpArray)
          ack = false;
        return tmpArray;
    }

    /**
     *  Searches the delivery list for a specified message. 
     *  Returns the index if found, -1 if not.
     *  @param msg  The message to look for
     */
    public int findMessage(OrderedMessage msg) {
        int i = 0;
        for( OrderedMessage listMsg : deliveryList ) {
            if ( msg.compareTo(listMsg) == 0 )
                return i;
            i++;
        }
        return -1;
    }
    
    /**
     * Signals that a peer is down and has been down for a while to
     * allow for messages taking different paths from this peer to
     * arrive.
     * @param peer	The dead peer
     */
    public void basicpeerdown(int peer) {
        mcui.debug("Peer "+peer+" has been dead for a while now!");
    }
    
}
