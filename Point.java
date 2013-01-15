/*************************************************************************
 *  Compilation:  javac Point.java
 *  Execution:    java Point
 *
 *  Immutable data type for 2D points.
 *
 *************************************************************************/

public class Point { 
    private double x;   // Cartesian
    private double y;   // coordinates
   
    // create and initialize a random point in unit square
    public Point() {
        this.x = Math.random();
        this.y = Math.random();
    }

    // create and initialize a point with given (x, y)
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // create and initialize a point from input stream
    public Point(In in) {
        x = in.readDouble();
        y = in.readDouble();
    }

    // accessor methods  
    public double x() { return x; }
    public double y() { return y; }

    // return Euclidean distance between this point and that point
    public double distanceTo(Point that) {
        double dx = this.x - that.x;
        double dy = this.y - that.y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    // draw point using standard draw
    public void draw() {
        StdDraw.point(x, y);
    }

    // draw the line from this point to that point
    public void drawTo(Point that) {
        StdDraw.line(this.x, this.y, that.x, that.y);
    }

    // return string representation of this point
    public String toString() {
        return "(" + x + ", " + y + ")";
    }



    // test client
    public static void main(String[] args) {
        Point p = new Point();
        System.out.println("p  = " + p);
        Point q = new Point(0.5, 0.5);
        System.out.println("q  = " + q);
        System.out.println("dist(p, q) = " + p.distanceTo(q));
    }
}
