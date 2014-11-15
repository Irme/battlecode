package aStarTest;

import battlecode.common.MapLocation;

public abstract class Heuristic {

	public abstract double getEstimatedDistance(MapLocation from, MapLocation to);
	
}
