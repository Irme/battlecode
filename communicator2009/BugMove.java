package communicator2009;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class BugMove {
	private enum BuggingState {
		INIT, FOLLOWING_LINE, AT_OBSTACLE, FOLLOWING_OBSTACLE ;
	}
	
	private static double h;
	private static BuggingState state = BuggingState.INIT;
	private static MapLocation dest;
	private static Line line; 
	private static RobotController rc; 
	private static MapLocation next; 
	private static MapLocation curr; 
	private static Direction dir; 
	private static Direction movingDir; 
	private static Direction towardsWallDir;
	private static boolean wallOnLeft;
	private static int[][] lastLookingDirections;
	
	public static boolean movableLoc(MapLocation loc, RobotController rcin) {
		//TerrainTile tile = rcin.senseTerrainTile(loc);
		//return tile.equals(TerrainTile.NORMAL) || tile.equals(TerrainTile.ROAD);
		return rc.canMove(rc.getLocation().directionTo(loc));
	}
	public static void move(MapLocation d, RobotController rcin, boolean sneaking) throws GameActionException {
		Direction dir = moveHelper(d,rcin,sneaking);
		if(dir != null){
			if(sneaking){
				rc.sneak(dir);
			}else{
				rc.move(dir);
			}
		}
	}
	public static Direction moveHelper(MapLocation d, RobotController rcin, boolean sneaking) throws GameActionException {
		rc = rcin;
		curr = rc.getLocation(); 
		
		if (!d.equals(dest)) 
			state = BuggingState.INIT; 
		
		if (curr == dest)
			return null;
		
		if (state == BuggingState.INIT) {
			dest = d; 
			lastLookingDirections = new int[rc.getMapWidth()][rc.getMapHeight()];
			//line = new Line(curr, dest);
			//next = line.getLineMapLoc(curr.add(curr.directionTo(dest)));
			dir = curr.directionTo(dest);
			
			if (!movableLoc(curr.add(dir), rc)) 
				state = BuggingState.AT_OBSTACLE;
			else 
				state = BuggingState.FOLLOWING_LINE;	
		}
		
		switch (state) {
		case AT_OBSTACLE:
//			System.out.println("at an obstacle");
			Direction leftDir = dir; 
			Direction rightDir = dir; 
			h = dest.distanceSquaredTo(curr);
			
			
			for (int i = 3; i > 0; i--) {
				leftDir = leftDir.rotateLeft();
				if (movableLoc(curr.add(leftDir), rc)) break; 
			}
			
			for (int i = 3; i > 0; i--) {
				rightDir = rightDir.rotateRight();
				if (movableLoc(curr.add(rightDir), rc)) break; 
			}
			
			int lastDir = lastLookingDirections[curr.x][curr.y];
			
			if (lastDir == 1) {
				wallOnLeft = true; 
				movingDir = rightDir;
				towardsWallDir = rightDir.rotateLeft(); 
			} else if (lastDir == -1) {
				wallOnLeft = false; 
				movingDir = leftDir; 
				towardsWallDir = leftDir.rotateRight(); 
			} else if (dest.distanceSquaredTo(curr.add(leftDir)) > dest.distanceSquaredTo(curr.add(rightDir))) {
				wallOnLeft = true;
				movingDir = rightDir;
				towardsWallDir = rightDir.rotateLeft(); 
				lastLookingDirections[curr.x][curr.y] = -1;
			} else {
				wallOnLeft = false; 
				movingDir = leftDir; 
				towardsWallDir = leftDir.rotateRight();
				lastLookingDirections[curr.x][curr.y] = 1; 
			}
			
			dir = movingDir; 
			
			state = BuggingState.FOLLOWING_OBSTACLE;
			break; 
			
		case FOLLOWING_OBSTACLE:
			Direction testDir = curr.directionTo(dest);
			if (movableLoc(curr.add(curr.directionTo(dest)), rc) && movableLoc(curr.add(testDir.rotateLeft()), rc) && movableLoc(curr.add(testDir.rotateRight()), rc)) {
				state = BuggingState.FOLLOWING_LINE; 
//				System.out.println("can move in direction of destination");
				return null; 
			} else {
				if (movableLoc(curr.add(towardsWallDir), rc)) {
					dir = towardsWallDir;
					towardsWallDir = wallOnLeft ? dir.rotateLeft() : dir.rotateRight(); 
				} else {
					dir = towardsWallDir; 
					int count = 0;
					do{
						dir = wallOnLeft ? dir.rotateRight() : dir.rotateLeft(); 
						count ++;
					}while(!movableLoc(curr.add(dir), rc) && count < 8);
					
					towardsWallDir = wallOnLeft ? dir.rotateLeft() : dir.rotateRight(); 
				}
//				System.out.println(towardsWallDir);
			}
			break; 
		
		case FOLLOWING_LINE:
			//next = line.getLineMapLoc(curr.add(curr.directionTo(dest)));
			dir = curr.directionTo(dest);
			
			if (!movableLoc(curr.add(dir), rc)) {
				state = BuggingState.AT_OBSTACLE;
			}
			break; 
		
		default:
			break;
		}
//		rc.setIndicatorString(0, "Moving Direction = "  + dir + " Towards Wall Direction = " + towardsWallDir + " " + (wallOnLeft ? "wall on left " : "wall on right ") + state );
		if (rc.isActive() && rc.canMove(dir)) {
			//SnailTrail.move(curr.add(dir), rc.getLocation(), rc);
			return dir;
		}
		return null;
	}
	
	
	
	private static class Line{
		private double m; // gradient 
		private double c; // y - intercept
		
		Line(MapLocation A, MapLocation B) {
			double xf = B.x; 
			double yf = B.y; 
			double xi = A.x; 
			double yi = A.y; 
			
			m = (yf - yi)/(xf - xi);
			c = yi - m*xi;  
		}
		
		MapLocation getLineMapLoc(MapLocation loc) {
			return new MapLocation(loc.x, (int) Math.round(m*((double) loc.x) + c));
		}
		
		boolean locOnLine(MapLocation loc) {
			double rhs =  Math.round(m*((double) loc.x) + c);
			double y = (double) loc.y;
			return y < (rhs + 1.5) && y > (rhs - 1.5);
		}
	}
	
}
