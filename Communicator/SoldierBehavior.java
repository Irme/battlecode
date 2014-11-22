package Communicator;

import java.util.Random;

import battlecode.common.*;

public class SoldierBehavior {
	
	
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	
	static int[][] map;
	static int width,height;
	static int currentCommand;
	static MapLocation target;
	static MapLocation backup;
	
	/**
	 * These states are representing the the logical areas of a soldier.
	 */
	private enum SoldierState{
		SPAWN,WAITING_FOR_COMMAND,BUILD_PASTR, ASSEMBLE, FLEEING,SCOUTING;
	}
	
	/**
	 * A lifecount of the soldier which is incremented every round and delivered to the HQ so that it knows that the 
	 * soldier is still alive.
	 */
	static int count = 0;
	
	/** the internal state of the soldier**/
	public static SoldierState state = SoldierState.SPAWN;
	
	/** the ID of the soldier which is necessary for the communication **/
	public static int iD;
	
	/**
	 * The entire logic of the soldier which is processed every round
	 * @param rc
	 * @throws Exception
	 */
	public static void run(RobotController rc) throws Exception{
		count = count % 10;
		
		// if the health of the soldier is below a certain threshold than switch to a fleeing state.
		if(rc.getHealth() <= StaticVariables.ROBOT_FLEEING_HEALTH_THRESHOLD){
			state = SoldierState.FLEEING;
		}
		
		// If the soldier is fleeing he tries to get back to the location he spawned.
		// if he regenerated enough health to have more than a certain threshold, he switches to the waiting-for-command state.
		if(state == SoldierState.FLEEING){
			count ++;
			broadcastFeedback(rc);
			SnailTrail.tryToMove(backup, rc);
			if(rc.getHealth() >= StaticVariables.ROBOT_RECOVERING_HEALTH_THRESHOLD){
				state = SoldierState.WAITING_FOR_COMMAND;
			}
		}
		
		//This is the first state a soldier starts with he moves randomly until he gets his ID from the HQ.
		//Then he switches to the waiting-for-command state.
		if(state == SoldierState.SPAWN){
			if(!tryToShoot(rc)){
				moveRandomly(rc);
			}
			iD = obtainID(rc);
			if(iD != 0){
				state = SoldierState.WAITING_FOR_COMMAND;
				backup = rc.getLocation();
			}
		}
		//The soldier moves randomly until he gets a command from the HQ.
		else if(state == SoldierState.WAITING_FOR_COMMAND){
			count ++;
			broadcastFeedback(rc);
			if(!tryToShoot(rc)){
				moveRandomly(rc);	
				lookForCommand(rc);
			}
		}else if
		//The soldier moves to a certain location to scout for enemies
		//He flees if the enemy is in shooting range
		(state == SoldierState.SCOUTING){
			count ++;
			broadcastFeedback(rc);
			int look = lookForNearestEnemySoldier(rc);
			if(look == 1000){
				SnailTrail.tryToMove(target, rc);
			}else if(look < StaticVariables.ROBOT_SCOUTING_DISTANCE_THRESHOLD){
				SnailTrail.tryToMove(backup, rc);
			}
			lookForCommand(rc);
		}
		//The soldier moves to a certain position he got from a command.
		//Atm assembling and attacking is the same because they only move to a target location
		//and attack everything in range.
		//the robot changes his internal-state if he gets a new command.
		else if(SoldierState.ASSEMBLE == state){
			count ++;
			broadcastFeedback(rc);
			if(rc.isActive()){
				if(!tryToShoot(rc)){
					SnailTrail.tryToMove(target, rc);
					lookForCommand(rc);
				}
			}
		}
		//In this state a soldier builds a pasture if he is at the target location
		//Looks always for new commands.
		else if(SoldierState.BUILD_PASTR == state){
			count ++;
			broadcastFeedback(rc);
			if(!tryToShoot(rc)){
				if(rc.getLocation().equals(target) && rc.isActive()){
					rc.construct(RobotType.PASTR);
				}	
				SnailTrail.tryToMove(target, rc);
				lookForCommand(rc);
				
			}
		}
	}
	
	/**
	 * The soldier reads the command-channel which is indexed by the soldiers ID.
	 * If the soldier receives a new command the soldier updates his internal state accordingly to the new command.
	 * The command incorporates the target maplocation and a command-type like assembling or building a pasture.
	 * @param rc
	 * @throws Exception
	 */
	public static void lookForCommand(RobotController rc) throws Exception{
		int command = rc.readBroadcast(StaticVariables.ROBOT_COMMAND_CHANNEL_START+(iD-1));
		if(command != currentCommand){
			currentCommand = command;
			target = StaticFunctions.intToLoc(command);
			state = interpreteCommand(command/10000);
		}
	}
	
	/**
	 * Writes the current maplocation and lifecount into a feedback channel which is analyzed by the HQ.
	 * @param rc
	 * @throws Exception
	 */
	public static void broadcastFeedback(RobotController rc) throws Exception{
		MapLocation mL = rc.senseRobotInfo(rc.getRobot()).location;
		rc.broadcast(StaticVariables.ROBOT_FEEDBACK_CHANNEL_START+(iD-1), (count + (mL.y*10)+(mL.x*1000)));
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
	
	/**
	 * This function is deprecated, its use depends on future development decisions.
	 * This function reads the tile information from the map that was broadcasted by the HQ.
	 * It ranges from channel 0 to 10000 which supports the maximum map-size of 100x100.
	 * @param rc
	 * @throws Exception
	 */
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
	
	
	public static int lookForNearestEnemySoldier(RobotController rc) throws GameActionException{
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		int min = 1000;
		for(int i = 0; i < enemyRobots.length; i ++){
			RobotInfo anEnemyInfo = rc.senseRobotInfo(enemyRobots[0]);
			int curr = anEnemyInfo.location.distanceSquaredTo(rc.getLocation());
			if(curr < min && anEnemyInfo.type == RobotType.SOLDIER){
				min = curr;
			}
		}
		return min;
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
	
	/**
	 * Returns the internal-state that results from a given command.
	 * Attacking = Assembling, because at the moment there is no difference.
	 * @param command
	 * @return
	 */
	public static SoldierState interpreteCommand(int command){
		SoldierState next = SoldierState.WAITING_FOR_COMMAND;
		switch(command){
		case StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION:
			return SoldierState.ASSEMBLE;
		case StaticVariables.COMMAND_ATTACK_LOCATION:
			return SoldierState.ASSEMBLE;
		case StaticVariables.COMMAND_BUILD_PASTR:
			return SoldierState.BUILD_PASTR;
		case StaticVariables.COMMAND_SCOUT_LOCATION:
			return SoldierState.SCOUTING;
		}
		return next;
	}
	
	/**
	 * Interpreters the content of the ID-channel. If its not a 0 or 1 than
	 * It returns the value as the new ID of this robot.
	 * @param rc
	 * @return returns the ID obtained from the ID-channel.
	 * @throws Exception
	 */
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
