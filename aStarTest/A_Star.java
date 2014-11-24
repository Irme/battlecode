package aStarTest;

import java.util.ArrayList;
import java.util.LinkedList;

import battlecode.common.*;;

public class A_Star {

	public static Heuristic hFunc = new SquaredDistance();
	
	public static LinkedList<MapLocation> searchPathTo(MapLocation goal, RobotController rc){
		MapLocation start = rc.getLocation();
		OpenList openList = new OpenList(rc.getMapWidth(), rc.getMapHeight());
		ClosedList closedList = new ClosedList(rc.getMapWidth(), rc.getMapHeight());
		openList.insert(new Node(start, 0 ,0));
		
		while(!openList.isEmpty()){
			Node q = openList.getAndRemoveBest();
			// System.out.println("Next best: " + q.loc.x + "," + q.loc.y);
			ArrayList<Node> successors = getSuccessors(q, rc, goal);
			for(int i = 0; i < successors.size(); ++i){
				Node successor = successors.get(i);
				// System.out.println("Search from node: " + (successor.loc.x) + ","  + (successor.loc.y));
				if(successor.isLocation(goal)){
					return successor.createPath();
				}
				
				if(closedList.isBetterNodeInList(successor)){	
					// System.out.println("There already is a better node in closed");
					continue;
				}
				openList.replaceIfBetter(successor);
			}
			closedList.add(q);
		}
		// System.out.println("No path found");
		return null; // No path found
	}

	private static ArrayList<Node> getSuccessors(Node n, RobotController rc, MapLocation goal){
		ArrayList<Node> successors = new ArrayList<Node>();
		
		MapLocation north = n.loc.add(Direction.NORTH);
		Node north_node = getSuccessor(rc, north, n, goal);
		if(north_node != null){
			successors.add(north_node);
		}
		
		MapLocation north_east = n.loc.add(Direction.NORTH_EAST);
		Node north_east_node = getSuccessor(rc, north_east, n, goal);
		if(north_east_node != null){
			successors.add(north_east_node);
		}
		
		MapLocation east = n.loc.add(Direction.EAST);
		Node east_node = getSuccessor(rc, east, n, goal);
		if(east_node != null){
			successors.add(east_node);
		}
		
		MapLocation south_east = n.loc.add(Direction.SOUTH_EAST);
		Node south_east_node = getSuccessor(rc, south_east, n, goal);
		if(south_east_node != null){
			successors.add(south_east_node);
		}
		
		MapLocation south = n.loc.add(Direction.SOUTH);
		Node south_node = getSuccessor(rc, south, n, goal);
		if(south_node != null){
			successors.add(south_node);
		}
		
		MapLocation south_west = n.loc.add(Direction.SOUTH_WEST);
		Node south_west_node = getSuccessor(rc, south_west, n, goal);
		if(south_west_node != null){
			successors.add(south_west_node);
		}
		
		MapLocation west = n.loc.add(Direction.WEST);
		Node west_node = getSuccessor(rc, west, n, goal);
		if(west_node != null){
			successors.add(west_node);
		}
		
		MapLocation north_west = n.loc.add(Direction.NORTH_WEST);
		Node north_west_node = getSuccessor(rc, north_west, n, goal);
		if(north_west_node != null){
			successors.add(north_west_node);
		}
		
		return successors;
	}
	
	private static Node getSuccessor(RobotController rc, MapLocation newLoc, Node currentNode, MapLocation goal){
		if(rc.senseTerrainTile(newLoc) == TerrainTile.NORMAL || rc.senseTerrainTile(newLoc) == TerrainTile.ROAD){
			double sucG = currentNode.g + 1; // TODO is walking diagonal more expensive?  Then this has to be fixed
			double sucH = hFunc.getEstimatedDistance(newLoc, goal);
			Node newNode = new Node(currentNode, newLoc, sucG, sucH);
			return newNode;
		} else{
			return null;
		}
	}
	
}
