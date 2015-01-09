package communicator2;

import java.util.LinkedList;

import battlecode.common.*;;

public class A_Star {

	public static Heuristic hFunc = new SquaredDistance();
	MapLocation start;
	OpenList openList;
	ClosedList closedList;
	LinkedList<MapLocation> path;
	
	public LinkedList<MapLocation> searchPathTo(MapLocation goal, RobotController rc){
//		MapLocation start = rc.getLocation();
//		OpenList openList = new OpenList(rc.getMapWidth(), rc.getMapHeight());
//		ClosedList closedList = new ClosedList(rc.getMapWidth(), rc.getMapHeight());
		
		start = rc.getLocation();
		openList = new OpenList(rc.getMapWidth(), rc.getMapHeight());
		closedList = new ClosedList(rc.getMapWidth(), rc.getMapHeight());
		path = null;
		
		openList.insert(new Node(start, 0 ,0));
		
		while(!openList.isEmpty()){
			Node q = openList.getAndRemoveBest();
			
			treatSuccessors(q, rc, goal);
			if(path != null){
				return path;
			}

			closedList.add(q);
		}
		System.out.println("No path found");
		return null; // No path found
	}

	public void moveTo(MapLocation goal, RobotController rc){
		LinkedList<MapLocation> path = searchPathTo(goal, rc);
		
		while(!path.isEmpty()){
			while(rc.isActive()){
				MapLocation nextLoc = path.getLast();
				
				Direction dirToMove = rc.getLocation().directionTo(nextLoc);
				if(rc.canMove(dirToMove)){
					try {
						rc.move(dirToMove);
						path.removeLast();
					} catch (GameActionException e) {
						e.printStackTrace();
					}
				}else{
					/*
					 *  walking around the obstacle:
					 *  go through the next tiles in the path until a tile is found that isn't occupied
					 *  then go to this tile with snail trail and move on with walking the calculated path
					 */
					// TODO to something if soldier gets stuck for a while / the next tile won't get free
					if(path.size() == 1){ // next tile is the last one
						return;
					}else{
						boolean freeTileFound = false;
						int counter = 0;
						while(!freeTileFound && counter <= path.size()-2){
							int nextTileIndex = path.size()-2-counter; // size - 1 is the current tile
							counter++;
							MapLocation nextLocAfter = path.get(nextTileIndex);
							try {
								// TODO check if nextLocAfter is in sensorRange (how can I find out  the sensor range?)
								// if it isn't we have a problem...  just return for the present until a solution is found
								GameObject objectOnNextLocAfter = rc.senseObjectAtLocation(nextLocAfter);
								if(objectOnNextLocAfter == null){
									boolean nextLocReached = false;
									while(!nextLocReached){
										SnailTrail.tryToMove(nextLocAfter, rc);
										if(StaticFunctions.locEquals(rc.getLocation(), nextLocAfter)){
											nextLocReached = true;
										}
									}
									freeTileFound = true;
								}
							} catch (GameActionException e) {
								// TODO check sensor range before and find out what to do, if there is no free tile on the path within sensor range...
								e.printStackTrace();
								return;
							}
						}
					}
				}
			}
		}
	}
	
	private void treatSuccessors(Node n, RobotController rc, MapLocation goal){
		MapLocation north = n.loc.add(Direction.NORTH);
		treatSuccessor(rc, north, n, goal);
		
		MapLocation north_east = n.loc.add(Direction.NORTH_EAST);
		treatSuccessor(rc, north_east, n, goal);
		
		MapLocation east = n.loc.add(Direction.EAST);
		treatSuccessor(rc, east, n, goal);
		
		MapLocation south_east = n.loc.add(Direction.SOUTH_EAST);
		treatSuccessor(rc, south_east, n, goal);
		
		MapLocation south = n.loc.add(Direction.SOUTH);
		treatSuccessor(rc, south, n, goal);
		
		MapLocation south_west = n.loc.add(Direction.SOUTH_WEST);
		treatSuccessor(rc, south_west, n, goal);
		
		MapLocation west = n.loc.add(Direction.WEST);
		treatSuccessor(rc, west, n, goal);
		
		MapLocation north_west = n.loc.add(Direction.NORTH_WEST);
		treatSuccessor(rc, north_west, n, goal);
	}
	
	private void treatSuccessor(RobotController rc, MapLocation newLoc, Node currentNode, MapLocation goal){
		if(rc.senseTerrainTile(newLoc) == TerrainTile.NORMAL || rc.senseTerrainTile(newLoc) == TerrainTile.ROAD){
			double sucG = currentNode.g + 1; // TODO is walking diagonal more expensive?  Then this has to be fixed
			double sucH = hFunc.getEstimatedDistance(newLoc, goal);
			Node successor = new Node(currentNode, newLoc, sucG, sucH);
			
			if(successor.isLocation(goal)){
				 path = successor.createPath();
			}
			
			if(closedList.isBetterNodeInList(successor)){	
				// System.out.println("There already is a better node in closed");
			}else{
				openList.replaceIfBetter(successor);
			}
		}
	}
}
