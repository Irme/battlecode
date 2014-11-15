package aStarTest;

import java.util.LinkedList;
import battlecode.common.MapLocation;

public class Node {

	public MapLocation loc;
	public double g, h, f;
	public Node parent;
	
	public Node(MapLocation loc, double g, double h){
		this.loc = loc;
		this.g = g;
		this.h = h;
		this.f = h + g;
	}

	public Node(Node parent, MapLocation loc, double g, double h){
		this.parent = parent;
		this.loc = loc;
		this.g = g;
		this.h = h;
		this.f = h + g;
	}
	
	public boolean isLocation(MapLocation ml){
		if(ml.x == this.loc.x && ml.y == this.loc.y){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean equalsLoc(Node node){
		if(node.loc.x == this.loc.x && node.loc.y == this.loc.y){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isBetterThanEquals(Node node){
		if(this.f >= node.f){
			return true;
		}else{
			return false;
		}
	}
	
	public LinkedList<MapLocation> createPath(){
		LinkedList<MapLocation> list = new LinkedList<MapLocation>();
		Node current = this;
		while(current.parent != null){
			list.add(current.loc);
			current = current.parent;
		}
		return list;
	}
	
}
