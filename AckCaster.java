
import mcgui.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * Implementation of Multicaster interface...
 *
 * @author Group 9
 */
public class AckCaster extends Multicaster {
    private OrderedMessage omsg, firstmsg;
    private List<OrderedMessage> deliveryList = new ArrayList<>();
    int ts;

    /**
     * No initializations needed for this simple one
     */
    public void init() {
        //mcui.debug("The network has "+hosts+" hosts!");
        ts = 0;
    }
        
    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        ts++;
        omsg = new OrderedMessage(id, ts, hosts, messagetext); 

        deliveryList.add(omsg);
        Collections.sort(deliveryList);
        
        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i, omsg);
            }
        }
    }
    
    public void basicreceive(int peer, Message message) {  
        ts++;    
        omsg = (OrderedMessage) message;
        int msgindex = findMessage(omsg);
        
        /* On receive, check if ack */ 
        if(omsg.isAck()) {
            //mcui.debug("Ack received from " + peer );
            
            /* Message is ack and corresponding message is in list.
             * Will set message in list as acked from sender of ack */
            if(msgindex >= 0) {
                deliveryList.get(msgindex).setAckIndex( peer );
                mcui.debug("Got ack from " + peer + " for message from "
                  + omsg.getSender() ); 
            }
          /* Message is ack but not in list. Create ack array for this placeholder,
           * then add to list. Exit. */
            else {
                mcui.debug("placeholder created, added to list. ");
                omsg.makeAckArray(peer);
                deliveryList.add(omsg);
                Collections.sort(deliveryList);
                return;
            }
        }
        /* Message is real message */
        else{
             
            /* Placeholder (ack) message found in list. Extract ack array from placeholder,
             * replace message ack array with this array, set message sender as acker in new array,
             * remove placeholder from list and insert message in its place */

           
            if(msgindex >= 0){
                mcui.debug("Real message received, placeholder found. msg has msgindex : " + msgindex );
                OrderedMessage placeholder = deliveryList.remove(msgindex);
                boolean[] arr = placeholder.getAckArray();
                printAckArray("Placeholder array:", arr);
                omsg.setAckArray(arr);
                printAckArray("Placeholder array copied to new:", omsg.getAckArray());
                omsg.setAckIndex(peer);
                printAckArray("Copied array with sender set as acked:", omsg.getAckArray());
                deliveryList.add(msgindex, omsg);
            }
            
            /* Real message and not in list. Add to list and sort. Calculate new index in list */
            else {
                 mcui.debug("Real message received, not in list. msg has msgindex : " + msgindex );                
                deliveryList.add(omsg);
                Collections.sort(deliveryList);
                msgindex = findMessage(omsg);
                mcui.debug("New index: " + msgindex );
                //mcui.debug("Real message received. It was not found in list, but added. "
               //   + "Message from " + deliveryList.get(0).getSender() + " is first in list");
                //mcui.debug("Its message index is: " + msgindex);
            }     
        }
        
        
        /* Message is first in list. Get message object from list. Set ack. */
        if(msgindex == 0) {
            firstmsg = deliveryList.get(0);
            firstmsg.setAckIndex(id);
            //mcui.debug("Message from " + firstmsg.getSender() + " is first in list");
            
           /* While the list isn't empty and the first message is fully acked,
            * deliver that message */
            int a = 0;
            while( deliveryList.size() != 0 && firstmsg.isAllAcked() && a < 3 ){
                a++;
                firstmsg = deliveryList.remove(0);
                mcui.deliver(firstmsg.getSender(), firstmsg.getText());
                
                /* If there are messages in the list after delivery 
                 * look at that message and set as acked */
                if(!deliveryList.isEmpty()){
                    firstmsg = deliveryList.get(0);
                    firstmsg.setAckIndex(id);
                }
            }
            /* There is a real message in the list that is not fully acked. Broadcast acks. */
            if(!deliveryList.isEmpty() && !firstmsg.isAck()) {
                //mcui.debug("Real message received, should send ack.");
                firstmsg.setAckIndex(id);
                broadcastAck(firstmsg);
            }
        }
        //printList();
    }
    

    public void printList() {
        int i = 0;
        for(OrderedMessage ms : deliveryList) {
             //mcui.debug("Listobject: "+ i + " " + ms.printMessage());   
             i++;   
        }
        //mcui.debug("-----End of list-----\n");
    }
    /**
     *  Takes a message and returns an ack message for that message
     *  @param msg The message to acknowledge
     */
    public OrderedMessage makeAckMessage(OrderedMessage msg) {
          OrderedMessage ackmsg = 
              new OrderedMessage(msg.getSender(), msg.getTimeStamp(), hosts, id);
          return ackmsg;
    }

    /**
     *  Takes a message and broadcasts an ack message for that message
     *  @param firstmsg The message to acknowledge
     */    
    public void broadcastAck(OrderedMessage firstmsg) {
        OrderedMessage ackmsg = makeAckMessage(firstmsg);
        for(int i=0; i < hosts; i++) {
            if(i != id) {
                bcom.basicsend(i, ackmsg);
                //mcui.debug("Sent ack to " + i );
            }
        }
    }

    /**
     *  Searches the delivery list for a specified message. 
     *  Returns the index if found, -1 if not.
     *  @param msg  The message to look for
     */
    public int findMessage(OrderedMessage msg) {
        int i = 0;
        for( OrderedMessage listMsg : deliveryList ) {
            //mcui.debug("incoming msg : " + msg.printMessage());
            //mcui.debug("message in list : " + listMsg.printMessage());
            if ( msg.compareTo(listMsg) == 0 ){
                //mcui.debug("oooh yeah.. a match!");
                return i;
            }
            i++;
        }
        return -1;
    }
    
    public void printAckArray(String s, boolean[] arr) {
        //mcui.debug(s + " " + Arrays.toString(arr) );
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
