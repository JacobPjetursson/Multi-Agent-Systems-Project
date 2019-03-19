import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
public class Client {
	
	private static String domain;
	private static String levelName;
    

    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
        
        // STEP 1 : First System.out.println(client name);
        System.out.println("Client name");
        
        // STEP 2 : Read level file from server;
        String response = serverMessages.readLine(); // response = #domain
        domain = serverMessages.readLine();
        response = serverMessages.readLine();  // response = #levelname
        levelName = serverMessages.readLine();
        response = serverMessages.readLine(); // response = #colors
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
	response = serverMessages.readLine();
	int cols = response.length();
	int rows = 0;
	List<String> levelLines = new ArrayList<String>();
	while(!response.contains("goal")) {
		levelLines.add(response);
		rows++;
		response = serverMessages.readLine();
	}
		
	State.walls = new boolean[rows][cols];
		


	for(int row = 0; row < rows; row++) {
		String levelLine = levelLines.get(row);
		for(int col = 0; col < cols; col++) {
			char chr = levelLine.charAt(col);
			if (chr == '+') {
				State.walls[row][col] = true;
			}else if (chr <= '9' && chr >= '0')	{
				//TODO : Agent here do something
			}else if(chr <= 'Z' && chr >= 'A') {
				//TODO : Box here do something
			}	
		}
	}
		
	response = serverMessages.readLine();
	while(!response.contains("end")) {
		int row = 0;
		for(int col = 0; col < response.length(); col++) {
			char chr = response.charAt(col);
			if(chr <= 'Z' && chr >= 'A') {
				//TODO : Goal here do something
			}	
					
		}
		row++;
		response = serverMessages.readLine();
	}
		
        
        // STEP 3 : System.out.println(Solution);

    }
}
