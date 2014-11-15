package aStarTest;

import battlecode.common.MapLocation;

public class SquaredDistance extends Heuristic {

	@Override
	public double getEstimatedDistance(MapLocation from, MapLocation to) {
		double verticalDistance = Math.abs(from.x - to.x);
		double horizontalDistance = Math.abs(from.y - to.y);

		// Pythagoras
		return verticalDistance * verticalDistance + horizontalDistance * horizontalDistance;
	}



}
