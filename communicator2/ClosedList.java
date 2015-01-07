package communicator2;

import battlecode.common.MapLocation;

public class ClosedList {
	Node[][] closedListMap; 

	public ClosedList(int width, int height){
		closedListMap = new Node[width][height];
	}
	
	public void add(Node node){
		MapLocation loc = node.loc;
		closedListMap[loc.x][loc.y] = node;
	}
	
	public boolean isBetterNodeInList(Node node){
		MapLocation loc = node.loc;
		if(closedListMap[loc.x][loc.y] == null || closedListMap[loc.x][loc.y].f >= node.f){
			return false;
		}else{
			return true;
		}
	}
	
	
}
