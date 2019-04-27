package state;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class DistanceMap {
	private int[][] map;
	
	DistanceMap(Location init) {
		boolean[][] walls = State.walls;
		map = new int[State.ROWS][State.COLS];
		Queue<Object[]> steps = new LinkedList<>();
		steps.add(new Object[] { init, 0 });
		Set<Location> stepSet = new HashSet<>();
		while (!steps.isEmpty()) {
			Object[] tuple = steps.poll();
			Location point = (Location) tuple[0];
			int value = (int) tuple[1];
			if (stepSet.contains(point)) continue;
			if (walls[point.getRow()][point.getCol()]) continue;
			stepSet.add(point);
			map[point.getRow()][point.getCol()] = value;
			value++;
			if (point.getRow() < State.ROWS-1) {
				steps.add(new Object[] { new Location(point.getRow() + 1, point.getCol()), value });
			}
			if (point.getRow() > 0) {
				steps.add(new Object[] { new Location(point.getRow() - 1, point.getCol()), value });
			}
			if (point.getCol() < State.COLS-1) {
				steps.add(new Object[] { new Location(point.getRow(), point.getCol() + 1), value });
			}
			if (point.getCol() > 0) {
				steps.add(new Object[] { new Location(point.getRow(), point.getCol() - 1), value });
			}
		}
	}
	
	public int distance(Location p) {
		return map[p.getRow()][p.getCol()];
	}
}