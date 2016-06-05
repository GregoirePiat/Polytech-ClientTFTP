
package src;
/**
 * Created by GregoirePiat on 19/05/16.
 */
public class Main {

    public static void main (String [] arg) {
        Client client = new Client("127.0.0.1");
        int crem = client.prepareSendFile("C:\\Users\\Nicolas\\Desktop\\Film.txt");
        //int crem = client.prepareSendFile("/test.txt");
        
        System.out.println("crem = " + crem);
    }

}
