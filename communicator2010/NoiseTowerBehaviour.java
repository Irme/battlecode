package communicator2010;

import java.util.Arrays;
import java.util.LinkedList;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class NoiseTowerBehaviour {
	private enum State {
		INIT, DEFAULT;
	}

	public static MapLocation[][] shootingDirs = new MapLocation[8][20];
	public static int[] locAmount = new int[8]; 
	public static State state = State.INIT; 
	public static RobotController rc; 
	public static int mapHeight; 
	public static int mapWidth; 
	public static MapLocation here; 
	public static int maxShootingDist = RobotType.NOISETOWER.attackRadiusMaxSquared;
	public static int minShootingDist = 3;
	public static double[][] cows; 
	public static int ordinal = 0; 
	public static int index = 0;
	public static void run(RobotController rcin) throws GameActionException {
		switch (state) {
		case INIT:
			rc = rcin;
			init();
			state = State.DEFAULT; 
			break;
		case DEFAULT:
			turn();
			break;
		}
	}

	public static void init() throws GameActionException {
		Robot[] types = rc.senseNearbyGameObjects(Robot.class,1,rc.getTeam());
		RobotInfo pastrInfo = null; 
		for (int i = 0; i < types.length; i++) {
			RobotInfo rinfo = rc.senseRobotInfo(types[i]);
			if (rinfo.type == RobotType.PASTR) {
				pastrInfo = rinfo;
				break; 
			}
		}
		if (pastrInfo != null) 
			here = pastrInfo.location; 
		else 
			here = rc.getLocation(); 

		mapHeight = rc.getMapHeight(); 
		mapWidth = rc.getMapWidth(); 
		cows = rc.senseCowGrowth();
		Direction[] dirs = Direction.values(); 
		for (int i = 0; i < 8; i++) {
			MapLocation curr = here;
			MapLocation lastLocation = curr;
			int counter = 0; 
			int dist; 
			for (int j = 0; j < 20; j++) {
				curr = curr.add(dirs[i]);
				dist = curr.distanceSquaredTo(here);
				
				if (dist <= 5)
					continue; 
				if (rc.senseTerrainTile(lastLocation) == TerrainTile.VOID || rc.senseTerrainTile(lastLocation) == TerrainTile.OFF_MAP)
					break; 
				
				if (dist >= maxShootingDist)
					break; 
				
//				if (cows[lastLocation.x][lastLocation.y] == 0)
//					break;
//				
				if (!rc.canAttackSquare(curr))
					break; 
				
				shootingDirs[i][counter] = curr;
				counter++; 
				lastLocation = curr; 
			}
			if (rc.canAttackSquare(curr.add(dirs[i]))) {
				shootingDirs[i][counter + 1] = curr.add(dirs[i]);
				counter++; 
			}
			locAmount[i] = counter; 
		}
		index = locAmount[0] - 1;
	}

	public static void turn() throws GameActionException {
		
		if (rc.isActive()) {
			if (index >= 0 && shootingDirs[ordinal][index] != null) 
				rc.attackSquareLight(shootingDirs[ordinal][index]);
			index--; 
		}
		
		if (index <= 0) {
			ordinal = (ordinal + 1)%8;
			index = locAmount[ordinal];
		}
	}
}
