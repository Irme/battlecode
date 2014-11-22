package Communicator;


import battlecode.common.RobotController;

public class PastrBehaviour {
	
	/** 
	 * Simple pastr logic such that we can use their health for the opponent model.
	 * @param rc
	 */
	public static void Pastr(RobotController rc) {
		OpponentModel om = new OpponentModel();
		om.pastureData(rc);
		
	}


}
