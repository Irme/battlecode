package aStarTest;

import java.util.LinkedList;
import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {
	public static Random rand = new Random(); 
	public static LinkedList<MapLocation> currentPath = new LinkedList<MapLocation>();
	
	public static void run(RobotController rc){
		rand.setSeed(rc.getRobot().getID());
		while(true){
			// ____________HQ ______________________________
			if(rc.getType() == RobotType.HQ){
				try {
					while(rc.isActive()){
						//if(rc.senseRobotCount() < 1){
							//MapSampling ms = new MapSampling(rc);
							// ArrayList<ExtendedTile> exTiles = ms.getExtendTileList();
							// System.out.println("Sampled map:");
							// for(int i = 0; i < exTiles.size(); ++i){
							// 	System.out.println(exTiles.toString());
							// }

							rc.spawn(getRandomDir(rc));
						// }
						rc.yield();
					}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
				
			// ____________Soldier___________________________	
			}else if(rc.getType() == RobotType.SOLDIER){
				try{
					while (rc.isActive()) {
						MapLocation goTo = getRandLocation(rc);
						System.out.println("Goal of robot " + rc.getRobot().getID() + ": " + locToString(goTo));
						moveTo(rc, goTo);
						rc.yield();
					}
				}catch(GameActionException e){
					e.printStackTrace();
				}
			}
			rc.yield();
		}
	}
	
	public static String locToString(MapLocation loc){
		return "(" + loc.x + "," + loc.y + ")";
	}
	
	public static MapLocation getRandLocation(RobotController rc){
		int mapHeight = rc.getMapHeight();
		int mapwidth = rc.getMapWidth();
		MapLocation randLoc = null;
		boolean valid = false;
		while(!valid){
			int randX = rand.nextInt(mapwidth);
			int randY = rand.nextInt(mapHeight);
			randLoc = new MapLocation(randX, randY);
			if(rc.senseTerrainTile(randLoc) != TerrainTile.VOID){
				valid = true;
			}
		}
		
		return randLoc;
	}
	
	// TODO walk around obstacles like robots
	public static void moveTo(RobotController rc, MapLocation loc) throws GameActionException{
		System.out.println("Find path from " + locToString(rc.getLocation()) + " to " + locToString(loc));
		rc.setIndicatorString(0, "Searching path from " + locToString(rc.getLocation()) + " to " + locToString(loc));
		currentPath = A_Star.searchPathTo(loc, rc);
		rc.setIndicatorString(0, "Found path from " + locToString(rc.getLocation()) + " to " + locToString(loc));
		System.out.println("Found path: " + pathToString(currentPath));
		//rc.setIndicatorString(0, "Moving to " + locToString(loc));
		while(!currentPath.isEmpty()){
			while(rc.isActive()){
				MapLocation nextLoc = currentPath.getLast();
				currentPath.removeLast();
				rc.setIndicatorString(1, "Next loc: " + locToString(nextLoc));
				rc.setIndicatorString(2, "Current path: " + pathToString(currentPath));
				rc.move(rc.getLocation().directionTo(nextLoc));
			}
		}
		rc.setIndicatorString(0, "Goal " + locToString(loc) + " reached.");
		rc.setIndicatorString(0, "Waiting");
		rc.setIndicatorString(1, "");
	}
	
	public static String pathToString(LinkedList<MapLocation> path){
		String s = "";
		for(int i = 0; i < path.size(); ++i){
			s += locToString(path.get(i)) + " ";
		}
		return s;
	}
	
	public static Direction getRandomDir(RobotController rc){
		boolean valid = false;
		Direction randDir = null;
		
		while(!valid){
			int randNum = rand.nextInt(8);
			switch(randNum){
			case 0:
				randDir = Direction.NORTH;
				break;
			case 1:
				randDir = Direction.NORTH_EAST;
				break;
			case 2:
				randDir = Direction.EAST;
				break;
			case 3:
				randDir = Direction.SOUTH_EAST;
				break;
			case 4:
				randDir = Direction.SOUTH;
				break;
			case 5:
				randDir = Direction.SOUTH_WEST;
				break;
			case 6:
				randDir = Direction.WEST;
				break;
			case 7:
				randDir = Direction.NORTH_WEST;
				break;
			default:
				randDir = Direction.NORTH; // or something else, but that shouldn't happen anyway
				break;
			}
			
			if(rc.canMove(randDir)){
				valid =  true;
			}
		}
		
		System.out.println("Random direction to spawn: " + randDir);
		return randDir;
	}
}
