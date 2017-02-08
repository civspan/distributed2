
import mcgui.*;
//import Tuple.*;
import java.util.*;
import java.io.*;


/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

		int seq = 0;
	//	String msg = "";
		Triple listMess1, listMess2;
		
		
		List<Tuple> orderList = new LinkedList<Tuple>();
    
    
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

   			
        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i,new ExampleMessage(id, seq, messagetext));
            }
        }
        mcui.debug("Sent out: \""+messagetext+"\"");
        mcui.deliver(id, messagetext, "from myself!");
    }
    
    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer,Message message) {
       	listMess1 = new Triple(id, seq, message);

   			
   			System.out.println(listMess1.msg.text);
   			System.out.println(listMess2.msg.text);
        mcui.deliver(peer, ((ExampleMessage)message).text);
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
