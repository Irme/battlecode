package communicator2010;


import java.util.Arrays;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class GroupBehaviour {

	public Group group;
	int index;
	int counter;
	public MapLocation target,start;
	MapLocation[] path;
	RobotController rc;
	public int pressure;
	public double dis;
	public double progress;
	public GroupBehaviour(Group group){
		this.group = group;
		this.index = 1;
		this.counter = 0;
		target = null;
		start = null;
		path = null;
	}
	public double buildFarm(RobotController rc, MapLocation target, MapLocation start, double length) throws GameActionException{
		run(rc,target,start,length,true,0);
		sendBuildingCommand(rc,group,path[path.length-2],true);
		sendBuildingCommand(rc,group,path[path.length-3],false);
		broadCastMoveTypeToGroup(rc,group,true);
		return progress;
	}
	public double run(RobotController rc, MapLocation target, MapLocation start, double length, boolean building, int addDirectionToEnemyToEnd) throws GameActionException{
		if(!target.equals(this.target)){
			this.rc = rc;
			this.target = target;
			this.start = start;
			progress = 0;
			init(rc,target,start, length,addDirectionToEnemyToEnd);
		}
		dis = getAvarageDistanceToFormation(rc, group);
		pressure = readPressureOnGroup(group, rc) ;
		int groupID = group.iD;
		if(group.getSize() <= 7 && counter >= 6 && !building && index >= 8){
			index = Math.max(1, index-1);
			counter = 0;	
		}else if((pressure == 0 && counter >= 7 && dis <= 15) ||(dis < 1f||(dis < 8 && counter >= 20 && pressure == 0)) || counter >= 25){
			if(pressure == 0){
				rc.setIndicatorString(1,"GroupMovement is advancing");
				index = Math.min(index+1, path.length-1);
				counter = 0;
			}	
		}else{
			counter++;
			rc.setIndicatorString(1,"GroupMovement is not advancing " + "counter: " + counter);
		}
		int loc = Math.min(path.length-1, index);
		int to = Math.min(path.length-1, index+1);
		MapLocation enemyCenter = readEnemyCenter(group, rc);
		
		rc.setIndicatorString(0,Clock.getRoundNum() + " GroupMovement// avgDis: "+ dis + " targetLoc" + path[loc] + " index: " + loc + " groupID " + groupID + " pressure " + pressure);
		if(enemyCenter != null){
			sendLineFormationCommand(rc, group, path[loc], enemyCenter);
		}else{
			sendLineFormationCommand(rc, group, path[loc], path[to]);
		}
		progress = (double)index/(double)(path.length-1);
		return progress;
	}
	
	public void init(RobotController rc, MapLocation target, MapLocation start, double length, int add) throws GameActionException{
		System.out.println("init called with: " + start);
		Astar.init(rc);
		index = 1;
		counter = 0;
		path = Astar.search(rc, start, target);
		int to = Math.max(0, add);
		for(int i = 0; i < to; i ++){
			addOneToPath();
		}
		cutPath((int)(path.length*length));
		broadcastPath(rc,group.iD,path);
		broadCastPathToGroup(rc, group, group.iD, true);
		group.sendCommandToGroup(null, StaticVariables.COMMAND_FORMATION_MOVE, rc);
	}
	
	public void cutPath(int newLength){
		newLength = Math.min(path.length, newLength);
		if(newLength <= 0 || newLength == path.length){
			return;
		}
		MapLocation[] tmp = new MapLocation[newLength];
		for(int i = 0; i < newLength; i++){
			tmp[i] = path[i];
		}
		path = tmp;
	}
	public void broadCastPathToGroup(RobotController rc, Group group, int pathID, boolean forward) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(group.iD*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		rc.broadcast(channel+8, pathID+1);
		rc.broadcast(channel+9, forward?1:0);
	}
	public void broadCastMoveTypeToGroup(RobotController rc, Group group, boolean sneaking) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(group.iD*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		rc.broadcast(channel+19, sneaking?1:0);
	}
	public void broadcastPath(RobotController rc, int pathID, MapLocation[] path) throws GameActionException{
		int to = Math.min(998, path.length);
		int channel = StaticVariables.ROBOT_PATHS_DISTRIBUTION_CHANNEL_START+(pathID*1000);
		rc.broadcast(channel, path.length);
		for(int i = 0; i < to; i ++){
			rc.broadcast(channel+i+1, (path[i].x)*100+path[i].y);
		}
	}
	public void addOneToPath(){
		MapLocation[] tmp = new MapLocation[path.length+1];
		for(int i = 0; i < path.length; i ++){
			tmp[i] = path[i];
		}
		tmp[tmp.length-1] = path[path.length-1].add(path[path.length-1].directionTo(rc.senseEnemyHQLocation()));
		path = tmp;
	}
	public double getAvarageDistanceToFormation(RobotController rc, Group group) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(group.iD*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		double counter = rc.readBroadcast(channel+10);
		double sum = rc.readBroadcast(channel+11);
	
		if(Clock.getRoundNum() % 6== 0){
			rc.broadcast(channel+10, 0);
			rc.broadcast(channel+11, 0);
		}
		if(counter == 0){
			return 100;
		}
		return sum/counter;
	}
	public int readPressureOnGroup(Group group, RobotController rc) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(group.iD*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		int result = rc.readBroadcast(channel+4);
		if(Clock.getRoundNum() % 3==0){
			rc.broadcast(channel+4, 0);
		}
		return result;
	}
	public MapLocation readEnemyCenter(Group group, RobotController rc) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(group.iD*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		float counter = rc.readBroadcast(channel+5);
		if(counter == 0){
			return null;
		}
		float x = rc.readBroadcast(channel+6);
		float y = rc.readBroadcast(channel+7);
		rc.broadcast(channel+5,0);
		rc.broadcast(channel+6,0);
		rc.broadcast(channel+7,0);
		return new MapLocation(Math.round(x/counter),Math.round(y/counter));
	}
	public void sendLineFormationCommand(RobotController rc,Group group, MapLocation middle, MapLocation target) throws GameActionException{
		MapLocation[] result = StaticFunctions.getLineEndPoints(rc, middle	, target);
		sendLineFormation(group.iD, rc, result[0], result[1], result[2],target);
		sendPathIndex(rc,group);
//		System.out.println(result[0] + " "+ middle + " "  + result[1]);
//		group.sendCommandToGroup(result[2], StaticVariables.COMMAND_FORMATION_MOVE, rc);
	}
	public void sendLineFormation(int groupID, RobotController rc,
			MapLocation p1, MapLocation p2, MapLocation middle,MapLocation target)
			throws GameActionException {
		// if(Clock.getRoundNum() < 20 ){
		// System.out.println(p1 + " " + p2 + " " + middle);
		// }

		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START
				+ (groupID * StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		rc.broadcast(channel, StaticFunctions.locToInt(p1));
		rc.broadcast(channel + 1, StaticFunctions.locToInt(p2));
		rc.broadcast(channel + 2, StaticFunctions.locToInt(middle));
		rc.broadcast(channel + 3, StaticFunctions.locToInt(target));
		
	}
	public void sendPathIndex(RobotController rc, Group group) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(group.iD*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		rc.broadcast(channel+12, index);
	}
	public void sendBuildCommandIfAppropiate(RobotController rc) throws GameActionException{
		if(progress == 1 && dis <= 8 && pressure == 0 && group.getSize() >= 10){
			sendBuildingCommand(rc,group,path[path.length-2],true);
			sendBuildingCommand(rc,group,path[path.length-3],false);
			broadCastMoveTypeToGroup(rc,group,true);
		}else{
			eraseBuildingCommand(rc,group);
		}
	}
	public boolean isBuilded(RobotController rc, boolean pastr) throws GameActionException{
		
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(group.iD*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		if(pastr){
			return rc.readBroadcast(channel+13) == 2;
		}else{
			return rc.readBroadcast(channel+16) == 2;
		}
		
	}
	public void sendBuildingCommand(RobotController rc, Group group, MapLocation target, boolean pastr) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(group.iD*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		if(pastr){
			rc.broadcast(channel+13, 1);
			rc.broadcast(channel+14, target.x);
			rc.broadcast(channel+15, target.y);
		}else{
			rc.broadcast(channel+16, 1);
			rc.broadcast(channel+17, target.x);
			rc.broadcast(channel+18, target.y);
		}
	}
	public void eraseBuildingCommand(RobotController rc, Group group) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(group.iD*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		rc.broadcast(channel+13, 0);
		rc.broadcast(channel+14, 0);
		rc.broadcast(channel+15, 0);
		rc.broadcast(channel+16, 0);
		rc.broadcast(channel+17, 0);
		rc.broadcast(channel+18, 0);
	}
}
