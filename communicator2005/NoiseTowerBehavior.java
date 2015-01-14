package communicator2005;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class NoiseTowerBehavior {

	public static MapLocation shooting;
	public static boolean init = true;
	public static int counter;
	public static int ordinal;
	public static MapLocation loc;
	public static void run(RobotController rc) throws GameActionException{
		if(init){
			init(rc);
			init = false;
		}
		if(counter <= 0){
			rotate(rc);
			if(ordinal % 2== 0){
				counter = 13;
			}else{
				counter = 11;
			}
		}
		if(rc.isActive()){
			rc.attackSquareLight(shooting);
			decrement(rc);
		}
	}
	public static void decrement(RobotController rc){
		shooting = shooting.add(shooting.directionTo(loc));
		counter --;
	}
	public static void rotate(RobotController rc){
		switch(ordinal){
		case 0:
			shooting = new MapLocation(loc.x+12,loc.y-12);
			ordinal ++;
			break;
		case 1:
			shooting = new MapLocation(loc.x+17,loc.y);
			ordinal ++;
			break;
		case 2:
			ordinal ++;
			shooting = new MapLocation(loc.x+12,loc.y+12);
			break;
		case 3:
			ordinal ++;
			shooting = new MapLocation(loc.x,loc.y+17);
			break;
		case 4:
			shooting = new MapLocation(loc.x-12,loc.y+12);
			ordinal ++;
			break;
		case 5:
			shooting = new MapLocation(loc.x-17,loc.y);
			ordinal ++;
			break;
		case 6:
			shooting = new MapLocation(loc.x-12,loc.y-12);
			ordinal ++;
			break;
		case 7:
			shooting = new MapLocation(loc.x,loc.y-17);
			ordinal = 0;
			break;
		}
	}
	public static void init(RobotController rc){
		loc = rc.getLocation();
		counter = 13;
		ordinal = 0;
		shooting = new MapLocation(loc.x,loc.y-17);
	}
}
