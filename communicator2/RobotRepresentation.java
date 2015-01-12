package communicator2;

import battlecode.common.MapLocation;

public class RobotRepresentation{
	int iD;
	int lifeTime;
	boolean initilazie;
	MapLocation mL;
	public RobotRepresentation(int iD){
		this.initilazie = true;
		this.iD = iD;
		this.lifeTime = 0;
	}
}