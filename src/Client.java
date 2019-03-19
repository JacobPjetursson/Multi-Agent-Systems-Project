import java.io.BufferedReader;
import java.io.InputStreamReader;
public class Client {
    

    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
        
        // STEP 1 : First System.out.println(client name);
        System.out.println("Client name");
        
        // STEP 2 : Read level file from server;
        String response = serverMessages.readLine();
        while (!response.contains("end")) {
            //Load the level
            response = serverMessages.readLine();
        }
        
        // STEP 3 : System.out.println(Solution);

    }
}
