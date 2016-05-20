

/**
 * Created by GregoirePiat on 19/05/16.
 */
public class Main {

    public static void main (String [] arg) {
        Client client = new Client("127.0.0.1", 69);
        client.prepareSendFile("C:\\Users\\Nicolas\\Desktop\\Film.txt");
    }

}
