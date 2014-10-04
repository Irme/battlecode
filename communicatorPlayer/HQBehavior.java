package communicator;

import battlecode.common.*;

public class HQBehavior {
	
	private enum HQState{
		INIT,DEFAULT;
	}
	private static class RobotRepresentation{
		int currentCommand;
		int lifeTime;
		boolean initilazie;
		
		public RobotRepresentation(){
			this.initilazie = true;
			this.lifeTime = 0;
			this.currentCommand = StaticVariables.COMMAND_NOT_RECEIVED_YET;
		}
	}
	
	public static MapLocation[] assemblyPositions;
	public static HQState state = HQState.INIT;
	public static RobotRepresentation[] robots = new RobotRepresentation[StaticVariables.MAX_ROBOTS_SPAWN];
	static int lifeCount = 0;
	
	public static void hqBehavior(RobotController rc) throws Exception{
		lifeCount ++;
		switch (state) {
		case INIT:
			MapMaker.makeMap(rc);
			assemblyPositions = MapMaker.buildImportanceMap(2,rc);
			state = HQState.DEFAULT;
			break;
		case DEFAULT:
			tryToSpawn(rc);
			deliverID(rc);
			updateInteralRobotRepresentation(rc);
			MapLocation[] enemyPastr = rc.sensePastrLocations(rc.getTeam().opponent());
			if(enemyPastr.length > 0){
				for(int i = 0; i < robots.length; i ++){
					if(robots[i] != null){
						sendCommand(enemyPastr[0], StaticVariables.ROBOT_COMMAND_CHANNEL_START+i, rc, StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
					}
				}
			}else{
				for(int i = 0; i < robots.length; i ++){
					if(robots[i] != null){
						sendCommand(assemblyPositions[0], StaticVariables.ROBOT_COMMAND_CHANNEL_START+i, rc, StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
					}
				}
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
	}
	public static void updateInteralRobotRepresentation(RobotController rc) throws Exception{
		for(int i = 0; i < StaticVariables.MAX_ROBOTS_SPAWN; i ++){
			if(robots[i] != null){
				int feedBack = rc.readBroadcast(StaticVariables.ROBOT_FEEDBACK_CHANNEL_START+i);
				int current = robots[i].lifeTime;
				robots[i].lifeTime = feedBack;
				if(feedBack  == current && !robots[i].initilazie){
					System.out.println((i+1) + " dies at: " + Clock.getRoundNum());
					robots[i] = null;
					 rc.broadcast(StaticVariables.ROBOT_FEEDBACK_CHANNEL_START+i,0);
				}else if(feedBack != current){
					robots[i].initilazie = false;
				}
			}
		}
	}
	public static void deliverID(RobotController rc) throws Exception{
		int current = rc.readBroadcast(StaticVariables.ROBOT_ID_CHANNEL);
		if(current == 1){
			for(int i = 0; i < robots.length; i ++){
				if(robots[i] == null){
					rc.broadcast(StaticVariables.ROBOT_ID_CHANNEL, (i+1)*10);
					robots[i] = new RobotRepresentation();
					return;
				}
			}
		}
	}
	public static void tryToSpawn(RobotController rc) throws Exception{
		if(rc.isActive()){
			int count = rc.senseRobotCount();
			deliverID(rc);
			if (count < 20) {
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
