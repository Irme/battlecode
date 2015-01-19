package communicator2010;

import java.util.Arrays;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SnailTrail {

	
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	static int tailLength =5;
	
	static int[][] tail;
	static LinkedList ll;
	static int mapWidth;
	static int mapHeight;
	public static void init(RobotController rc){
		tail = new int[rc.getMapHeight()+1][rc.getMapWidth()+1];
		ll = new LinkedList();
	}
	
	public static void move(MapLocation destination, MapLocation currentPos,RobotController rc) throws GameActionException{
		if(currentPos.equals(destination)){
			return;
		}
		if(tail == null){
			init(rc);
		}
		MapLocation move = helper(destination,currentPos,rc);
		if(move != null){
			tail[move.y][move.x] = 1;
			ll.addItem(move.x, move.y);
		}else{
			tail[currentPos.y][currentPos.x] = 1;
			ll.addItem(currentPos.x, currentPos.y);
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
//				tail[loc.y][loc.x] = 1;
//				ll.addItem(loc.x, loc.y);
				return loc;
			}
//			tail[loc.y][loc.x] = 1;
//			ll.addItem(loc.x, loc.y);
		}
		int left = target.ordinal();
		int right = left;
		Direction tryToMove;
		left = (left+1)%8;
		right = (right+7)%8;
		while(left != right){
			tryToMove = directions[left];
			MapLocation loc = currentPos.add(tryToMove);
			
			if((loc.x >= 0 && loc.y >= 0)&&tail[loc.y][loc.x]!=1 ){		
//				tail[loc.y][loc.x] = 1;
//				ll.addItem(loc.x, loc.y);
				if(rc.canMove(tryToMove)){
					rc.move(tryToMove);
					return loc;
				}
			}
			tryToMove = directions[right];
			loc = currentPos.add(tryToMove);
			if((loc.x >= 0 && loc.y >= 0)&&tail[loc.y][loc.x]!=1 ){		
//				tail[loc.y][loc.x] = 1;
//				ll.addItem(loc.x, loc.y);
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
		if((loc.x >= 0 && loc.y >= 0)&&tail[loc.y][loc.x]!=1 ){		
//			tail[loc.y][loc.x] = 1;
//			ll.addItem(loc.x, loc.y);
			if(rc.canMove(tryToMove)){
				rc.move(tryToMove);
				return loc;
			}
		}
//		ll.removeLast();
		return null;
	}
	public static class LinkedList{
		private int count;
		private Node start, end;
		public LinkedList(){
			count = 0;
			start = null;
			end = null;
		}
		public String toString(){
			Node curr = start;
			if(curr == null){
				return "";
			}
			String s = curr.toString();
			
			while((curr = curr.next) != null){
				s+= curr;
			}
			
			return s;
		}
		public int length(){
			Node curr = start;
			int n = curr == null?0:1;
			if(curr == null){
				return n;
			}
			while((curr = curr.next) != null){
				n++;
			}
			
			return n;
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
			if(count >= tailLength){
				
					tail[end.y][end.x] = 0; 
	//				System.out.println("happens " + end.x + " " + end.y);
					end = end.prev;
					end.next = null;
					count --;
				
			}
//			System.out.println(start + " " + end);
		}
		
		public void removeLast(){
//			System.out.println("happens");

//			System.out.println("happens " + end.x + " " + end.y);
			if(end != null){
				tail[end.y][end.x] = 0; 
				end = end.prev;
				if(end != null){
					end.next = null;
				}
				count --;
			}
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
	
	public static void printTail(){
		System.out.println("___");
		for(int i = 0; i < tail.length; i ++){
			System.out.println(Arrays.toString(tail[i]));
		}
		System.out.println("____");
	}
	public static void sneak(MapLocation destination, MapLocation currentPos,RobotController rc) throws GameActionException{
		if(currentPos.equals(destination)){
			return;
		}
		if(tail == null){
			init(rc);
		}
		MapLocation move = helperSneak(destination,currentPos,rc);
		if(move != null){
			tail[move.y][move.x] = 1;
			ll.addItem(move.x, move.y);
		}else{
			tail[currentPos.y][currentPos.x] = 1;
			ll.addItem(currentPos.x, currentPos.y);
		}
	}
	public static MapLocation helperSneak(MapLocation destination, MapLocation currentPos,RobotController rc) throws GameActionException{
		if(!rc.isActive()){
			return null;
		}
		
		Direction target = currentPos.directionTo(destination);
		if(rc.canMove(target)){
			MapLocation loc = currentPos.add(target);
			if(tail[loc.y][loc.x]!=1){
				rc.sneak(target);
//				tail[loc.y][loc.x] = 1;
//				ll.addItem(loc.x, loc.y);
				return loc;
			}
//			tail[loc.y][loc.x] = 1;
//			ll.addItem(loc.x, loc.y);
		}
		int left = target.ordinal();
		int right = left;
		Direction tryToMove;
		left = (left+1)%8;
		right = (right+7)%8;
		while(left != right){
			tryToMove = directions[left];
			MapLocation loc = currentPos.add(tryToMove);
			
			if((loc.x >= 0 && loc.y >= 0)&&tail[loc.y][loc.x]!=1 ){		
//				tail[loc.y][loc.x] = 1;
//				ll.addItem(loc.x, loc.y);
				if(rc.canMove(tryToMove)){
					rc.sneak(tryToMove);
					return loc;
				}
			}
			tryToMove = directions[right];
			loc = currentPos.add(tryToMove);
			if((loc.x >= 0 && loc.y >= 0)&&tail[loc.y][loc.x]!=1 ){		
//				tail[loc.y][loc.x] = 1;
//				ll.addItem(loc.x, loc.y);
				if(rc.canMove(tryToMove)){
					rc.sneak(tryToMove);
					return loc;
				}
			}
			left = (left+1)%8;
			right = (right+7)%8;
		}
		tryToMove = directions[left];
		MapLocation loc = currentPos.add(tryToMove);
		if((loc.x >= 0 && loc.y >= 0)&&tail[loc.y][loc.x]!=1 ){		
//			tail[loc.y][loc.x] = 1;
//			ll.addItem(loc.x, loc.y);
			if(rc.canMove(tryToMove)){
				rc.sneak(tryToMove);
				return loc;
			}
		}
//		ll.removeLast();
		return null;
	}
}


