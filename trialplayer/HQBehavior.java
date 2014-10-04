package fightPlayer;

import battlecode.common.*;

public class HQBehavior {
	
	private enum HQState{
		INIT,DEFAULT;
	}
	
	public static MapLocation[] assemblyPositions;
	public static HQState state = HQState.INIT;
	
	public static void hqBehavior(RobotController rc) throws Exception{
	
		if (!rc.isActive()) {
			return;
		}
		switch (state) {
		case INIT:
			MapMaker.makeMap(rc);
			MapMaker.broadcastMap(rc);
			assemblyPositions = MapMaker.buildImportanceMap(2,rc);
//			MapMaker.displayMap();
			sendCommand(assemblyPositions[0],11000,rc,StaticVariables.COMMAND_BUILD_PASTR);
			sendCommand(assemblyPositions[0],11001,rc,StaticVariables.COMMAND_BUILD_PASTR);
			sendCommand(assemblyPositions[0],11002,rc,StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
			sendCommand(assemblyPositions[0],11003,rc,StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
			state = HQState.DEFAULT;
			break;
		case DEFAULT:
			int count = rc.senseRobotCount();
			if (count < 20) {
				Direction toEnemy = rc.getLocation().directionTo(
						rc.senseEnemyHQLocation());
				if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
					rc.spawn(toEnemy);
				}
			}
			MapLocation[] enemyPastr = rc.sensePastrLocations(rc.getTeam().opponent());
			if(enemyPastr.length > 0){
				sendCommand(enemyPastr[0],11002,rc,StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
				sendCommand(enemyPastr[0],11003,rc,StaticVariables.COMMAND_ASSEMBLE_AT_LOCATION);
			}
			break;
		}
	}
	
	public static void sendCommand(MapLocation loc, int channelID, RobotController rc, int command) throws Exception{
		int broadcast = StaticFunctions.locToInt(loc) + (10000 * command);
		rc.broadcast(channelID, broadcast);
	}
}
