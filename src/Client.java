import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import state.Agent;
import state.Box;
import state.Goal;
import state.Location;
import state.State;
public class Client {

	private static String domain;
	private static String levelName;

	public static void main(String[] args) throws Exception {
		BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
		Map<Character, Integer> colorMap = new HashMap<>();
		
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
			String color = split[0];
			color = color.substring(0, color.length()-1);
			for(int i = 1; i < split.length; i++) {
				char chr = split[i].charAt(0);
				colorMap.put(chr, getColorCode(color));
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
		List<Agent> agents = new ArrayList<>();
		List<Box> boxes = new ArrayList<>();
		List<Goal> goals = new ArrayList<>();
		for(int row = 0; row < rows; row++) {
			String levelLine = levelLines.get(row);
			for(int col = 0; col < cols; col++) {
				char chr = levelLine.charAt(col);
				if (chr == '+') {
					State.walls[row][col] = true;
				}else if (chr <= '9' && chr >= '0')	{
					Location position = new Location(row, col);
					int color = colorMap.get(chr);
					int id = Character.getNumericValue(chr);
					agents.add(new Agent(position, color, id));
				}else if(chr <= 'Z' && chr >= 'A') {
					Location position = new Location(row, col);
					int color = colorMap.get(chr);
					boxes.add(new Box(position, color, chr));
				}
			}
		}

		response = serverMessages.readLine();
		int row = 0;
		while(!response.contains("end")) {
			for(int col = 0; col < response.length(); col++) {
				char chr = response.charAt(col);
				if(chr <= 'Z' && chr >= 'A') {
					Location position = new Location(row, col);
					int color = colorMap.get(chr);
					goals.add(new Goal(position, color, chr));
				}
			}
			row++;
			response = serverMessages.readLine();
		}
		
		for (Goal goal : goals) {
			System.err.println(goal);
		}
		for (Box box : boxes) {
			System.err.println(box);
		}
		for (Agent agent : agents) {
			System.err.println(agent);
		}


		// STEP 3 : System.out.println(Solution);
	}
	
	public static int getColorCode(String color) {
		switch (color) {
		case "blue":
			return 1;
		case "red":
			return 2;
		case "cyan":
			return 3;
		case "purple":
			return 4;
		case "green":
			return 5;
		case "orange":
			return 6;
		case "pink":
			return 7;
		case "grey":
			return 8;
		case "lightblue":
			return 9;
		case "brown":
			return 10;
		default:
			return 0;
		}
	}
	
}
