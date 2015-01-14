package communicator2005;

import battlecode.common.MapLocation;

public class RobotRepresentation{
	int iD;
	int lifeTime;
	boolean initilazie;
	MapLocation mL;
	public Group group;
	public RobotRepresentation(int iD){
		this.initilazie = true;
		this.iD = iD;
		this.lifeTime = 0;
	}
	
	public void notifyDeath(){
		group.notifyDeath(iD);
	}
}