/* Intersection
 * 
 * Author: David Paulk
 * Partners: Michael Newman and Allan Jabri
 * 
 * Compilation: javac Intersection.java
 * Execution: --
 * Dependencies: Point.java, StdOut.java
 * 
 * Description: Intersection creates an intersection Node that can
 * be compared to other intersections by its Point's coordinate pair.
 */

/*
 * makes Node for a vertex on the directed graph
 */ 

public class Intersection implements Comparable<Intersection>
{
    // intersections surrounding current intersection
    Intersection north, south, east, west;
    
    Queue<FlowEdge> inEdges;
    Queue<FlowEdge> outEdges;
    int inFlow;
    int outFlow;
    
    // coordinates corresponding to current intersection
    Point p;
    
    // create new intersection-node (constructor)
    public Intersection(Intersection north, Intersection south,
                        Intersection east, Intersection west,
                        Point p) {
        StdOut.println("Intersection created"); // TESTING
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.p = p;
    } 
    // copy constructor
    public Intersection(Intersection i) {
        StdOut.println("Intersection copied"); // TESTING
        this.north = i.north;
        this.south = i.south;
        this.east = i.east;
        this.west = i.west;
        this.p = i.p;
        this.inFlow = i.inFlow;
        this.outFlow = i.outFlow;
        this.inEdges = new Queue<FlowEdge>();
        for (FlowEdge e : i.inEdges)
            this.inEdges.enqueue(e);
        this.outEdges = new Queue<FlowEdge>();
        for (FlowEdge e : i.outEdges)
            this.outEdges.enqueue(e);
    }
    
    public int inFlow() {
        for (FlowEdge e : this.inEdges)
            this.inFlow += e.flow();
        return this.inFlow;
    }
    
    public int outFlow() {
        for (FlowEdge e : this.outEdges)
            this.outFlow += e.flow();
        return this.outFlow;
    }
    
    public int size(Iterable<FlowEdge> edges) {
        // find number of in-flow edges
        int size = 0;
        for (FlowEdge e : edges) {
            size++;
        }
        return size;
    }
    
    // is this Intersection's point smaller than that one?
    // comparing y-coordinates and breaking ties by x-coordinates
    public int compareTo(Intersection that) {
        return this.p.compareTo(that.p);
    }
}