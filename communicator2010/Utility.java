package communicator2010;

import battlecode.common.MapLocation;

public class Utility {
	public static MapLocation findCorrespondingMapLocation(double x, double y) {
		return new MapLocation ((int) Math.round(x), (int) Math.round(y));
	}
	
	public static MapLocation subtract(MapLocation A, MapLocation B) {
		return new MapLocation(A.x - B.x, A.y - A.y);
	}
	
	public static MapLocation add(MapLocation A, MapLocation B) {
		return new MapLocation(A.x + B.x, A.y + B.y);
	}
	
	public static MapLocation scalarMult(MapLocation A, double c) {
		return new MapLocation((int) Math.round(((double)A.x)*c), (int) Math.round(((double)A.y)*c));
	}
	public static double[] sinValues = new double[]{0, -0,4872, 0.8509, -0.9990, 0.8940, -0.5624, 0.0884, 0.4081, -0.8012, 0.9912, -0.9301, 0.6333, -0.1760, -0.3258, 0.7451, -0.9756};
	public static double[] cosValues = new double[]{1, -0.8733, 0.5253, -0.0442, -0.4481, 0.8268, -0.9961, 0.9129, -0.5985, 0.1323, 0.3673, -0.7739, 0.9844, -0.9454, 0.6669, -0.2194}; 

}
