package communicator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import battlecode.common.*;
/**
 * This class is responsible for finding good locations to assemble and/or build a pasture.
 * At the moment the map-building functions are not used because no map information is exchanged 
 * between the HQ and the soldiers.
 * The finding of good locations is currently used but it will be changed in the near future because
 * it is not robust.
 * 
 * 
 * @author Alexander Bartl
 *
 */
public class MapMaker {
	
	public static int width,height;
	public static int[][] map;
	public static int[][] importanceMap;
	public static double[][] growths;
	public static MapLocation bestFound = null;
	public static double bestGrowth = -Double.MAX_VALUE;
	public static int searchProgress = 0;

	public static void makeMap(RobotController rc){
		width = rc.getMapWidth();
		height = rc.getMapHeight();
		map = new int[width][height];
		for(int x = 0; x < width; x ++){
			for(int y = 0; y < height; y++){
				map[x][y] = rc.senseTerrainTile(new MapLocation(x,y)).ordinal();			
			}
		}
	}
	public static void searchForSpots(RobotController rc, int linesToWork){
		if(searchProgress == 0){
			growths = rc.senseCowGrowth();
		}
		
	}
//	public static void buildImportanceMap(RobotController rc){
//		width = rc.getMapWidth();
//		height = rc.getMapHeight();
//		double[][] growths = rc.senseCowGrowth();
//		importanceMap = new double[width][height];
//		for(int i = 0; i < 1; i ++){
//			for(int y = 1; y < height-1; y++){
//				for(int x = 1; x < width-1; x ++){
//					importanceMap[x-1][y] += growths[x][y];
//					importanceMap[x][y-1] += growths[x][y];
//					importanceMap[x-1][y-1] += growths[x][y];
//					importanceMap[x+1][y] += growths[x][y];
//					importanceMap[x][y+1] += growths[x][y];
//					importanceMap[x+1][y+1] += growths[x][y];
//					importanceMap[x+1][y-1] += growths[x][y];
//					importanceMap[x-1][y+1] += growths[x][y];
//				}
//			}
//		}
//		for(int y = 0; y < height; y++){
//			for(int x = 0; x < width; x ++){
//				growths[x][y] = importanceMap[x][y];
//			}
//		}
//		System.out.println("finishMap: " + Clock.getRoundNum());
////		for(int i = 0; i < growths.length; i ++){
////			System.out.println(Arrays.toString(growths[i]));
////		}
//	}
	public static MapLocation[] buildImportanceMap(int num, RobotController rc){
		importanceMap = new int[width][height];
		int[][] tmpMap = new int[width][height];
		int xStart = StaticVariables.HQ_FIELD_WIDTH;
		int xEnd = width-StaticVariables.HQ_FIELD_WIDTH;
		int yStart = StaticVariables.HQ_FIELD_HEIGHT;
		int yEnd = height-StaticVariables.HQ_FIELD_HEIGHT;
		int count = 0;
		for(int y = yStart; y < yEnd; y++){
			for(int x = xStart; x < xEnd; x ++){
				if(map[x][y] == 0){
					importanceMap[x][y] = 1;
					count ++;
				}
			}
		}
		for(int i = 0; i < 1; i ++){
			for(int y = yStart; y < yEnd; y++){
				for(int x = xStart; x < xEnd; x ++){
					int result = 1;
					result*= importanceMap[x-1][y];
					result*= importanceMap[x][y-1];
					result*= importanceMap[x-1][y-1];
					result*= importanceMap[x][y];
					result*= importanceMap[x+1][y];
					result*= importanceMap[x][y+1];
					result*= importanceMap[x+1][y+1];
					result*= importanceMap[x+1][y-1];
					result*= importanceMap[x-1][y+1];
					if(result == 0 && importanceMap[x][y] == 1){
						count --;
					}
					tmpMap[x][y] = result;
				}
			}
			for(int y = yStart; y < yEnd; y++){
				for(int x = xStart; x < xEnd; x ++){
					importanceMap[x][y] = tmpMap[x][y];
				}
			}
		}
		MapLocation[] possible = new MapLocation[count];
		int index = 0;
		for(int y = yStart; y < yEnd; y++){
			for(int x = xStart; x < xEnd; x ++){
				if(importanceMap[x][y] == 1){
					possible[index] = new MapLocation(x,y);
					index ++;
				}
			}
		}
		MapLocation[] result = new MapLocation[num];
		double best = 0;
		for(int l = 0; l < 6; l++){
			MapLocation[] currentLocs =  new MapLocation[num];
			double[] worstDistances = new double[num];
			double current = 0;
			for(int i = 0; i < num; i ++){
				worstDistances[i] = 1000000;
			}
			for(int i = 0; i < num; i ++){
				int pos = (int) (Math.random()*possible.length);
				currentLocs[i] = possible[pos];
			}
			for(int i = 0; i < num; i ++){
				for(int k = 0; k < num; k ++){
					if(i != k){
						double distance = currentLocs[i].distanceSquaredTo(currentLocs[k]);
						if(distance < worstDistances[i]){
							worstDistances[i] = distance;
						}
					}
				}
				current += worstDistances[i];
			}
			if(current > best){
				best = current;
				for(int i = 0; i < num; i ++){
					result[i] = new MapLocation(currentLocs[i].x,currentLocs[i].y);
				}
			}
		}
//		System.out.println("worst: " + Arrays.toString(result) + " best: " + best);
//		for(int i = 0; i < num; i ++){
//			importanceMap[result[i].x][result[i].y] = 2;
//		}
		final MapLocation hq = rc.senseHQLocation();
		Comparator c = new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				MapLocation m1 = (MapLocation) arg0;
				MapLocation m2 = (MapLocation) arg1;
				int dis1 = m1.distanceSquaredTo(hq);
				int dis2 = m2.distanceSquaredTo(hq);
				if(dis1 == dis2){
					return 0;
				}
				return dis1 > dis2? 1:-1;
			}
			
		};
		Arrays.sort(result, c);
		return result;
	}
	
	public static void displayMap(){
		for(int y = 0; y < height; y++){
			String line = "";
			for(int x = 0; x < width; x++){
				line += importanceMap[x][y]+"-";
			}
			System.out.println(line);
		}
	}
	public static void broadcastMap(RobotController rc) throws Exception{
		int num = 0;
		for(int y = 0; y < height; y ++){
			for(int x = 0; x < width; x ++){
				rc.broadcast(num, map[x][y]);
				num++;
			}
		}
	}
}
