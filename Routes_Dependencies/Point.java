/*************************************************************************
  * Name: David Paulk and Regina Cai
  * Login: dpaulk and rrcai
  * Precept: Friday P01 and Friday P03
  * Date: October 3, 2012
  *
  * Compilation: javac Point.java
  * Execution: Since this does not have a main method (as per the
  * API), this is tested using a PointPlotter.java program. 
  * Dependencies: StdDraw.java
  * 
  * Description: An immutable data type for points in the plane.
  * Can draw the point, draw a line between 2 points, calculate
  * the slope between 2 points, compare two points lexicographically,
  * makes a String representation, and has a Comparator. 
  *
  *************************************************************************/

import java.util.Comparator;

public class Point implements Comparable<Point> {
    
    // Comparator called SLOPE_ORDER, see compare method below
    public final Comparator<Point> SLOPE_ORDER = new SlopeOrder();
    
    private final double x;                             // x coordinate
    private final double y;                             // y coordinate
    
    // create the point (x, y)
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // return x value of a point
    public double x() {
        return this.x;
    }
    
    // return y value of a point
    public double y() {
        return this.y;
    }
    
    // plot this point to standard drawing
    public void draw() {
        StdDraw.point(x, y);
    }
    
    // draw line between this and that point to standard drawing
    public void drawTo(Point that) {
        StdDraw.line(this.x, this.y, that.x, that.y);
    }
    
    // slope between this point and that point
    public double slopeTo(Point that) 
    {
        // y-difference between 2 points
        double yDelta = (that.y - this.y);
        // x-difference between 2 points
        double xDelta = (that.x - this.x);      
        
        if (yDelta == 0 && xDelta == 0)
        {
            // slope is negative infinity if same point
            return Double.NEGATIVE_INFINITY;    
        }
        
        if (yDelta == 0)
        {
            // slope is 0 if horizontal line
            return 0;                          
        }
        
        if (xDelta == 0)
        {
            // slope is positive infinity if vertical line
            return Double.POSITIVE_INFINITY;     
        }
        
        return (yDelta / xDelta);
    }
    
    // is this point lexicographically smaller than that one?
    // comparing y-coordinates and breaking ties by x-coordinates
    public int compareTo(Point that) 
    {
        if (this.y > that.y) return +1;
        else if (this.y < that.y) return -1;
        // break ties with x-coordinates
        else if (this.x > that.x) return +1;
        else if (this.x < that.x) return -1;
        return 0;
    }
    
    // return String representation of this point
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    
    // comparator that allows sorting by slope
    private class SlopeOrder implements Comparator<Point>
    {
        public int compare(Point q1, Point q2)
        {
            double slope1 = slopeTo(q1);
            double slope2 = slopeTo(q2);
            
            if (slope1 < slope2)
            {
                return -1;
            }
            
            if (slope1 > slope2)
            {
                return +1;
            }
            
            else return 0;
        }
    }
}