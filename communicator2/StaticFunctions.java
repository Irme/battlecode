package communicator2;

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
	
	public static String locToString(MapLocation loc){
		return "(" + loc.x + "," + loc.y + ")";
	}
	
	public  static boolean locEquals(MapLocation loc1, MapLocation loc2){
		if(loc1.x == loc2.x && loc1.y == loc2.y){
			return true;
		}else{
			return false;
		}
	}
}
