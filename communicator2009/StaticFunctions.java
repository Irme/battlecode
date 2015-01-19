package communicator2009;

import battlecode.common.*;

public class StaticFunctions {

	/**
	 * Wraps a maplocation into an integer value.
	 * 
	 * @param m is the maplocation
	 * @return
	 */
	public static int locToInt(MapLocation m){
		return (m.x*100 + m.y);
	}
	
	/**
	 * Extracts a maplocation from an integer value.
	 * The first two digits represent the y-value.
	 * The third and fourth digit represent the x-value.
	 * 
	 * @param i is the integer
	 * @return
	 */
	public static MapLocation intToLoc(int i){
		return new MapLocation((i/100)%100,i%100);
	}
	
	

	
	public static MapLocation getPointOnLine(int x, int y, int x1, int y1, int x2,
			int y2) {

		float A = x - x1;
		float B = y - y1;
		float C = x2 - x1;
		float D = y2 - y1;

		float dot = A * C + B * D;
		float len_sq = C * C + D * D;
		float param = dot / len_sq;

		if (param < 0 || (x1 == x2 && y1 == y2)) {
			return new MapLocation(x1,y1);
		} else if (param > 1) {
			return new MapLocation(x2,y2);
		} else {
			return new MapLocation((int)Math.round((x1 +param*C)),(int)Math.round((y1+param*D)));
		}

	}
	
//	public static MapLocation[] getLineEndPoints(RobotController rc, MapLocation middlePoint, MapLocation destination){
//		MapLocation[] result = new MapLocation[3];
//		int x = (destination.y-middlePoint.y)*2;
//		int y = -(destination.x-middlePoint.x)*2;
//		MapLocation rightPoint = middlePoint.add(x, y);
//		MapLocation leftPoint = middlePoint.add(-x, -y);
//		result[0] = new MapLocation(middlePoint.x,middlePoint.y);
//		result[1] = new MapLocation(middlePoint.x,middlePoint.y);
//		
//		for(int i = 0; i < 5; i ++){
//			result[0] = result[0].add(result[0].directionTo(leftPoint));
//			TerrainTile tile = rc.senseTerrainTile(result[0]);
//			if(tile == TerrainTile.VOID || tile == TerrainTile.OFF_MAP){
//				result[0] = result[0].subtract(result[0].directionTo(leftPoint));
//				break;
//			}
//		}
//		for(int i = 0; i < 5; i ++){
//			result[1] = result[1].add(result[1].directionTo(rightPoint));
//			TerrainTile tile = rc.senseTerrainTile(result[1]);
//			if(tile == TerrainTile.VOID || tile == TerrainTile.OFF_MAP){
//				result[1] = result[1].subtract(result[1].directionTo(rightPoint));
//				break;
//			}
//		}
//		result[2] = middlePoint;
//	return result;
//	}	
	public static MapLocation[] getLineEndPoints(RobotController rc, MapLocation middlePoint, MapLocation destination){
		MapLocation[] result = new MapLocation[3];
		int x = (destination.y-middlePoint.y)*7;
		int y = -(destination.x-middlePoint.x)*7;
		MapLocation rightPoint = middlePoint.add(x, y);
		MapLocation leftPoint = middlePoint.add(-x, -y);
		result[0] = new MapLocation(middlePoint.x,middlePoint.y);
		result[1] = new MapLocation(middlePoint.x,middlePoint.y);
		
		for(int i = 0; i < 5; i ++){
			Direction dir = result[0].directionTo(leftPoint);
			result[0] = result[0].add(dir);
			TerrainTile tile = rc.senseTerrainTile(result[0]);
			if(tile == TerrainTile.VOID || tile == TerrainTile.OFF_MAP){
				result[0] = result[0].subtract(dir);
				break;
			}
		}
		for(int i = 0; i < 5; i ++){
			Direction dir = result[1].directionTo(rightPoint);
			result[1] = result[1].add(dir);
			TerrainTile tile = rc.senseTerrainTile(result[1]);
			if(tile == TerrainTile.VOID || tile == TerrainTile.OFF_MAP){
				result[1] = result[1].subtract(dir);
				break;
			}
		}
		float dx = result[1].x-result[0].x;
		float dy = result[1].y-result[0].y;
		result[2] = result[0].add(Math.round(dx/2f), Math.round(dy/2f));
		return result;
	}	
}
