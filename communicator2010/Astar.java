package communicator2010;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class Astar {

	public static Node[][] closedList;
	public static Node[][] openList; 
	public static LinkedList ll;
	public static MapLocation target;
	public static int mapHeight, mapWidth;
	public static void init(RobotController rc){
		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();
		closedList = new Node[mapHeight][mapWidth];
		openList = new Node[mapHeight][mapWidth];
	}
	
	public static MapLocation[] search(RobotController rc, MapLocation start, MapLocation target){
		Astar.target = target;
		ll = new LinkedList(new Node(start.x,start.y,start.distanceSquaredTo(target),0,null));
//		System.out.println("start search");
//		addSurroundingNodes(ll.start, rc, ll.start.g);
//		System.out.println("list: " + ll);
		Node current;
		while((current = ll.getBest()) != null){
			addSurroundingNodes(current, rc, current.g);
			closedList[current.y][current.x] = current; 
//			System.out.println(current);
			if(current.x == target.x && current.y == target.y){
//				System.out.println("found " + current);
				break;
			}
		}
		if(current == null){
			return null;
		}
		MapLocation[] path = new MapLocation[current.g+1];
		int index = path.length;
		while(current != null){
//			System.out.println(current);
			index --;
			path[index] = new MapLocation(current.x, current.y);
//			System.out.println(rc.senseTerrainTile(path[index]));
			current = current.prevTile;
		}
		return path;
	}
	
	public static void addSurroundingNodes(Node center,RobotController rc, int g){
		addNodeForTerrainTile(rc,new MapLocation(center.x-1,center.y-1),g,center);
		addNodeForTerrainTile(rc,new MapLocation(center.x,center.y-1),g,center);
		addNodeForTerrainTile(rc,new MapLocation(center.x+1,center.y-1),g,center);
		addNodeForTerrainTile(rc,new MapLocation(center.x-1,center.y),g,center);
		addNodeForTerrainTile(rc,new MapLocation(center.x+1,center.y),g,center);
		addNodeForTerrainTile(rc,new MapLocation(center.x-1,center.y+1),g,center);
		addNodeForTerrainTile(rc,new MapLocation(center.x,center.y+1),g,center);
		addNodeForTerrainTile(rc,new MapLocation(center.x+1,center.y+1),g,center);
	}
	public static void addNodeForTerrainTile(RobotController rc, MapLocation mL, int g, Node prevTile){
		TerrainTile tt = rc.senseTerrainTile(new MapLocation(mL.x,mL.y));
		if(tt == TerrainTile.NORMAL || tt == TerrainTile.ROAD){
			float cost = g + mL.distanceSquaredTo(target) + (tt == TerrainTile.ROAD?-5:0);
			Node old = closedList[mL.y][mL.x];
			if(old != null){
				if(old.f > cost){
					ll.addItem(mL.x, mL.y, cost, g+1,prevTile);
				}
			}else{
				ll.addItem(mL.x, mL.y, cost, g+1,prevTile);
			}
			
		}		
	}
	
	public static class LinkedList{
		
		Node start;
		public LinkedList(Node start){
			this.start = start;
		}
		public Node getBest(){
			Node erg = this.start;
			this.start = start != null?start.next:null;
			return erg;
		}
		public void addItem(int x, int y, float f, int g, Node prevTile){
			Node current = this.start;
			Node next;
			Node newItem = new Node(x,y,f,g,prevTile);
			if(openList[y][x] != null){
				if(openList[y][x].f > newItem.f){
					openList[y][x] = newItem;
				}else{
					return;
				}
			}else{
				openList[y][x] = newItem;
			}
//			System.out.println(newItem);
			if(start == null){
				start = newItem;
				return;
			}
			if(start.f > f){
				newItem.next = this.start;
				this.start = newItem;
				return;
			}
			
			while((next = current.next) != null){
				if(next.f > f){
					current.next = newItem;
					newItem.next = next;
					return;
				}
				current = next;
			}
			current.next = newItem;
			
		}	
		public String toString(){
			String s = "";
			Node current = this.start;
			while(current != null){
				s += current.f + " ";
				current = current.next;
			}
			return s;
		}
	}
	
	public static class Node{
		@Override
		public String toString() {
			return "Node [x=" + x + ", y=" + y + ", g=" + g + ", f=" + f
					+ "]";
		}
		public Node(int x, int y, float f, int g, Node prevTile) {
			super();
			this.x = x;
			this.y = y;
			this.f = f;
			this.g = g;
			this.next = null;
			this.prevTile = prevTile;
		}
		int x,y,g;
		float f;
		Node next;
		Node prevTile;
	}
}
