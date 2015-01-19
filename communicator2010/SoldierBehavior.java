package communicator2010;

import java.util.Random;

import battlecode.common.*;

public class SoldierBehavior {
	
	
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	static int lastUpdate = 0;
	static int[][] map;
	static int width,height;
	static int currentCommand;
	static MapLocation target;
	static MapLocation backup;
	static MapLocation thisPos;
	static Random rand;
	static int groupNum;
	static MapLocation linePoint1, linePoint2, lineMiddlePoint, formationTarget;
	static int pathID;
	static int pathProgress;
	static boolean forward = true;
	static boolean inFormation = false;
	static boolean checkForPathID = false;
	static boolean sneaking = false;
	
	static int healthOurs;
	static int healthThem;
	static int numUs;
	static int numThem;
	
	/**
	 * These states are representing the the logical areas of a soldier.
	 */
	private enum SoldierState{
		SPAWN,WAITING_FOR_COMMAND,BUILD_PASTR, ASSEMBLE, FLEEING,SCOUTING,GROUP_MOVEMENT,FARMING_MODE, FORMATION_MOVE,BUILD_NOISE_TOWER, SUICIDE_MISSION, FOLLOWING_PATH;
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
	
	public static int health;
	
	public static RobotInfo[] enemyRobots;
	public static RobotInfo[] enemiesInShootingRange;
	static MapLocation suicideLoc;
	static boolean attacked;
	static boolean buildsPastr = false;
	/**
	 * The entire logic of the soldier which is processed every round
	 * @param rc
	 * @throws Exception
	 */
	public static void run(RobotController rc) throws Exception{
		if(rc.isConstructing()){
			if(rc.getActionDelay() <= 2){
				System.out.println("buildingDone " + buildsPastr);
				notifyBuild(rc, buildsPastr);
			}
			return;
		}
		getEnemies(rc);
		thisPos = rc.getLocation();

		rc.setIndicatorString(2, state.toString() + " " + iD + " " +  lineMiddlePoint + " groupID: " + groupNum + " " + currentCommand + " clock " + Clock.getRoundNum());
		if(state == SoldierState.SUICIDE_MISSION){
			MapLocation loc = thisPos;
			Direction dir = loc.directionTo(suicideLoc);
			if(thisPos.equals(suicideLoc)||!rc.canMove(dir)){
				rc.yield();
				rc.selfDestruct();
			}
		
			if (rc.isActive()) {
				if (rc.canMove(dir)) {
					rc.move(dir);
					loc = loc.add(dir);
				}
				
			}

			dir = loc.directionTo(suicideLoc);
			if(loc.equals(suicideLoc)||!rc.canMove(dir)){
				rc.yield();
				rc.selfDestruct();
			}
			return;
		}
		
		count = count % 100;
		int temp = health;
		health = (int) rc.getHealth();
		if(temp > health){
			attacked = true;
		}
		
		//rc.setIndicatorString(0, ""+ currentCommand + " " + (state == SoldierState.SPAWN?"waitsforID":"") + (state == SoldierState.WAITING_FOR_COMMAND?"waitsforCommand":"") +  (state == SoldierState.BUILD_PASTR?"tries to build pastr":""));
		// if the health of the soldier is below a certain threshold than switch to a fleeing state.
		if(rc.getHealth() <= StaticVariables.ROBOT_FLEEING_HEALTH_THRESHOLD){
			state = SoldierState.FLEEING;
			currentCommand = 0;
		}
		
		// If the soldier is fleeing he tries to get back to the location he spawned.
		// if he regenerated enough health to have more than a certain threshold, he switches to the waiting-for-command state.
		if(state == SoldierState.FLEEING){
			count ++;
			broadcastFeedback(rc);
			BugMove.move(backup, rc, sneaking);
			if(rc.getHealth() >= StaticVariables.ROBOT_RECOVERING_HEALTH_THRESHOLD){
				state = SoldierState.WAITING_FOR_COMMAND;
			}
		}
		
		//This is the first state a soldier starts with he moves randomly until he gets his ID from the HQ.
		//Then he switches to the waiting-for-command state.
		if(state == SoldierState.SPAWN){
			if(!tryToShoot(rc)){
				if(rand == null){
					rand = new Random(rc.getRobot().getID());
				}
				moveRandomly(rc);
			}
			iD = obtainID(rc);
			if(iD != 0){
				rand = new Random(iD);
				state = SoldierState.WAITING_FOR_COMMAND;
				backup = thisPos;
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
				BugMove.move(target, rc, sneaking);
			}else if(look < StaticVariables.ROBOT_SCOUTING_DISTANCE_THRESHOLD){

				BugMove.move(backup, rc, sneaking);
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
					BugMove.move(target, rc, sneaking);
					lookForCommand(rc);
				}
			}
		}
		//In this state a soldier builds a pasture if he is at the target location
		//Looks always for new commands.
		else if(SoldierState.BUILD_PASTR == state){
			if(rc.isConstructing()){
				return;
			}
			count ++;
			broadcastFeedback(rc);
			if(!tryToShoot(rc)){
				if(thisPos.equals(target) && rc.isActive()){
					rc.construct(RobotType.PASTR);
					buildsPastr = true;
				}	
				BugMove.move(target, rc, sneaking);
				lookForCommand(rc);
				
			}
		}
		// This is almost the same behavior as for ASSEMBLE. The difference is that the soldier sometimes
		// moves randomly based on StaticVariables.PROBABILITY_GROUP_MOVE_RANDOM_MOVE
		else if(SoldierState.GROUP_MOVEMENT == state){
			count ++;
			broadcastFeedback(rc);
			if(rc.isActive()){
				if(!tryToShoot(rc)){
					if(rand.nextFloat() < StaticVariables.PROBABILITY_GROUP_MOVE_RANDOM_MOVE){
						moveRandomly(rc);
					}else{
						BugMove.move(target, rc, sneaking);
					}
					lookForCommand(rc);
				}
			}
		}
		
		//
		else if(SoldierState.FARMING_MODE == state){
			count ++;
			broadcastFeedback(rc);
			if(rc.isActive()){
				if(!tryToShoot(rc)){
					if(rand.nextFloat() < 0.6f){
						shootToCollectCows(rc,target);
					}
					if(rand.nextFloat() < 0.5){
						sneakRandomly(rc);
					}else{
						BugMove.move(target, rc,sneaking);
					}
					lookForCommand(rc);
				}
			}
		}
		else if(SoldierState.FORMATION_MOVE == state){
			count ++;
			broadcastFeedback(rc);
//			rc.setIndicatorString(1,Clock.getRoundNum() +   "isInFormationMoveState " + lastUpdate + " " + broadcastFeedback(rc));
			broadPressureOnGroup(rc);
			broadCastEnemyCenter(rc);
			if(constructIfPossible(rc)){
				return;
			}
			if(rc.isActive()){
//				if(rc.getHealth() < 00){
//					suicideLoc = lookForSuicidePosition(rc);
//					if(suicideLoc != null && manhattenDistance(rc.getLocation(), suicideLoc)<=3){
//						state = SoldierState.SUICIDE_MISSION;
//						return;
//					}
//				}
				getGroupPowers(rc);
				rc.setIndicatorString(0, "healthUs: " + healthOurs + " healthThem: " + healthThem + " numUs: " + numUs + " numThem: " + numThem);
				if(enemyRobots.length > 0){
					double ourPower = (double)healthOurs/(double)numThem; 
					double theirPower = (double)healthThem/(double)numUs;
					if(ourPower < (theirPower*StaticVariables.MULTIPLICATOR_GROUP_POWER)){
						MapLocation enemyLoc = getFleeingTarget(rc);

						rc.setIndicatorString(0, Clock.getRoundNum() + " tries to flee from: " + enemyLoc);
						if(tryToMoveAway(rc,enemyLoc)){
							lookForCommand(rc);
							rc.setIndicatorString(0, "fled " + enemyLoc);
							return;
						}else{
							tryToShoot(rc);
							lookForCommand(rc);
							return;
						}
					}
					RobotInfo enemyInfo = enemyRobots[closestSoldier(rc,7)];
////					System.out.println(iD + " " +manhattenDistance(rc.getLocation(), enemyInfo.location) + " " + enemyInfo.robot.getID());
					int distance = manhattenDistance(rc.getLocation(), enemyInfo.location);
					if(distance <= 2 || (distance <= 6 && enemyInfo.type == RobotType.HQ) /**|| (health <= StaticVariables.ROBOT_RETREATING_HEALTH_THRESHOLD && distance <= 7)*/){
						MapLocation enemyLoc = getFleeingTarget(rc);
						if(tryToMoveAway( rc,enemyLoc)){
							lookForCommand(rc);
							return;
						}else{
							tryToShoot(rc);
							lookForCommand(rc);
							return;
						}
					}
				}
				tryToShoot(rc);
				formationMove(rc);
			}
			lookForCommand(rc);
		}
		else if(SoldierState.BUILD_NOISE_TOWER == state){
			if(rc.isConstructing()){
				return;
			}
			count ++;
			broadcastFeedback(rc);
			if(!tryToShoot(rc)){
				if(rc.getLocation().equals(target) && rc.isActive()){
					rc.construct(RobotType.NOISETOWER);
				}	
				BugMove.move(target, rc,sneaking);
				lookForCommand(rc);
				
			}
		}
		else if(SoldierState.FOLLOWING_PATH == state){
			count ++;
			broadcastFeedback(rc);
			tryToShoot(rc);
			followPath(rc);
			lookForCommand(rc);	
		}

	}
	public static MapLocation getFleeingTarget(RobotController rc){
		float x = 0;
		float y = 0;
		float count = enemyRobots.length;
		for(int i = 0; i < enemyRobots.length; i ++){
			MapLocation loc = enemyRobots[i].location;
			x+= loc.x;
			y+= loc.y;
		}
		return new MapLocation((int)(x/count),(int) (y/count));
	}
	public static void getPathIDForLineFormation(RobotController rc) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		int data = rc.readBroadcast(channel+8);
		if(data <= 0){
			return;
		}
		if(pathID != data-1){
			pathProgress = 1;
		}
		pathID = data-1;
		forward = rc.readBroadcast(channel+9)==1;
	}
	public static MapLocation readEnemyCenter( RobotController rc) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		float counter = rc.readBroadcast(channel+5);
		if(counter == 0){
			return null;
		}
		float x = rc.readBroadcast(channel+6);
		float y = rc.readBroadcast(channel+7);
		return new MapLocation(Math.round(x/counter),Math.round(y/counter));
	}
	public static void broadCastEnemyCenter(RobotController rc) throws GameActionException{
		int to = Math.min(3, enemyRobots.length);

//		rc.setIndicatorString(2, "enemyCenter not send");
		if(to == 0){
			return;
		}
//		rc.setIndicatorString(2, Clock.getRoundNum() + " enemyCenter send " + to);
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		int x = rc.readBroadcast(channel+6);
		int y = rc.readBroadcast(channel+7);
		int counter = rc.readBroadcast(channel+5);
		for(int i = 0; i < to; i ++){
			RobotInfo rI = enemyRobots[i];
			if(rI.type == RobotType.SOLDIER){
				x += rI.location.x;
				y += rI.location.y;
				counter ++;
			}
		}
		rc.broadcast(channel+5, counter);
		rc.broadcast(channel+6, x);
		rc.broadcast(channel+7, y);
	}
	public static void followPath(RobotController rc) throws GameActionException{
//		rc.setIndicatorString(2, "followsPath " + forward);
		
		if(rc.isActive()){
			
			int channel = StaticVariables.ROBOT_PATHS_DISTRIBUTION_CHANNEL_START+(1000*pathID);
			int pathLength = rc.readBroadcast(channel);
			MapLocation end = StaticFunctions.intToLoc(rc.readBroadcast(channel+pathLength));
			if(end.equals(rc.getLocation())){
				return;
			}
			MapLocation next = StaticFunctions.intToLoc(rc.readBroadcast(channel+1+pathProgress));
//			rc.setIndicatorString(1,"followsPath: " +  next.toString() + " progress " + pathProgress + " length " + pathLength + " pathID " + pathID);
			Direction dir = rc.getLocation().directionTo(next);
			if(!rc.canMove(dir)){

				if(rc.getLocation().distanceSquaredTo(next) < rc.getType().sensorRadiusSquared){
					Robot r = (Robot) rc.senseObjectAtLocation(next);
					if(r!= null){
						RobotInfo rI = rc.senseRobotInfo(r);
						if(rI.type == RobotType.PASTR || rI.type == RobotType.NOISETOWER|| rI.type == RobotType.HQ){
							if(forward){
								pathProgress++;
								pathProgress = Math.min(pathLength-1, pathProgress);
							}else{
								pathProgress--;
								pathProgress = Math.max(0, pathProgress);	
							}
						}
					}
				}
			}
			
			Direction dir2 = BugMove.moveHelper(next, rc,sneaking);
			if(dir2 != null){
				MapLocation target = rc.getLocation().add(dir2);
				if(getGroupPowersForFutureMove(rc, target) > 1){
					if(sneaking){
						rc.sneak(dir2);
					}else{
						rc.move(dir2);
					}
				}
			}
//			BugMove.move(next, rc,sneaking);
			
			if(rc.getLocation().equals(next)){
				if(forward){
					pathProgress++;
					pathProgress = Math.min(pathLength-1, pathProgress);
				}else{
					pathProgress--;
					pathProgress = Math.max(0, pathProgress);	
				}
				return;
			}
			next = StaticFunctions.intToLoc(rc.readBroadcast(channel+2+pathProgress));
			if(rc.getLocation().equals(next)){
				if(forward){
					pathProgress++;
					pathProgress = Math.min(pathLength-1, pathProgress);
				}else{
					pathProgress--;
					pathProgress = Math.max(0, pathProgress);	
				}return;
			}
		}
	}
	public static int closestSoldier(RobotController rc, int dis) throws GameActionException{
	MapLocation currLoc = rc.getLocation();
		for(int i = 0; i < enemyRobots.length; i++){
			RobotInfo enemyInfo = enemyRobots[i];
			if(manhattenDistance(enemyInfo.location, currLoc) <=dis){
				return i;
			}
		}
		return 0;
	}
	private static boolean tryToMoveAway(RobotController rc, MapLocation location) throws GameActionException {
		Direction dir = rc.getLocation().directionTo(location);
		int ordinal = (dir.ordinal()+4)%8;
		if(rc.isActive()){
			if(rc.canMove(directions[ordinal])){
				if(sneaking){
					rc.sneak(directions[ordinal]);
				}else{
					rc.move(directions[ordinal]);
				}
				return true;
			}
			ordinal = (ordinal+1)%8;
			if(rc.canMove(directions[ordinal])){
				if(sneaking){
					rc.sneak(directions[ordinal]);
				}else{
					rc.move(directions[ordinal]);
				}return true;
			}
			ordinal = (ordinal+6)%8;
			if(rc.canMove(directions[ordinal])){
				if(sneaking){
					rc.sneak(directions[ordinal]);
				}else{
					rc.move(directions[ordinal]);
				}return true;
			}
//			ordinal = (ordinal+5)%8;
//			if(rc.canMove(directions[ordinal])){
//				rc.move(directions[ordinal]);
//				return;
//			}
//			ordinal = (ordinal+7)%8;
//			if(rc.canMove(directions[ordinal])){
//				rc.move(directions[ordinal]);
//				return;
//			}
		}
		return false;
	}
	public static MapLocation lookForSuicidePosition(RobotController rc) throws GameActionException{

			MapLocation currLoc = rc.getLocation();
			float count = 0;
			float x = 0,y = 0;
			if(enemyRobots.length > 1){
				for(int i = 0; i < enemyRobots.length; i ++){
					RobotInfo anEnemyInfo = enemyRobots[i];
					MapLocation enemyLoc = anEnemyInfo.location;
					int mD = manhattenDistance(currLoc, enemyLoc);
					if(mD == 3 || mD == 2){
						 count ++;
						 x+= enemyLoc.x;
						 y+= enemyLoc.y;
					}
				}
			}
			if(count > 0){
				return new MapLocation(Math.round(x/count),Math.round(y/count));
			}
			return null;
	}
	public static int manhattenDistance(MapLocation loc1, MapLocation loc2){
		return (Math.abs(loc1.x-loc2.x)+Math.abs(loc1.y-loc2.y));
	}
	public static void shootToCollectCows(RobotController rc, MapLocation pastrLoc) throws GameActionException{
		if(rc.isActive()){
			MapLocation currLoc = rc.getLocation();
			int dist = currLoc.distanceSquaredTo(pastrLoc);
			if(dist <= 16 && dist >= 9){
				int x = -(pastrLoc.x-currLoc.x);
				int y = -(pastrLoc.y-currLoc.y);
				MapLocation target = currLoc.add(x*3, y*3);
				Direction dir = currLoc.directionTo(target);
				MapLocation shootingPos = currLoc.add(dir, 3);
				if(shootingPos.distanceSquaredTo(currLoc) > 9){
					shootingPos = shootingPos.subtract(dir);
				}
				rc.attackSquare(shootingPos);
			}
		}
	}
	public static void broadCastDistanceToFormation(RobotController rc, int distanceSquared) throws GameActionException{

		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		
			int counter = rc.readBroadcast(channel+10)+1;
			int sumDis = rc.readBroadcast(channel+11)+distanceSquared;
			rc.broadcast(channel+10, counter);
			rc.broadcast(channel+11, sumDis);
		
	}
	public static int getPathIndex(RobotController rc) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
//		rc.setIndicatorString(2, "targetIndex: " + rc.readBroadcast(channel+12));
		return rc.readBroadcast(channel+12);
	}
	public static void formationMove(RobotController rc) throws GameActionException{
		MapLocation curr = rc.getLocation();
//		if(checkForPathID){
//			getPathIDForLineFormation(rc);
//			checkForPathID = false;
//		}
//		rc.setIndicatorString(2, Clock.getRoundNum() + " enters formation move");
		getPathIDForLineFormation(rc);
		readLineFormation(rc);
		int pathIndex = getPathIndex(rc);
		if(pathProgress <= pathIndex-5){
//		if(!inFormation && curr.distanceSquaredTo(lineMiddlePoint) >=30){
			followPath(rc);
//			rc.setIndicatorString(2, "followsPath");
			return;
		}
		if(curr.distanceSquaredTo(lineMiddlePoint) <= 15){
			pathProgress = pathIndex;
		}
		MapLocation to;
		if(health <= StaticVariables.ROBOT_BACKUP_HEALTH_THRESHOLD){
			Direction dir = lineMiddlePoint.directionTo(formationTarget);
			linePoint1  = linePoint1.subtract(dir);
			linePoint2 = linePoint2.subtract(dir);
			lineMiddlePoint = lineMiddlePoint.subtract(dir);
			rc.setIndicatorString(1,"2st line: " +linePoint1+ " " + lineMiddlePoint + " " + linePoint2 + " direction: " +  lineMiddlePoint.directionTo(formationTarget));
			to = StaticFunctions.getPointOnLine(curr.x,curr.y, linePoint1.x, linePoint1.y, linePoint2.x, linePoint2.y);
		}else{
			to = StaticFunctions.getPointOnLine(curr.x,curr.y, linePoint1.x, linePoint1.y, linePoint2.x, linePoint2.y);
			rc.setIndicatorString(1,"1st line: " + linePoint1+ " " + lineMiddlePoint + " " + linePoint2 + " direction: " +  lineMiddlePoint.directionTo(formationTarget));
			
		}

		MapLocation tmp = to;
		if(curr.equals(to) || to.equals(linePoint1) || to.equals(linePoint2)){
			to = SoldierBehavior.lineMiddlePoint;
			if(rc.isActive()){
				if(curr.distanceSquaredTo(to) <= 15){
					Direction dir = rc.getLocation().directionTo(to);
					
					if(rc.canMove(dir) && getGroupPowersForFutureMove(rc, curr.add(dir)) > 1){
						if(sneaking){
							rc.sneak(dir);
						}else{
							rc.move(dir);
						}
						curr = curr.add(dir);
					}
				}else{
//					BugMove.move(lineMiddlePoint, rc, sneaking);
					Direction dir = BugMove.moveHelper(lineMiddlePoint, rc,sneaking);
					if(dir != null){
						MapLocation target = curr.add(dir);
						if(getGroupPowersForFutureMove(rc, target) > 1){
							if(sneaking){
								rc.sneak(dir);
							}else{
								rc.move(dir);
							}
							curr = curr.add(dir);
						}
					}
				}
			}
		}else{
//			BugMove.move(lineMiddlePoint, rc, sneaking);
			Direction dir = BugMove.moveHelper(lineMiddlePoint, rc,sneaking);
			if(dir != null){
				MapLocation target = curr.add(dir);
				if(getGroupPowersForFutureMove(rc, target) > 1){
					if(sneaking){
						rc.sneak(dir);
					}else{
						rc.move(dir);
					}
					curr = curr.add(dir);
				}
			}
		}
		int distance = curr.distanceSquaredTo(tmp);
//		rc.setIndicatorString(2, Clock.getRoundNum() + ", dis to formation: " + distance + " target: " + tmp);
		broadCastDistanceToFormation(rc, distance);
	}
	
	public static void broadPressureOnGroup(RobotController rc) throws GameActionException{
		if(attacked){
			int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
			rc.broadcast(channel+4, rc.readBroadcast(channel+4)+1);
			attacked = false;
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
			state = interpretCommand(command/100000);
		}
	}
	public static void notifyBuild(RobotController rc, boolean pastr) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		if(pastr){
			rc.broadcast(channel+13, 2);
		}else{
			rc.broadcast(channel+16, 2);
		}
	}
	public static void readLineFormation(RobotController rc) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		SoldierBehavior.linePoint1 = StaticFunctions.intToLoc(rc.readBroadcast(channel));
		SoldierBehavior.linePoint2 = StaticFunctions.intToLoc(rc.readBroadcast(channel+1));
		SoldierBehavior.lineMiddlePoint = StaticFunctions.intToLoc(rc.readBroadcast(channel+2));
		SoldierBehavior.formationTarget = StaticFunctions.intToLoc(rc.readBroadcast(channel+3));
//		rc.setIndicatorString(1, linePoint1 + " " + linePoint2 + " " + lineMiddlePoint);
	}
	public static void getGroupNumber(){
		groupNum = (currentCommand/10000)%10;
	}
	/**
	 * Writes the current maplocation and lifecount into a feedback channel which is analyzed by the HQ.
	 * @param rc
	 * @throws Exception
	 */
	public static int broadcastFeedback(RobotController rc) throws Exception{
		MapLocation mL = rc.senseRobotInfo(rc.getRobot()).location;
		int feedback =(mL.x*100)+mL.y;
		feedback += Math.max(0, health-1)*10000;
		feedback += count *1000000;
		rc.broadcast(StaticVariables.ROBOT_FEEDBACK_CHANNEL_START+(iD-1),feedback);
		return feedback;
	}
	public static void sneakRandomly(RobotController rc) throws Exception{
		Direction moveDirection = directions[(int) (rand.nextFloat()*8f)];
		if (rc.isActive() && rc.canMove(moveDirection)) {
			rc.sneak(moveDirection);
		}
	}
	public static void moveRandomly(RobotController rc) throws Exception{
		Direction moveDirection = directions[(int) (rand.nextFloat()*8f)];
		if (rc.isActive() && rc.canMove(moveDirection)) {
			rc.move(moveDirection);
		}
	}
	public static void moveToEnemyHQ(RobotController rc) throws Exception{
		Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		if (rc.isActive() && rc.canMove(toEnemy)) {
			rc.move(toEnemy);
		}
	}
	public static boolean moveToLoc(MapLocation loc, RobotController rc) throws Exception{
		Direction toDest = rc.getLocation().directionTo(loc);
		if (rc.isActive() && rc.canMove(toDest)) {
			rc.move(toDest);
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
		int min = 1000;
		for(int i = 0; i < enemyRobots.length; i ++){
			RobotInfo anEnemyInfo = enemyRobots[0];
			int curr = anEnemyInfo.location.distanceSquaredTo(rc.getLocation());
			if(curr < min && (anEnemyInfo.type == RobotType.SOLDIER || anEnemyInfo.type == RobotType.HQ)){
				min = curr;
			}
		}
		return min;
	}
	private static boolean tryToShoot(RobotController rc) throws Exception {

//		if(rc.isActive()){
//			if(enemyRobots.length > 0){
//				for(int i = 0; i < enemyRobots.length; i ++){
//					RobotInfo anEnemyInfo = enemyRobots[i];
//					if(anEnemyInfo.type != RobotType.HQ && anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<=rc.getType().attackRadiusMaxSquared){
//						rc.attackSquare(anEnemyInfo.location);
//						return true;
//						
//					}
//				}
//			}
//		}
		return attackWeakestEnemy(rc);
	}
	
	/**
	 * Returns the internal-state that results from a given command.
	 * Attacking = Assembling, because at the moment there is no difference.
	 * @param command
	 * @return
	 */
	public static SoldierState interpretCommand(int command){
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
		case StaticVariables.COMMAND_GROUP_MOVE_TO_LOCATION:
			return SoldierState.GROUP_MOVEMENT;
		case StaticVariables.COMMAND_FARMING_MODE:
			return SoldierState.FARMING_MODE;
		case StaticVariables.COMMAND_FORMATION_MOVE:
			checkForPathID = true;
			forward = true;
			inFormation = false;
			getGroupNumber();
			return SoldierState.FORMATION_MOVE;
		case StaticVariables.COMMAND_BUILD_NOISE_TOWER:
			return SoldierState.BUILD_NOISE_TOWER;
		case StaticVariables.COMMAND_FOLLOW_PATH:
			forward = true;
			pathID = currentCommand%10;
			pathProgress = 1;
			return SoldierState.FOLLOWING_PATH;
		}
		System.out.println(iD + " weird Command: " + command);
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
	public static boolean attackWeakestEnemy(RobotController rc) throws GameActionException{
		if(!rc.isActive()){
			return false;
		}
		double min = 1000;
		RobotInfo rI = null;
		for(int i = 0; i < enemiesInShootingRange.length; i ++){
			RobotInfo curr = enemiesInShootingRange[i];
			if(curr.health < min){
				min = curr.health;
				rI = curr;
			}
		}
		if(rI == null){
			return false;
		}
		rc.attackSquare(rI.location);
		return true;
	}
	public static void getEnemies(RobotController rc) throws GameActionException{
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, RobotType.SOLDIER.sensorRadiusSquared, rc.getTeam().opponent());
		enemyRobots = new RobotInfo[enemies.length];
		for(int i = 0; i < enemyRobots.length; i ++){
			enemyRobots[i] = rc.senseRobotInfo(enemies[i]);
		}
		enemies = rc.senseNearbyGameObjects(Robot.class, RobotType.SOLDIER.attackRadiusMaxSquared, rc.getTeam().opponent());
		enemiesInShootingRange = new RobotInfo[enemies.length];
		for(int i = 0; i < enemiesInShootingRange.length; i ++){
			enemiesInShootingRange[i] = rc.senseRobotInfo(enemies[i]);
		}
		lastUpdate = Clock.getRoundNum();
	}
	/**
	 * return 0 == enemies already in shooting range
	 * return 1 == move causes disadvantage
	 * return 2 == move causes advantage
	 * return 3 == you can just move
	 * @param rc
	 * @param loc
	 * @return
	 * @throws GameActionException 
	 */
	public static int getGroupPowersForFutureMove(RobotController rc, MapLocation loc) throws GameActionException{
		if(enemiesInShootingRange.length != 0){
//			rc.setIndicatorString(2,Clock.getRoundNum() +   "enemies already in shooting range, last update: " + lastUpdate);
			return 0;
		}
		if(enemyRobots.length == 0){
//			rc.setIndicatorString(2,Clock.getRoundNum() +   "no visible enemies, last update: " + lastUpdate);
			return 3;
		}
		healthOurs = 0;
		numUs = 0;
		healthOurs = 0;
		numThem = rc.senseNearbyGameObjects(Robot.class, loc, RobotType.SOLDIER.attackRadiusMaxSquared, rc.getTeam().opponent()).length;
		int tmp = 0;
		Robot[] oursTmp,ours = null;
		for(int i = 0; i < enemyRobots.length; i ++){
			oursTmp = rc.senseNearbyGameObjects(Robot.class, enemyRobots[i].location, RobotType.SOLDIER.attackRadiusMaxSquared, rc.getTeam());
			tmp = oursTmp.length;
			if(tmp > numUs){
				numUs = tmp;
				ours = oursTmp;
			}
		}
		boolean found = false;
		for(int i = 0; i < enemyRobots.length; i ++){
			if(enemyRobots[i].location.distanceSquaredTo(loc) <= RobotType.SOLDIER.attackRadiusMaxSquared){
				if(!found){
					numUs++;
					healthOurs+=health;
					found = true;
				}
				if(enemyRobots[i].type == RobotType.SOLDIER&& !enemyRobots[i].isConstructing){
					healthThem += enemyRobots[i].health;
				}
			}
		}
		if(ours != null){
			for(int i = 0; i < ours.length; i ++){
				RobotInfo rI = rc.senseRobotInfo(ours[i]);
				if(rI.type == RobotType.SOLDIER&&!rI.isConstructing){
					healthOurs += rI.health;
				}
			}
		}
		double ourPower = (double)healthOurs/(double)numThem; 
		double theirPower = (double)healthThem/(double)numUs;
//		rc.setIndicatorString(2,Clock.getRoundNum() +  " healthOurs: " + healthOurs + "healthUs: " + numUs + " healthThem: " + healthThem+ "numThem: " + numThem+ ", last update: " + lastUpdate + " targetLoc " + loc);
		if(ourPower < (theirPower*StaticVariables.MULTIPLICATOR_GROUP_POWER)){
			return 1;
		}else{
			return 2;
		}
	}
	public static void getGroupPowers(RobotController rc) throws GameActionException{
		numUs = 0;
		numThem = enemiesInShootingRange.length;
		int tmp = 0;
		Robot[] oursTmp,ours = null;
		for(int i = 0; i < enemiesInShootingRange.length; i ++){
			oursTmp = rc.senseNearbyGameObjects(Robot.class, enemiesInShootingRange[i].location, RobotType.SOLDIER.attackRadiusMaxSquared, rc.getTeam());
			tmp = oursTmp.length;
			if(tmp > numUs){
				numUs = tmp;
				ours = oursTmp;
			}
		}
		numUs ++;
		healthOurs = health;
		rc.setIndicatorString(1,Clock.getRoundNum() + " round| " + ours+" "+ enemiesInShootingRange.length);
		if(ours != null){
			for(int i = 0; i < ours.length; i ++){
				RobotInfo rI = rc.senseRobotInfo(ours[i]);
				if(rI.type == RobotType.SOLDIER&&!rI.isConstructing){
					healthOurs += rI.health;
				}
			}
		}
		healthThem = 0;
		for(int i = 0; i < enemiesInShootingRange.length; i ++){
			if(enemiesInShootingRange[i].type == RobotType.SOLDIER &&!enemiesInShootingRange[i].isConstructing){
				healthThem += enemiesInShootingRange[i].health;
			}
		}
	}
	public static void getMovingType(RobotController rc) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		sneaking = rc.readBroadcast(channel+19)==1;
	}
	public static int getBuildingCommand(RobotController rc) throws GameActionException{
		int channel = StaticVariables.ROBOT_GROUP_COMMAND_CHANNEL_START+(groupNum*StaticVariables.GROUP_CHANNEL_LENGTH_PER_GROUP);
		int build = rc.readBroadcast(channel+13);
		if(build == 1){
			if(thisPos.equals(new MapLocation(rc.readBroadcast(channel+14),rc.readBroadcast(channel+15)))){
				return 1;
			}
		}
		int build2 = rc.readBroadcast(channel+16);
		if(build2 == 1){
			if(thisPos.equals(new MapLocation(rc.readBroadcast(channel+17),rc.readBroadcast(channel+18)))){
				return 2;
			}
		}
		return 0;
	}
	public static boolean constructIfPossible(RobotController rc) throws GameActionException{
		int buildingCommand = getBuildingCommand(rc);
		getMovingType(rc);
		if(buildingCommand == 1){
			while(!rc.isActive()){
				rc.yield();
			}
			rc.construct(RobotType.PASTR);
			buildsPastr = true;
			return true;
		}else if(buildingCommand == 2){
			while(!rc.isActive()){
				rc.yield();
			}
			rc.construct(RobotType.NOISETOWER);
			return true;
		}
		return false;
	}
}
