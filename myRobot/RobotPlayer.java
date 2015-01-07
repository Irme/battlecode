package myRobot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	public static int mapWidth, mapHeight;
	public static boolean search = true;
	public static MapLocation[] path;
	public static int index = 1;
	public static void run(RobotController rc) throws Exception{
		mapWidth = rc.getMapWidth();
		mapHeight = rc.getMapHeight();
		while(true){
			if(rc.getType() == RobotType.HQ){
//				if(search){
//					Astar.init(rc);
//					Astar.search(rc, rc.senseHQLocation(),rc.senseEnemyHQLocation());
//					search = false;
//				}	
				if(rc.senseRobotCount() != 1){
					Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
					try {
						rc.spawn(toEnemy);
					} catch (GameActionException e) {
					}
				}
			}else{
//				if(search){
//					SnailTrail.init(rc);
//					search = false;
//				}
//				SnailTrail.move(rc.senseEnemyHQLocation(),rc.getLocation(), rc);
//				
//				if(search){
//					MapLocation pos = rc.getLocation();
////					System.out.println(pos);
//					BugMove.init(rc, rc.senseEnemyHQLocation());
//					
//					search = false;
//				}
//				if(Clock.getRoundNum()<200){
//					BugMove.move(rc);
//				}
				if(search){
					Astar.init(rc);
					path = Astar.search(rc, rc.senseHQLocation(),rc.senseEnemyHQLocation());
					search = false;
//					System.out.println("length: " + path.length);
//					System.out.println(Arrays.toString(path));
				}	
				if(path != null){
					
					Direction dir= rc.getLocation().directionTo(path[index]);
					if(rc.isActive() && rc.canMove(dir)){
						rc.move(dir);
					}else{
//						System.out.println("cannot move " + dir + " " + rc.getLocation() + " " + path[index]);
					}
					if(rc.getLocation().equals(path[index])){
						index++;
//						System.out.println("index: " + index);
//						System.out.println(path[index]);
					}
				}
			}
			
			rc.yield();
		}
	}
}
