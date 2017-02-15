
import java.sql.Timestamp;
import java.util.*;

/**
 * Implementation of Multicaster interface...
 *
 * @author Group 9
 */
public class ListTester {
   // private OrderedMessage omsg, amsg;

    Timestamp ts;
    
    public static void main(String args[]) {
        
        List<OrderedMessage> testList = new ArrayList<>();
        List<OrderedMessage> testList2 = new ArrayList<>();
        List<OrderedMessage> testList3 = new ArrayList<>();
                List<OrderedMessage> testList4 = new ArrayList<>();
        ListTester lt = new ListTester();
     //   testList  = new ArrayList<>();
        OrderedMessage amsg, omsg;
        amsg = new OrderedMessage(1, 1, 3, 3);
        omsg = new OrderedMessage(1, 1, 3, "");
        
        for(int i = 0; i < 3; i++) {
            
            omsg = new OrderedMessage(i, i+1, 3, "");
            if(i==0) {
               amsg = new OrderedMessage(i, i+1, 3, 3);                
            }
            testList.add(omsg);
        }
       
       // amsg = new OrderedMessage(i, tid, 3, "");
        Collections.sort(testList);
        
        int msgindex = lt.findMessage(amsg, testList);
        if(msgindex >= 0){
            System.out.println("Hittade riktigt msg motsvarande ack i lista på pos " + msgindex);  
        }
       lt.printList(testList);
        
        for(int i = 0; i < 3; i++) {
            amsg = new OrderedMessage(i, i+2, 3, 10);
            if(i==2) {
               omsg = new OrderedMessage(i, i+2, 3, "");                
            }

            testList2.add(amsg);
        }
        Collections.sort(testList2);
        msgindex = lt.findMessage(omsg, testList2);
        if(msgindex >= 0){
     
            System.out.println("\nHittade msg i lista av acks. index: " + msgindex); 
            System.out.println("Msg har sender : " + omsg.getSender() + " och ts: " + omsg.getTimeStamp()); 
            int j = 0;
            for (OrderedMessage msg : testList2) {
                System.out.println("Ack: "+j + " har sender: " +msg.getSender() + " och ts: " + msg.getTimeStamp());
                j++;
            }  

        }
        
       lt.printList(testList2);
           
       //--------------------CASE 3-------------------
       omsg = new OrderedMessage(1, 2, 6, "");   
       testList3.add(omsg);
       omsg = new OrderedMessage(3, 3, 6, "");   
       testList3.add(omsg);    
       omsg = new OrderedMessage(1, 5, 6, "");   
       testList3.add(omsg);
       omsg = new OrderedMessage(3, 4, 6, "");   
       testList3.add(omsg);
       omsg = new OrderedMessage(4, 4, 6, "");   
       testList3.add(omsg);
       Collections.sort(testList3);       
       lt.printList(testList3);    
       
       //-------------CASE 4-----------------------
       omsg = new OrderedMessage(2, 4, 6, "");
       testList4.add(lt.makeAckMessage(omsg));
       msgindex = lt.findMessage(omsg, testList4);
        
        if(msgindex == 0)
            System.out.println("\nHittade msg i jämförelse av omgjort ack.");
       
    }
    
    public static OrderedMessage makeAckMessage(OrderedMessage msg) {
          OrderedMessage ackmsg = 
              new OrderedMessage(msg.getSender(), msg.getTimeStamp(), 6, 7);
          return ackmsg;
    }
    public static void printList(List<OrderedMessage> al) {
        int i = 0;
        for(OrderedMessage ms : al) {
             System.out.println("Listobject: "+ i + " " + ms.printMessage());   
             i++;   
        }
        System.out.println("-----End of list-----\n");
    }
     /**
     *  Searches the delivery list for a specified message. 
     *  Returns the index if found, -1 if not.
     *  @param msg  The message to look for
     */
    public static int findMessage(OrderedMessage msg, List<OrderedMessage> testList) {
        int i = 0;
        for( OrderedMessage listMsg : testList ) {
            if ( msg.compareTo(listMsg) == 0 ){
                return i;
            }
            i++;
        }
        return -1;
    }
    
}
