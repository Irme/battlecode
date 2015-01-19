package communicator2009;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import battlecode.common.*;

/**
 * This class is responsible for finding good locations to assemble and/or build
 * a pasture. At the moment the map-building functions are not used because no
 * map information is exchanged between the HQ and the soldiers. The finding of
 * good locations is currently used but it will be changed in the near future
 * because it is not robust.
 * 
 * 
 * @author Alexander Bartl
 * 
 */
public class MapMaker {

	private static enum SymType {
		VERTICAL, HORIZONTAL, DIAGONAL;
	}

	private static enum RelativeHQLocation {
		UP, DOWN, LEFT, RIGHT;
	}

	public static int width, height;
	public static double[][] growths;
	public static double[][] assessments;
	public static MapLocation bestFound = null;
	public static double bestGrowth = -Double.MAX_VALUE;
	public static int searchProgress = 0;

	public static int mapHeight;
	public static int mapWidth;
	public static int maxDistSquared;
	public static MapLocation ourHQ;
	public static MapLocation enemyHQ;

	public static RobotController rc;
	public static int[] xOffSet;
	public static int[] yOffSet;
	public static int boxMaxX, boxMaxY, boxMinX, boxMinY;

	private static SymType sym;
	private static RelativeHQLocation relLoc;
	private static Line line;
	private static int xIndex;
	private static int yIndex;
	private static int bytecodeStart;
	private static int usableBytecode;
	public static boolean finishedSearch;
	private static MapLocation midPoint;

	public static void init(RobotController rcin) {
		rc = rcin;

		mapHeight = rc.getMapHeight();
		mapWidth = rc.getMapWidth();

		ourHQ = rc.senseHQLocation();
		enemyHQ = rc.senseEnemyHQLocation();

		xOffSet = new int[] {3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0,-1, -1,
				-1, -1, -1, -1, -2, -2, -2, -2, -2, -2, -3, -3, -3, -3, -3, -3 };
		yOffSet = new int[] {3, 2, 1, 0, -1, -2, -3, 3, 2, 1, 0, -1, -2, -3, 3, 2, 1, 0, -1,
				-2,-3, 3, 2, 1, 0, -1, -2, -3, 3, 2, 1, 0, -1, -2, -3, 3, 2, 1, 0, -1, -2, -3};

		growths = rc.senseCowGrowth();
		assessments = new double[mapWidth][mapHeight];

		maxDistSquared = (new MapLocation(0, 0))
				.distanceSquaredTo(new MapLocation(mapWidth, mapHeight));

		if (ourHQ.x == enemyHQ.x) {
			sym = SymType.HORIZONTAL;
			if (ourHQ.y < enemyHQ.y)
				relLoc = RelativeHQLocation.UP;
			else
				relLoc = RelativeHQLocation.DOWN;
		} else if (ourHQ.y == enemyHQ.y) {
			sym = SymType.VERTICAL;
			if (ourHQ.x < enemyHQ.x)
				relLoc = RelativeHQLocation.LEFT;
			else
				relLoc = RelativeHQLocation.RIGHT;
		} else {
			sym = SymType.DIAGONAL;
		}

		if (sym == SymType.DIAGONAL) {
			midPoint = Utility.scalarMult(Utility.add(ourHQ, enemyHQ), 0.5);
			double grad = ((double) (enemyHQ.y - ourHQ.y) / ((double) (enemyHQ.x - ourHQ.x)));
			line = new Line(midPoint, (-1 / grad));
			relLoc = (line.getPointType(ourHQ) == Line.Point.LEFT) ? RelativeHQLocation.LEFT
					: RelativeHQLocation.RIGHT;
		}

		xIndex = 0;
		yIndex = 0;
		finishedSearch = false;
	}

	public static void assessMapLocations() {
		boolean firstRound = true; 
		for (int y = 0; y < mapHeight; y+=3) {
			if (sym == SymType.HORIZONTAL) {
				if (relLoc == RelativeHQLocation.UP) {
					if (y > mapHeight/2) 
						break; 
				} else {
					if (y < mapHeight/2) {
						y = mapHeight/2; 
						continue; 
					}
				}
			}
			for (int x = 0; x < mapWidth; x+=3) {
 				if (firstRound) {
 					//System.out.println("Started at ["+ xIndex + ", " + yIndex + "].");
 					x = xIndex; 
					y = yIndex; 
					firstRound = false; 
				}
				if (!inBounds(x, y))
					continue; 
 				if (sym == SymType.VERTICAL) {
					if (relLoc == RelativeHQLocation.LEFT) {
						if (x > mapWidth/2) 
							break; 
					} else {
						if (x < mapWidth/2) {
							x = mapHeight/2; 
							continue;
						}
					}
				}
				if (sym == SymType.DIAGONAL) {
					Line.Point pt = line.getPointType(new MapLocation(x, y));
					if (relLoc == RelativeHQLocation.LEFT) {
						if (pt != Line.Point.LEFT) 
							continue; 
						
					} else {
						if (pt != Line.Point.RIGHT)
							continue;
						
					}
				}

				assessments[x][y] = assessSpot(x, y);
				
				if (!enoughBytecode()) {
					xIndex = x; 
					yIndex = y;
					//System.out.println("Ended at ["+ xIndex + ", " + yIndex + "].");
					return; 
				}
			}
		}
		finishedSearch = true; 
		
	}

	private static boolean inBounds(MapLocation loc) {
		return inBounds(loc.x, loc.y);
	}
	
	private static boolean inBounds(int x, int y) {
		return (x > -1) && (x < mapWidth) && (y > -1) && (y < mapHeight);
	}
	private static boolean enoughBytecode() {
		return Clock.getBytecodesLeft() > 100; 
	}

	public static MapLocation findBestPastureLocation() {
		int bestX = -1;
		int bestY = -1;
		double bestRate = 0;
		double rate;
		for (int j = 0; j < mapHeight; j++) {
			for (int i = 0; i < mapWidth; i++) {
				rate = assessments[i][j];
				if (rate == 0)
					continue;

				if (rate > bestRate) {
					bestRate = rate;
					bestX = i;
					bestY = j;
				}
			}
		}

		return new MapLocation(bestX, bestY);
	}

	private static double assessSpot(int x, int y) {
		if ( rc.senseTerrainTile(new MapLocation(x, y)) == TerrainTile.VOID)
			return 0;
		int j, k;
		double totalCowGrowth = 0;

		for (int i = 0; i < 42; i++) {

			j = x + xOffSet[i];
			k = y + yOffSet[i];
			if (!inBounds(j, k)) continue; 
			totalCowGrowth = totalCowGrowth + growths[j][k];
		}

		totalCowGrowth = totalCowGrowth * 100;

		return totalCowGrowth;

	}

	private static double distanceHeuristic(int x, int y) {
		MapLocation here = new MapLocation(x, y);
		double ourDist = ourHQ.distanceSquaredTo(here);
		double theirDist = enemyHQ.distanceSquaredTo(here);

		return (1 - (ourDist) / ((double) maxDistSquared))
				- (1 - (theirDist) / ((double) maxDistSquared));
	}

	public static boolean inBounds() {
		return true;
	}
}
