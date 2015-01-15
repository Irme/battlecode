package communicator2004;

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
			return new MapLocation((int)(x1 +param*C),(int)(y1+param*D));
		}

	}
	
	
}
