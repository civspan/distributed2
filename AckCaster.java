
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
    private List<Integer> crashList = new ArrayList<>();
    int ts;
    int currentHosts;
    
    /**
     * Initialize logical clock and number of current hosts.
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
        currentHosts = hosts;
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
            if(i != id && !crashList.contains(i)) {
                  bcom.basicsend(i, omsg);
            }
        }
    }
    
    public void basicreceive(int peer, Message message) {  
        omsg = (OrderedMessage) message;
        int msgindex = findMessage(omsg);
        
        /* On receive, check if ack */ 
        if(!omsg.isCrashed() && omsg.isAck()) {
            
            /* Message is ack and corresponding message is in list.
             * Will set message in list as acked from sender of ack */
            if(msgindex >= 0) {
                deliveryList.get(msgindex).setAckIndex( peer );
                crashedAcks(deliveryList.get(msgindex));
            }
          /* Message is ack but not in list. Create ack array for this placeholder,
           * then add to list. Exit. */
            else {

                omsg.makeAckArray(peer);
                deliveryList.add(omsg);
                Collections.sort(deliveryList);
                return;
            }
        }
        /* Message is real message */
        else if(!omsg.isCrashed()){
              ts++; 
                 
            /* Placeholder (ack) message found in list. Extract ack array from placeholder,
             * replace message ack array with this array, set message sender as acker in new array,
             * remove placeholder from list and insert message in its place */
            if(msgindex >= 0){
                OrderedMessage placeholder = deliveryList.remove(msgindex);
                boolean[] arr = placeholder.getAckArray();
                omsg.setAckArray(arr);
                omsg.setAckIndex(peer);
                crashedAcks(omsg);
                deliveryList.add(msgindex, omsg);
            }
            
            /* Real message and not in list. Add to list and sort. Calculate new index in list */
            else {
                deliveryList.add(omsg);
                Collections.sort(deliveryList);
                msgindex = findMessage(omsg);
            }     
        }
        
      /*  Get message object from list. Set ack. */
        if(!deliveryList.isEmpty()) {
          firstmsg = deliveryList.get(0);
          firstmsg.setAckIndex(id);
          crashedAcks(firstmsg);
                
           /* While the list isn't empty and the first message is fully acked,
            * deliver that message */
            while( deliveryList.size() != 0 && firstmsg.isAllAcked() ){
                
                /* If I haven't sent acks for this message, and I'm not the original sender
                 * of the message - broadcast acks */
                if( !(firstmsg.haveSentAcks() || firstmsg.getSender() == id) ) {
                    broadcastAck(firstmsg);
                    firstmsg.sentAcks();
                }   
                /* Remove the first message in the list and deliver it. */ 
                firstmsg = deliveryList.remove(0);
                mcui.deliver(firstmsg.getSender(), firstmsg.getText());
                
                /* If there are messages in the list after delivery 
                 * look at that message and set as acked */
                if(!deliveryList.isEmpty()){
                    firstmsg = deliveryList.get(0);
                    firstmsg.setAckIndex(id);
                    crashedAcks(firstmsg);
                }
            }
            
            /* There is a real message in the list that is not fully acked -  
             * that I did not send - that isn't an ack - 
             * that I haven't already acked -> broadcast acks. */
            if(!(deliveryList.isEmpty() || firstmsg.isAck() || 
                firstmsg.getSender() == id || firstmsg.haveSentAcks()) ) {
                firstmsg.sentAcks();
                broadcastAck(firstmsg);
            }
        }
    }
    
    /**
     *  Debugging method. Prints the delivery list.
     */
    public void printList() {
        int i = 0;
        for(OrderedMessage ms : deliveryList) {
             mcui.debug("Listobject: "+ i + " " + ms.printMessage());   
             i++;   
        }
        mcui.debug("-----End of list-----\n");
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
     *  to all correct peers.  
     *  @param firstmsg The message to acknowledge
     */    
    public void broadcastAck(OrderedMessage firstmsg) {
        OrderedMessage ackmsg = makeAckMessage(firstmsg);
        for(int i=0; i < hosts; i++) {
            if(i != id && !crashList.contains(i)) {
                bcom.basicsend(i, ackmsg);
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
            if ( msg.compareTo(listMsg) == 0 )
                return i;
            i++;
        }
        return -1;
    }
    
    public void printAckArray(String s, boolean[] arr) {
        mcui.debug(s + " " + Arrays.toString(arr) );
    }
    
    /**
     *  Traverses the list of crashed peers and acks the  
     *  supplied message on their behalf
     *  @param msg  The message to ack on behalf of the crashed peers
     */
    public void crashedAcks(OrderedMessage msg) {
        for(int crashed : crashList){
            msg.setAckIndex(crashed);
        }
    }
    
    /**
     * Signals that a peer is down and has been down for a while to
     * allow for messages taking different paths from this peer to
     * arrive.
     * @param peer	The dead peer
     */
    public void basicpeerdown(int peer) {
        currentHosts -= 1;
        
        /* Create a crashmessage and send it to myself.
         * Now I can wake up and deliver messages not from the dead peer
         * in my delivery list that are now set as acked from the dead peer. */
        OrderedMessage omsg = new OrderedMessage(id,true); 
        crashList.add(peer);
        bcom.basicsend(id, omsg);
        int i = 0;
        /* Removes real messages sent by crashed peer from delivery list */
        for(OrderedMessage msg : deliveryList) {
            if(msg.getSender() == peer && !msg.isAck())
                deliveryList.remove(i);
            i++;
        }   
        mcui.debug("Peer "+peer+" has been dead for a while now!");
        mcui.debug("There were " + hosts + " hosts. Now there are " + currentHosts);
      
    }
    
}
