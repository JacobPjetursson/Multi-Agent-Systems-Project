import java.io.BufferedReader;
import java.io.InputStreamReader;
public class Client {
    

    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
        
        // STEP 1 : First System.out.println(client name);
        System.out.println("Client name");
        
        // STEP 2 : Read level file from server;
        String response = serverMessages.readLine();
        while (true) {
        	if(response.contains("#colors")) {
        		response = serverMessages.readLine();
        		while(!response.contains("#initial")) {
        			String[] split = response.split("\\s+");
        			String color = split[0].substring(0, split[0].length()-1);
        			for(int i = 1; i < split.length; i++) {
        				char chr = split[i].charAt(0);
        				if(chr <= '9' && chr >= '0') {
        					//TODO : Agent has color
        				}else if(chr <= 'Z' && chr >= 'A') {
        					//TODO : Box has color
        				}
        			}
        			response = serverMessages.readLine();
        			
        		}
        		break;
        		
        	} else if(response.contains("#initial")) {
        		while(!response.contains("goal")) {
        			int row = 0;
        			for(int col = 0; col < response.length(); col++) {
        				char chr = response.charAt(col);
        				State.walls[row][col] = false;
        				if (chr == '+') {
        					State.walls[row][col] = true;
        				}else if (chr <= '9' && chr >= '0')	{
        					//TODO : Agent here do something
        				}else if(chr <= 'Z' && chr >= 'A') {
        					//TODO : Box here do something
        				}	
        						
        			}
        			row++;
        		}
        		continue;
        	}
            response = serverMessages.readLine();
        }
        
        // STEP 3 : System.out.println(Solution);

    }
}
