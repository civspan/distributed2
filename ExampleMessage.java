
import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleMessage extends Message {
        
    String text;
    int seq;
        
    public ExampleMessage(int sender,int seq, String text) {
        super(sender);
        this.text = text;
        this.seq = seq;
    }
    
    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }
    
    public static final long serialVersionUID = 0;
}
