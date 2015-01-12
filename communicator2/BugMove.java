package communicator2;

import java.util.Arrays;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class BugMove {

	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	
	
	static boolean onLine;
	static boolean rightAline;
	static MapLocation start, destination, current, mapMiddle, surrounding;
	static int[][] line;
	static float lineM, lineT;
	static BugState state;
	static int dX,dY;
	static LineType lineType;
	static int maxX,maxY,minX,minY, distance;
	private enum LineType{
		VERTICAL,NOT_VERTICAL;
	}
	
	private enum BugState{
		ONLINE,INIT,OFFLINE;
	}
	
	public static void init(RobotController rc, MapLocation destination){
		BugMove.onLine = true;
		BugMove.rightAline = true;
		BugMove.start = rc.getLocation();
		BugMove.destination = destination;
		maxX = Math.max(start.x, destination.x);
		minX = Math.min(start.x, destination.x);
		maxY = Math.max(start.y, destination.y);
		minY = Math.min(start.y, destination.y);
		distance = 0;
		line = new int[rc.getMapHeight()][rc.getMapWidth()];
		state = BugState.ONLINE;
		mapMiddle = new MapLocation(rc.getMapWidth()/2,rc.getMapHeight()/2);
		dX = destination.x-start.x;
		dY = destination.y-start.y;
		
		estimateLine();
		
	}
	public static void estimateLine(){
		if(destination.x==start.x){
			lineType = LineType.VERTICAL;
			return;
		}
		lineM = (float)(destination.y-start.y)/(float)(destination.x-start.x);
		lineT = (lineM*-(float)start.x) + (float)start.y;
		System.out.println("line: " + lineM + " " + lineT);
		lineType = LineType.NOT_VERTICAL;
	}
	public static boolean isOnLine(){
		if(lineType == LineType.VERTICAL){
			return current.x == start.x;
		}
		int yL = (int)((lineM*(current.x-1))+lineT);
		int yU = (int)((lineM*(current.x+1))+lineT);
		System.out.println("test: " + current + " " + yL + " " + yU);
//		return current.y == (int)((lineM*current.x)+lineT);
		return current.y >= yL && current.y <= yU;
	}
//	public static boolean isOnLine(MapLocation loc){
//		if(lineType == LineType.VERTICAL){
//			return loc.x == loc.x;
//		}
//		System.out.println(loc + " " +((lineM*loc.x)+lineT));
//		return loc.y == (int)((lineM*loc.x)+lineT);
//
//	}
	public static Direction getDirectionOnLine(Direction cur){
		if(destination.x < current.x){
			MapLocation target = new MapLocation(current.x-1,(int)((lineM*current.x-1)+lineT));
			return current.directionTo(target);
		}else if(destination.x > current.x){
			MapLocation target = new MapLocation(current.x+1,(int)((lineM*current.x+1)+lineT));
			return current.directionTo(target);
		}
		return cur;
	}
	public static boolean isInBox(){
		return current.x >= minX && current.x <= maxX && current.y >= minY && current.y <= maxY;
	}
	public static void move(RobotController rc) throws Exception{
		if(!rc.isActive()){
			return;
		}
	
		current = rc.getLocation();
		System.out.println("prevLoc "+ current);
		if(current.equals(destination)){
			return;
		}
		if(state == BugState.ONLINE){
			Direction dir = current.directionTo(destination);
			Direction old = dir;
			dir = getDirectionOnLine(dir);
			if(rc.canMove(dir)){
				rc.move(dir);		
				current = rc.getLocation();
				System.out.println("moves online but is? " + isOnLine());
				return;
			}else if(rc.canMove(old)){
				rc.move(old);		
				current = rc.getLocation();
				return;
			}else{
				state = BugState.OFFLINE;
				distance = current.distanceSquaredTo(start);
				System.out.println("switching to offline " + dir);
				surrounding = current.add(dir);
				calculateAlign();
			}
		}
		if(state == BugState.OFFLINE){
			Direction dir = current.directionTo(surrounding);
			if(rightAline){
				int ordinal = dir.ordinal();
				ordinal = (ordinal+1)%8;
				while(!rc.canMove(directions[ordinal])){
					ordinal = (ordinal+1)%8;
				}
				rc.move(directions[ordinal]);
				surrounding = current.add(directions[(ordinal+7)%8]);
				current = current.add(directions[ordinal]);
				System.out.println("surr " + current);
			}else{
				int ordinal = dir.ordinal();
				ordinal = (ordinal+7)%8;
				while(!rc.canMove(directions[ordinal])){
					ordinal = (ordinal+7)%8;
				}
				rc.move(directions[ordinal]);

				surrounding = current.add(directions[(ordinal+1)%8]);
				current = current.add(directions[ordinal]);
				System.out.println("surr " + current);
			}
			if(isOnLine() && isInBox() && current.distanceSquaredTo(start) > distance){
				System.out.println("switch to online");
				System.out.println( (int)((lineM*current.x)+lineT) + " " + current);
				state = BugState.ONLINE;
			}
		}
	}
	public static void calculateAlign(){
		int toMiddleX = mapMiddle.x-current.x;
		int toMiddleY = mapMiddle.y-current.y;
		
		int dot = (toMiddleX*dY)+(toMiddleY*-dX);
		rightAline = dot<0;
		System.out.println(rightAline);
	}
	
	public static void fillLine(){
		if(destination.x-start.x == 0){
			if(destination.y > start.y){
				drawStraightLine(0, destination.x-start.x, destination.y-start.y);
				return;
			}else{
				drawStraightLine(2, destination.x-start.x, destination.y-start.y);
				return;
			}
		}
		
		lineM = (destination.y-start.y)/(destination.x-start.x);
		lineT = (lineM*-start.x) + start.y;
		drawLine();
	}
	
	public static void drawLine(){
		if(start.x < destination.x){
			int start = BugMove.start.x;
			int end = destination.x;
			float y = ((start*lineM)+lineT-lineM);
			for(int x = start; x <= end; x++){
				y += lineM;
				line[(int)y][x] = 1;
			}
		}else{
			int end = start.x;
			int start = destination.x;
			float y = ((start*lineM)+lineT-lineM);
			for(int x = start; x <= end; x++){
				y += lineM;
				line[(int)y][x] = 1;
			}
		}
	}
	
	public static void drawStraightLine(int orientation, int x, int y){
		int startY = start.y;
		int startX = start.x;
		switch(orientation){
		case 0:
			for(int deltaY = startY; deltaY <= startY+y; deltaY ++){
				line[deltaY][startX] = 1;
			}
			break;
		case 1:
			for(int deltaX = startX; deltaX <= startX+x; deltaX ++){
				line[startY][deltaX] = 1;
			}
			break;
		case 2:
			for(int deltaY = startY; deltaY >= startY+y; deltaY --){
				line[deltaY][startX] = 1;
			}
			break;
		case 3:
			for(int deltaX = startX; deltaX >= startX+x; deltaX --){
				line[startY][deltaX] = 1;
			}
			break;
		}
	}
}
