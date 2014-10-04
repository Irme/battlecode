package communicator;

import java.util.Random;

import battlecode.common.*;

public class SoldierBehavior {
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static int[][] map;
	static int width,height;
	static MapLocation target;
	static MapLocation backup;
	private enum SoldierState{
		SPAWN,WAITING_FOR_COMMAND,BUILD_PASTR, ASSEMBLE, FLEEING;
	}
	static int count = 0;
	public static SoldierState state = SoldierState.SPAWN;
	public static int iD;
	public static void run(RobotController rc) throws Exception{
		if(rc.getHealth() <= StaticVariables.ROBOT_FLEEING_HEALTH_THRESHOLD){
			state = SoldierState.FLEEING;
		}
		if(state == SoldierState.FLEEING){
			count ++;
			broadcastFeedback(rc);
			if(!moveToLoc(backup,rc)){
				moveRandomly(rc);
			}
			if(rc.getHealth() >= StaticVariables.ROBOT_RECOVERING_HEALTH_THRESHOLD){
				state = SoldierState.WAITING_FOR_COMMAND;
			}
		}
		if(state == SoldierState.SPAWN){
			if(!tryToShoot(rc)){
				moveRandomly(rc);
			}
			iD = obtainID(rc);
			if(iD != 0){
//				System.out.println("new id obtained: " + iD + " roundCount: " + Clock.getRoundNum());
				state = SoldierState.WAITING_FOR_COMMAND;
				backup = rc.getLocation();
			}
		}else if(state == SoldierState.WAITING_FOR_COMMAND){
			count ++;
			broadcastFeedback(rc);
			if(!tryToShoot(rc)){
				moveRandomly(rc);	
				int command = rc.readBroadcast(11000+(iD-1));
				target = StaticFunctions.intToLoc(command);
				state = interpreteCommand(command/10000);	
			}
		}else if(SoldierState.ASSEMBLE == state){
//			System.out.println("prev: " + Clock.getBytecodeNum());
			count ++;
			broadcastFeedback(rc);
			if(rc.isActive()){
				if(!tryToShoot(rc)){
					if(!moveToLoc(target,rc)){
						moveRandomly(rc);
					}
					if(count % 3 == 0){
						int command = rc.readBroadcast(11000+(iD-1));
						target = StaticFunctions.intToLoc(command);
						state = interpreteCommand(command/10000);
					}
				}
			}
//			System.out.println("after: " + Clock.getBytecodeNum());
		}else if(SoldierState.BUILD_PASTR == state){
			count ++;
			broadcastFeedback(rc);
			if(!tryToShoot(rc)){
				if(rc.getLocation().equals(target) && rc.isActive()){
					rc.construct(RobotType.PASTR);
//					System.out.println(iD + " tries to construct PASTR");
				}
				if(!moveToLoc(target,rc)){
					moveRandomly(rc);
				}	
				if(count % 3 == 0){
					int command = rc.readBroadcast(11000+(iD-1));
					target = StaticFunctions.intToLoc(command);
					state = interpreteCommand(command/10000);
				}
			}
		}
	}
	public static void broadcastFeedback(RobotController rc) throws Exception{
		rc.broadcast(StaticVariables.ROBOT_FEEDBACK_CHANNEL_START+(iD-1), count);
	}
	public static void moveRandomly(RobotController rc) throws Exception{
		Direction moveDirection = directions[(int) (Math.random()*8)];
		if (rc.isActive() && rc.canMove(moveDirection)) {
			rc.sneak(moveDirection);
		}
	}
	public static void moveToEnemyHQ(RobotController rc) throws Exception{
		Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		if (rc.isActive() && rc.canMove(toEnemy)) {
			rc.sneak(toEnemy);
		}
	}
	public static boolean moveToLoc(MapLocation loc, RobotController rc) throws Exception{
		Direction toDest = rc.getLocation().directionTo(loc);
		if (rc.isActive() && rc.canMove(toDest)) {
			rc.sneak(toDest);
			return true;
		}else{
			return false;
		}
	}
	public static void updateInternalMap(RobotController rc) throws Exception{
		width = rc.getMapWidth();
		height = rc.getMapHeight();
		map = new int[width][height];
		int index = 0;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				map[x][y] = rc.readBroadcast(index);
				index++;
			}
		}
	}
	private static boolean tryToShoot(RobotController rc) throws Exception {
		if(rc.isActive()){
			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
			
			if(enemyRobots.length > 0){
				for(int i = 0; i < enemyRobots.length; i ++){
					Robot current = enemyRobots[i];
					RobotInfo anEnemyInfo;
					anEnemyInfo = rc.senseRobotInfo(current);
					if(anEnemyInfo.type != RobotType.HQ && anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
						rc.attackSquare(anEnemyInfo.location);
						return true;
						
					}
				}
			}
		}
		return false;
	}
	public static SoldierState interpreteCommand(int command){
		SoldierState next = SoldierState.WAITING_FOR_COMMAND;
		switch(command){
		case StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION:
			return SoldierState.ASSEMBLE;
		case StaticVariables.COMMAND_BUILD_PASTR:
			return SoldierState.BUILD_PASTR;
		}
		return next;
	}
	public static int obtainID(RobotController rc) throws Exception{
		int id = rc.readBroadcast(StaticVariables.ROBOT_ID_CHANNEL)/10;
		if(id == 0){
			rc.broadcast(StaticVariables.ROBOT_ID_CHANNEL, 1);
		}else{
			rc.broadcast(StaticVariables.ROBOT_ID_CHANNEL, 0);
		}
		return id;
	}
}
