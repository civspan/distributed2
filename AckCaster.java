
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

    //private OrderedMessage b = new OrderedMessage(1,t,"",{});
    //deliveryList.add(a);
    //deliveryList.add(b);
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
        // Timestamp t = new Timestamp(System.currentTimeMillis());
        // boolean[] p = {true};
       // OrderedMessage a = new OrderedMessage(1,t,"",p);
      //deliveryList.add(a);
      
     // int r = findMessage(a);
     // mcui.debug("findmessage: \""+r+"\"");
      // mcui.debug("printmsg: \""+omsg+"\"");
      Timestamp ts = new Timestamp(System.currentTimeMillis());
      boolean[] ackArray = ackArrayInit(hosts);
      ackArray[id] = true;
     
      
      omsg = new OrderedMessage(id, ts, messagetext,ackArray);
      //mcui.debug("printmsg: \""+omsg+"\"");
      deliveryList.add(omsg);
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
    

    public void basicreceive(int peer, Message message) {      
        OrderedMessage ms = (OrderedMessage) message;
        int sender;
        int msgindex = findMessage(ms);
        printList();
        mcui.debug("End of list..\n");
        //mcui.debug("received msg: \""+ms.getText()+"\"");
        // On receive, 
        if(ms.isAck()) {
          //mcui.debug("received ack: \""+ms.getText()+"\"");
          sender = ms.getSender();
          if(msgindex >= 0) {
            mcui.debug("msg in list");
            deliveryList.get(msgindex).setAckIndex(sender); 
          }
        /* Message is ack but not in list*/
          else {
            //mcui.debug("msg NOT in list");
            ms.setAckIndex(ms.getAckSender());
            deliveryList.add(ms);
            Collections.sort(deliveryList);
          }
        }
        else{
          //mcui.debug("received real msg: \""+ms.getText()+"\"");
          if(msgindex >= 0){
            
            boolean[] arr = deliveryList.remove(msgindex).getAckArray();
            ms.setAckArray(arr);
            deliveryList.add(msgindex,ms);
          }
          else {
            //mcui.debug("length of list is before: \""+deliveryList.size()+"\"");
            deliveryList.add(ms);
            Collections.sort(deliveryList);
            //mcui.debug("length of list is after: \""+deliveryList.size()+"\"");
          }     
        }
        msgindex = findMessage(ms);
        if(msgindex == 0) {
          deliveryList.get(0).setAckIndex(id);
          OrderedMessage msg = deliveryList.get(0);
          //mcui.debug("message : \""+msg.getText()+"\"");
          //mcui.debug("message : \""+msgindex+"\"");
         
          while(msg.isAllAcked() && !deliveryList.isEmpty()){
            msg = deliveryList.remove(0);
            mcui.deliver(peer, msg.getText());
            if(!deliveryList.isEmpty()){
              msg = deliveryList.get(0);
              msg.setAckIndex(id);
            }
          }
          if(!deliveryList.isEmpty() && !msg.isAck()) {
            
            //mcui.debug("ackno : ");
            msg.setAckIndex(id);
            boolean[] ackArray = ackArrayInit(hosts);
            ackArray[id] = true;
            OrderedMessage ackmsg = new OrderedMessage(msg.getSender(),msg.getTimeStamp(),ackArray,id);
            //mcui.debug("ackmsg is : \""+ackmsg.+"\"");
            for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
              if(i != id) {
                bcom.basicsend(i, ackmsg);
              }
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

    public void printList() {
      for(OrderedMessage ms : deliveryList) {
         mcui.debug("Listobject : \""+ms.printMessage()+"\"");      
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
