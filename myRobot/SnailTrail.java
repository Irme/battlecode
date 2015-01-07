package myRobot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SnailTrail {

	
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	final static int tailLength = 50;
	
	static int[][] tail;
	static LinkedList ll;
	static int mapWidth;
	static int mapHeight;
	public static void init(RobotController rc){
		tail = new int[rc.getMapHeight()+1][rc.getMapWidth()+1];
		ll = new LinkedList();
	}
	public static void move(MapLocation destination, MapLocation currentPos,RobotController rc) throws GameActionException{
		MapLocation move = helper(destination,currentPos,rc);
		if(move != null){
			ll.addItem(move.x, move.y);
		}
	}
	public static MapLocation helper(MapLocation destination, MapLocation currentPos,RobotController rc) throws GameActionException{
		if(!rc.isActive()){
			return null;
		}
		
		Direction target = currentPos.directionTo(destination);
		if(rc.canMove(target)){
			MapLocation loc = currentPos.add(target);
			if(tail[loc.y][loc.x]!=1){
				rc.move(target);
				tail[loc.y][loc.x] = 1;
				return loc;
			}
			tail[loc.y][loc.x] = 1;
		}
		int left = target.ordinal();
		int right = left;
		Direction tryToMove;
		left = (left+1)%8;
		right = (right+7)%8;
		while(left != right){
			tryToMove = directions[left];
			MapLocation loc = currentPos.add(tryToMove);
			
			if(tail[loc.y][loc.x]!=1 && (loc.x >= 0 && loc.y >= 0)){		
				tail[loc.y][loc.x] = 1;
				if(rc.canMove(tryToMove)){
					rc.move(tryToMove);
					return loc;
				}
			}
			tryToMove = directions[right];
			loc = currentPos.add(tryToMove);
			if(tail[loc.y][loc.x]!=1&& (loc.x >= 0 && loc.y >= 0)){		
				tail[loc.y][loc.x] = 1;
				if(rc.canMove(tryToMove)){
					rc.move(tryToMove);
					return loc;
				}
			}
			left = (left+1)%8;
			right = (right+7)%8;
		}
		tryToMove = directions[left];
		MapLocation loc = currentPos.add(tryToMove);
		if(tail[loc.y][loc.x]!=1&& (loc.x >= 0 && loc.y >= 0)){		
			tail[loc.y][loc.x] = 1;
			if(rc.canMove(tryToMove)){
				rc.move(tryToMove);
				return loc;
			}
		}
		ll.removeLast();
		return null;
	}
	private static class LinkedList{
		int count;
		Node start, end;
		public LinkedList(){
			count = 0;
			start = null;
			end = null;
		}
		public void addItem(int x, int y){
			Node newNode = new Node(x,y,null,null);
			if(start == null){
				start = newNode;
				end = newNode;
				count++;
				return;
			}
			start.prev = newNode;
			newNode.next = start;
			start = newNode;
			count ++;
			if(count == tailLength){
				tail[end.y][end.x] = 0; 
//				System.out.println("happens " + end.x + " " + end.y);
				end = end.prev;
				count --;
			}
//			System.out.println(start + " " + end);
		}
		public void removeLast(){
//			System.out.println("happens");
			tail[end.y][end.x] = 0; 
//			System.out.println("happens " + end.x + " " + end.y);
			end = end.prev;
			count --;
		}
		
		private static class Node{
			Node prev,next;
			int x,y;
			public Node(int x, int y, Node prev, Node next){
				this.prev = prev;
				this.next = next;
				this.x = x;
				this.y = y;
			}
			public String toString(){
				return "[" + y +", " + x + "]";
			}
		}
	}
}


