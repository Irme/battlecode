package communicator2004;

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
	
	public MapLocation getLocation(){
		return StaticFunctions.intToLoc(lifeTime);
	}
	
	public int getHealth(){
		return ((lifeTime/10000)%100)+1;
	}
}