package communicator2;

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
	public static double[][] growths;
	public static MapLocation bestFound = null;
	public static MapLocation secondBestFound = null;
	public static double bestGrowth = -Double.MAX_VALUE;
	public static int searchProgress = 0;
	
	public static int boxMaxX,boxMaxY,boxMinX,boxMinY;
	
	public static void searchForSpots(RobotController rc, int linesToWork){
		if(searchProgress == 0){
			width = rc.getMapWidth();
			height = rc.getMapHeight();
			growths = rc.senseCowGrowth();
			MapLocation middle = new MapLocation(width/2,height/2);
			MapLocation hqLoc = HQBehavior.thisPos;
			System.out.println(middle);
			
			int smalerX = hqLoc.x < middle.x? 0:1;
			int smalerY = hqLoc.y < middle.y? 0:1;
			
			MapLocation corner = new MapLocation(smalerX*(width-1),smalerY*(height-1));
			
			boxMaxX = Math.max(middle.x, corner.x);
			boxMinX = Math.min(middle.x, corner.x);
			boxMaxY = Math.max(middle.y, corner.y);
			boxMinY = Math.min(middle.y, corner.y);
			
			boxMinX = Math.max(1, boxMinX);
			boxMinY = Math.max(1, boxMinY);
		}
		for(int x = boxMinX; x < boxMaxX; x ++){
			for(int y = boxMinY; y < boxMaxY; y++){
				double currGrowth = 0;
				currGrowth += growths[x-1][y];
				currGrowth += growths[x][y-1];
				currGrowth += growths[x-1][y-1];
				currGrowth += growths[x+1][y];
				currGrowth += growths[x][y+1];
				currGrowth += growths[x+1][y+1];
				currGrowth += growths[x+1][y-1];
				currGrowth += growths[x-1][y+1];
				currGrowth += growths[x][y];
				if(currGrowth > bestGrowth){
					bestGrowth = currGrowth;
					if(bestFound != null){
						secondBestFound = new MapLocation(x,y);
					} else{
						bestFound = new MapLocation(x,y);
					}
				}
			}
		}
		System.out.println(Clock.getRoundNum() + " finished");
	}
}
