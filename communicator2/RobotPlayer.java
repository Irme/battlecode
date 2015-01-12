package communicator2;
import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	static int roundCount = 0;

	
	public static void run(RobotController rc) {	
		OpponentModel om = new OpponentModel();
		RobotType type = rc.getType();
		try{
			switch (type) {
			case HQ:
				while(true){
					HQBehavior.hqBehavior(rc);
					roundCount++;
					rc.yield();
				}
			case SOLDIER:
				while(true){
					om.scoutingData(rc);
					SoldierBehavior.run(rc);
					rc.yield();
				}
			case PASTR:
				while(true){
					om.pastureData(rc);
					rc.yield();
				}
			default:
				while(true){
					rc.yield();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
