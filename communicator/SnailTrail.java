package communicator;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SnailTrail {

	static int[] lookingOrdinals = {0, 1, 2, 3, -1, -2, -3, 4};
	static Direction[] directions = Direction.values();
	static ArrayList<MapLocation> snailTrail = new ArrayList<MapLocation>();
	static Random rand = new Random();
	public static void tryToMove(Direction chosenDir, RobotController rc) throws GameActionException {
		while (snailTrail.size() < 5)
			snailTrail.add(new MapLocation(-1, -1));
		
		if (rc.isActive()) {
			snailTrail.remove(0);
			snailTrail.add(rc.getLocation());
			
			int lookingDir = 2*rand.nextInt(2) - 1;
			int forwardOrd = chosenDir.ordinal(); 
			
			for (int ordOffset : lookingOrdinals) {
				Direction trialDir = directions[(forwardOrd + lookingDir*ordOffset + 8)%8];
				boolean canMove = true; 
				if (rc.canMove(trialDir)) {
					MapLocation resultingLocation = rc.getLocation().add(trialDir);
					for (MapLocation m : snailTrail) {
						if (!m.equals(rc.getLocation())) {
							if (resultingLocation.isAdjacentTo(m) || resultingLocation.equals(m)) {
								break; 
							}
						}		
					}
					if(canMove) {
						rc.move(trialDir);
						break; 
					}
				}	
			}
		}
	}
	
	public static void tryToMove(MapLocation chosenLoc, RobotController rc) throws GameActionException {
		Direction chosenDir = rc.getLocation().directionTo(chosenLoc);
		tryToMove(chosenDir, rc);
	}
	
	
}