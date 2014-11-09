package communicator;

import java.util.ArrayList;

import battlecode.common.*;

public class HQBehavior {
	
	private enum HQState{
		INIT,DEFAULT;
	}
	private static class RobotRepresentation{
		int currentCommand;
		int iD;
		int lifeTime;
		boolean initilazie;
		MapLocation mL;
		public RobotRepresentation(int iD){
			this.initilazie = true;
			this.iD = iD;
			this.lifeTime = 0;
			this.currentCommand = StaticVariables.COMMAND_NOT_RECEIVED_YET;
		}
	}
	
	public static MapLocation[] assemblyPositions;
	public static MapLocation lastTarget;
	
	public static HQState state = HQState.INIT;
	public static RobotRepresentation[] robots = new RobotRepresentation[StaticVariables.MAX_ROBOTS_SPAWN];
	public static ArrayList<RobotRepresentation> attacking = new ArrayList<RobotRepresentation>();
	public static ArrayList<RobotRepresentation> assembling= new ArrayList<RobotRepresentation>();
	public static ArrayList<RobotRepresentation> building= new ArrayList<RobotRepresentation>();
	public static ArrayList<RobotRepresentation> free= new ArrayList<RobotRepresentation>();
	
	static int lifeCount = 0;
	static int byteCodeSum = 0;
	public static void hqBehavior(RobotController rc) throws Exception{
		int prevByteCode = Clock.getBytecodeNum();
		lifeCount ++;
		switch (state) {
		case INIT:
			MapMaker.makeMap(rc);
			assemblyPositions = MapMaker.buildImportanceMap(2,rc);
			state = HQState.DEFAULT;
			break;
		case DEFAULT:
			System.out.println(lifeCount + " try to spawn: " + rc.senseRobotCount());
			tryToSpawn(rc);
			deliverID(rc);
			updateInteralRobotRepresentation(rc);
			MapLocation[] enemyPastr = rc.sensePastrLocations(rc.getTeam().opponent());
			if(enemyPastr.length > 0){
				deliverCommand(enemyPastr[0], rc, StaticVariables.COMMAND_ATTACK_LOCATION);
				
				if(lastTarget != null && enemyPastr[0].x != lastTarget.x && enemyPastr[0].y != lastTarget.y ){
					deliverCommand(enemyPastr[0], rc, StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
				}
				lastTarget = enemyPastr[0];
//				for(int i = 0; i < robots.length; i ++){
//					if(robots[i] != null){
//						sendCommand(enemyPastr[0], StaticVariables.ROBOT_COMMAND_CHANNEL_START+i, rc, StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
//					}
//				}
			}else{
				deliverCommand(assemblyPositions[0], rc, StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
//				for(int i = 0; i < robots.length; i ++){
//					if(robots[i] != null){
//						sendCommand(assemblyPositions[0], StaticVariables.ROBOT_COMMAND_CHANNEL_START+i, rc, StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
//					}
//				}
			}
			if(lifeCount >= 400 &&rc.sensePastrLocations(rc.getTeam()).length == 0){
				for(int i = robots.length-1; i >= 0; i --){
					if(robots[i] != null){
						sendCommand(assemblyPositions[0], StaticVariables.ROBOT_COMMAND_CHANNEL_START+i, rc, StaticVariables.COMMAND_BUILD_PASTR);
						return;
					}
				}
			}
			break;
		}
		byteCodeSum +=  Clock.getBytecodeNum()-prevByteCode;
		System.out.println(byteCodeSum/Clock.getRoundNum());
	}
	public static void updateInteralRobotRepresentation(RobotController rc) throws Exception{

		for(int i=StaticVariables.MAX_ROBOTS_SPAWN; --i >= 0;) {
			if(robots[i] != null){
				int feedBack = rc.readBroadcast(StaticVariables.ROBOT_FEEDBACK_CHANNEL_START+i);
				int current = robots[i].lifeTime;
				robots[i].lifeTime = feedBack;
				robots[i].mL = new MapLocation(feedBack/1000,(feedBack/10)%100);
				if(feedBack == current && !robots[i].initilazie){
//					System.out.println((i+1) + " dies at: " + Clock.getRoundNum());
					robots[i] = null;
					rc.broadcast(StaticVariables.ROBOT_FEEDBACK_CHANNEL_START+i,0);
				}else if(feedBack != current){
					robots[i].initilazie = false;
				}
			}
		}
	}
	public static void deliverCommand(MapLocation loc, RobotController rc, int command) throws Exception{
		switch(command){
		case StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION:
			deliverCommandToGroup(loc,HQBehavior.attacking,HQBehavior.assembling,command,rc);
			deliverCommandToGroup(loc,HQBehavior.free,HQBehavior.assembling,command,rc);
			break;
		case StaticVariables.COMMAND_ATTACK_LOCATION:
			deliverCommandToGroup(loc,HQBehavior.assembling,HQBehavior.attacking,command,rc);
			deliverCommandToGroup(loc,HQBehavior.free,HQBehavior.attacking,command,rc);
			break;
		case StaticVariables.COMMAND_BUILD_PASTR:
			break;
		}
	}

	public static void deliverCommandToGroup(MapLocation loc, ArrayList<RobotRepresentation> from, ArrayList<RobotRepresentation> to, int command, RobotController rc) throws Exception{
		while(from.size() != 0){
			RobotRepresentation rR = from.get(0);
			sendCommand(loc, StaticVariables.ROBOT_COMMAND_CHANNEL_START+rR.iD, rc, StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
			to.add(rR);
			from.remove(0);
		}
	}
	public static void deliverID(RobotController rc) throws Exception{
		int current = rc.readBroadcast(StaticVariables.ROBOT_ID_CHANNEL);
		if(current == 1){
			for(int i = 0; i < robots.length; i ++){
				if(robots[i] == null){
					rc.broadcast(StaticVariables.ROBOT_ID_CHANNEL, (i+1)*10);
					robots[i] = new RobotRepresentation(i);
					free.add(robots[i]);
					return;
				}
			}
		}
	}
	public static void tryToSpawn(RobotController rc) throws Exception{
		if(rc.isActive()){
			int count = rc.senseRobotCount();
//			deliverID(rc);
			if (count <= StaticVariables.MAX_ROBOTS_SPAWN) {
				Direction toEnemy = rc.getLocation().directionTo(
						rc.senseEnemyHQLocation());
				if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
					rc.spawn(toEnemy);
				}
			}
		}
	}
	public static void sendCommand(MapLocation loc, int channelID, RobotController rc, int command) throws Exception{
		int broadcast = StaticFunctions.locToInt(loc) + (10000 * command);
		rc.broadcast(channelID, broadcast);
	}
	
}
