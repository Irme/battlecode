package communicator2010;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class PastureBehavior {

	
	public static void run(RobotController rc) throws GameActionException{
		int data = rc.readBroadcast(StaticVariables.ROBOT_PASTR_ONE_CHANNEL);
		if(data == 0){
			rc.broadcast(StaticVariables.ROBOT_PASTR_ONE_CHANNEL, (int) rc.getHealth());
		}else{
			rc.broadcast(StaticVariables.ROBOT_PASTR_TWO_CHANNEL, (int) rc.getHealth());
		}
	}
}
