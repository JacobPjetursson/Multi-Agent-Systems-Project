import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import state.*;

public class Client {

	private static String domain;
	private static String levelName;
	public static Set<Integer> agentsOfColor;

	public static void main(String[] args) throws Exception {
		BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
		Map<Character, Integer> colorMap = new HashMap<>();
		agentsOfColor = new HashSet<>();
		
		// STEP 1 : First System.out.println(client name);
		System.out.println("Bob");

		// STEP 2 : Read level file from server;
		String response;
		serverMessages.readLine(); // response = #domain
		domain = serverMessages.readLine();
		serverMessages.readLine();  // response = #levelname
		levelName = serverMessages.readLine();
		serverMessages.readLine(); // response = #colors
		response = serverMessages.readLine();
		while(!response.contains("#initial")) {
			//TODO : Split on more than black space (MAHelloWorl)
			String[] colorSplit = response.split(":");
			String[] split = colorSplit[1].split(",");
			String color = colorSplit[0];
			color = color.substring(0, color.length());
			for(int i = 0; i < split.length; i++) {
				split[i] = split[i].replaceAll("\\s","");
				if(split[i].length() < 1) {
					continue;
				}
				char chr = split[i].charAt(0);
				colorMap.put(chr, getColorCode(color));
				if(chr >= '0' && chr <= '9') {
					agentsOfColor.add(getColorCode(color));
				}
			}
			response = serverMessages.readLine();
		}
		response = serverMessages.readLine();
		int cols = 0;
		int rows = 0;
		List<String> levelLines = new ArrayList<>();
		while(!response.contains("goal")) {
			cols = Math.max(cols, response.length());
			levelLines.add(response);
			rows++;
			response = serverMessages.readLine();
		}
      
		State.ROWS = rows;
		State.COLS = cols;
		State.walls = new boolean[rows][cols];
		Map<Integer, Agent> agents = new HashMap<>();
		Map<Integer, Box> boxes = new HashMap<>();
		Map<Character,Integer> boxLetters = new HashMap<>();
		int boxId = 1;
		List<Goal> goals = new ArrayList<>();
		Map<Location,Goal> goalMap = new HashMap<>();
		Set<Character> goalLetters = new HashSet<>();
		
		for(int row = 0; row < rows; row++) {
			String levelLine = levelLines.get(row);
			for(int col = 0; col < levelLine.length(); col++) {
				char chr = levelLine.charAt(col);
				if (chr == '+') {
					State.walls[row][col] = true;
				}else if (chr <= '9' && chr >= '0')	{
					Location position = new Location(row, col);
					int color = colorMap.get(chr);
					int id = Character.getNumericValue(chr);
					agents.put(id, new Agent(position, color, id));
				}else if(chr <= 'Z' && chr >= 'A') {
					Location position = new Location(row, col);
					int color = colorMap.get(chr);
					if(agentsOfColor.contains(color)) {
						boxes.put(boxId, new Box(boxId, color, chr, position));
						boxId++;
						if(!boxLetters.containsKey(chr)) {
							boxLetters.put(chr,1);
						}
						boxLetters.put(chr,boxLetters.get(chr)+1);
						
					}else {
						State.walls[row][col] = true;
					}
					
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
					if(agentsOfColor.contains(color)) {
						Goal goal = new BoxGoal(position, color, chr);
						goals.add(goal);
						goalMap.put(position, goal);
						goalLetters.add(chr);
					}
					
					
				}else if(chr >= '0' && chr <= '9') {
					Location position = new Location(row, col);
					int color = colorMap.get(chr);
					Goal goal = new AgentGoal(position, color, chr);
					goals.add(goal);
					goalMap.put(position, goal);
				}
			}
			row++;
			response = serverMessages.readLine();
		}

		for (Goal goal : goals) {
			System.err.println(goal);
		}
		for (Box box : boxes.values()) {
			System.err.println(box);
		}
		for (Agent agent : agents.values()) {
			System.err.println(agent);
		}
		
		State.goals = goals;
        State.goalMap = goalMap;
        State initialState = new State(agents, boxes);
        
        boolean changed = true;
        while(changed) {
        	changed = false;
        	//Set spaces which are unreachables to walls
            for(row = 0; row < rows; row++) {
        		for(int col = 0; col < cols; col++) {
        			Location curLoc = new Location(row,col);
        			DistanceMap dm = State.DISTANCE_MAPS.get(curLoc);
        			int dist = 0;
        			for(Agent agent : agents.values()) {
        	        	Location agentLoc = agent.getLocation();
        	        	if(!agentLoc.equals(curLoc)) {
            				dist+=dm.distance(agentLoc);
            			}else {
            				dist++;
            			}
        	        	
        			}
        			if(dist == 0 && !State.walls[row][col]) {
        				State.walls[row][col] = true;
        				changed = true;
        				if(goalMap.containsKey(new Location(row,col))) {
        					goalMap.remove(new Location(row,col));
        				}
        			}
        			
        		}
        	}
            
            //Set boxes that are unreachable to walls
            List<Integer> boxesToRemove = new ArrayList<>();
            for(Box box : boxes.values()) {
        		int boxColor = box.getColor();
        		DistanceMap dm = State.DISTANCE_MAPS.get(box.getLocation());
        		boolean isReachable = false;
        		for(Agent agent : agents.values()) {
        			int agentColor = agent.getColor();
        			if(agentColor == boxColor) {
        				if(dm.distance(agent.getLocation())!=0) {
        					isReachable = true;
        					break;
        				}
        			}
        		}
        		if(!isReachable) {
        			boxesToRemove.add(box.getId());
        			if(goalMap.containsKey(box.getLocation())) {
    					goalMap.remove(box.getLocation());
    				}
        			if(!State.walls[box.getLocation().getRow()][box.getLocation().getCol()]) {
            			State.walls[box.getLocation().getRow()][box.getLocation().getCol()] = true;
            			changed = true;
            			
            		}
        		}
        	}
            for(Integer i : boxesToRemove) {
            	boxes.remove(i);
            }
            initialState = new State(agents, boxes);
        }
        initialState = new State(agents, boxes);
        
        for(int i = 0; i < State.ROWS; i++) {
        	for(int j = 0; j < State.COLS; j++) {
        		if(State.walls[i][j]) {
        			System.err.print("x");
        		}else {
        			System.err.print(" ");
        		}
        		
        	}
        	System.err.println();
        }
        
        
        for(Character c : boxLetters.keySet()) {
        	if(goalLetters.contains(c) || boxLetters.get(c) < 20) {
        		continue;
        	}
    		for(Box box : boxes.values()) {
    			if(box.getLetter() != c) {
    				continue;
    			}
				
				State.walls[box.getLocation().getRow()][box.getLocation().getCol()] = true;
				
        	
        		
        	}
            boolean revert = false;
            initialState = new State(agents, boxes);
            l : for(Goal goal : goals) {
            	DistanceMap dm = State.DISTANCE_MAPS.get(goal.getLocation());
            	for(Box box : boxes.values()) {
            		if(box.getLetter() == goal.getLetter()) {
            			if(dm.distance(box.getLocation()) == 0 && !box.getLocation().equals(goal.getLocation())) {
            				revert = true;
            				break l;
            			}else {
            				break;
            			}
            		}
            	}
            	for(Agent agent : agents.values()) {
            		if(agent.getLetter() == goal.getLetter()) {
            			if(dm.distance(agent.getLocation()) == 0 && !agent.getLocation().equals(goal.getLocation())) {
            				revert = true;
            				break l;
            			}else {
            				break;
            			}
            		}
            	}
            }
            if(revert) {
            	for(Box box : boxes.values()) {
        			if(box.getLetter() != c) {
        				continue;
        			}
    				if(goalLetters.contains(box.getLetter())) {
                		continue;
                	}
    				State.walls[box.getLocation().getRow()][box.getLocation().getCol()] = false;
    				
            	
            		
            	}
            	initialState = new State(agents, boxes);
            }else {
            	List<Box> boxesToRemove = new ArrayList<>();
            	for(Box box : boxes.values()) {
            		if(box.getLetter() == c) {
            			boxesToRemove.add(box);
            		}
            	}
            	for(Box box : boxesToRemove) {
            		boxes.remove(box.getId());
            	}
            	
            }
    	}
    	
        
        State.goalMap = goalMap;
        State.goals = new ArrayList<>(goalMap.values());
        
        
        Thread schedule = new Thread(new Scheduler(initialState, serverMessages));
        schedule.start();
		// STEP 3 : System.out.println(Solution);
	}
	
	public static String getDomain() {
		return domain;
	}
	
	public static String getLevel() {
		return levelName;
	}
	
	private static int getColorCode(String color) {
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
