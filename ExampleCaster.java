
import mcgui.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {
    private ExampleMessage emsg;
    private List ls = new ArrayList<ExampleMessage>();


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
      emsg = new ExampleMessage(id, messagetext,ts);
      ls.add(emsg);
      ls.sort();
      for(int i=0; i < hosts; i++) {
          /* Sends to everyone except itself */
          if(i != id) {
              bcom.basicsend(i,emsg);
          }
      }
        //mcui.debug("Sent out: \""+messagetext+"\"");
        //mcui.deliver(id, messagetext, "from myself!");
    }
    
    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer,Message message) {
        ExampleMessage ms = (ExampleMessage) message;
        if(ms.isAck()){
          if()
        }

        //mcui.deliver(peer, ((ExampleMessage)message).text);
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
