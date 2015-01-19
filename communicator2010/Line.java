package communicator2010;

import battlecode.common.MapLocation;

public class Line{
	private double m; // gradient 
	private double c; // y - intercept
	public enum Point{
		LEFT, RIGHT, ON_LINE;
	}
	Line(MapLocation A, MapLocation B) {
		double xf = B.x; 
		double yf = B.y; 
		double xi = A.x; 
		double yi = A.y; 
		
		m = (yf - yi)/(xf - xi);
		c = yi - m*xi;  
	}
	
	Line(MapLocation A, double gradient) {
		double x = A.x; 
		double y = A.y; 
		
		m = gradient; 
		c = y - m*x;  
	}
	
	public Point getPointType(MapLocation loc) {
		double rhs = Math.round(m*((double) loc.x) + c);
		double y = (double) loc.y; 
		if (y == (rhs))
			return Point.ON_LINE; 
		else if (y > (rhs)) 
			return Point.LEFT;
		else 
			return Point.RIGHT; 
	}
	
	
	public MapLocation getLineMapLoc(MapLocation loc) {
		return new MapLocation(loc.x, (int) Math.round(m*((double) loc.x) + c));
	}
	
	public boolean locOnLine(MapLocation loc, double tolerance) {
		double rhs =  Math.round(m*((double) loc.x) + c);
		double y = (double) loc.y;
		return y < (rhs + tolerance) && y > (rhs - tolerance);
	}
	
	public boolean locOnLine(int x, int y) {
		double rhs = Math.round(m*((double) x) + c);
		double y2 = (double) y;
		return y < (rhs + 1) && y > (rhs - 1);
	}
}
