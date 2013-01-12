/* Routes of Evacuation
 * Author: David Paulk
 * Partners: Allan Jabri and Michael Newman
 * 
 * Compilation: javac Routes.java
 * Execution: java Routes roads.txt
 * 
 * Dependencies:
 * Point.java, DirectedEdge.java, EdgeWeightedDigraph.java, FlowEdge.java
 * FlowNetwork.java, FordFulkerson.java, ST.java, In.java
 * 
 * Description: Routes.java shows the routes that a population can
 * travel along during an evacuation scenario.  The routes created
 * by implementing a directed graph with weighted edges.  The digraph
 * has edges which are characterized by travel capacity, length, and
 * bandwidth.  The vertices are nodes and are characterized by their
 * set of coordinates, which are each an intersection among roads.
 * To efficiently store intersections in a symbol table indexed by
 * coordinate-set keys, Point instances are made and the comparator
 * for the Point class is used to determine when items should be added
 * to or taken out of the symbol table.
 * 
 * The following 3 questions are answered by the graphing analysis:
 * 1- What roads stand out as the best path to take to escape?
 *  > This question can be answered by looking at the shortest
 *    path along the directed graph.  If the graph were to be
 *    undirected, this question could be answered simply by taking
 *    the convex hull of all intersection vertex-points, but since
 *    it is directed, a shortest path finding algorithm must be
 *    implemented after first creating a graphical representation
 *    of the streets and intersections.
 * 
 * 2- What are the bottleneck roads of escape? (Which roads get
 * clogged up first in an evacuation of large enough scale to clog
 * some roads?)
 *  > This question is answered by evacuation analysis using a
 *    Ford-Fulkerson Algorithm, specifically through it's max-flow
 *    and min-cut finding properties.  The road-edges that are
 *    not, according to the Ford-Fulkerson Algorithm, in the min-cut
 *    have reached their bottleneck capacity for a current
 *    road population distribution.
 * 
 * 3- What matters more: the travel capacity of a road, or how close
 * to normal wrt to the explosion radius road's direction is?
 *  > This question can be answered by observing the effects of using
 *    different roadway capacities and different explosion radii in
 *    the simulation portion of the project 'Routes' is a sub-class of.
 *    The importance of capacity and explosion radius are only comparable
 *    within the explosion radius, and they are comparable in importance
 *    there because a smaller radius covers a smaller and more diluted
 *    representation of the total population while road capacity, though
 *    considering the total population along each road-edge, can allow
 *    looser or stricter fluctuation in the magnitutde of evactuation
 *    along each individual road-segment, affecting the whole network.
 *    
 */

/* STUFF TO STILL ADD TO THE CODE (POSSIBLY): 
 *  > SHORTEST PATH FOR BEST PATH OF ESCAPE
 *  > RADIUS OF HAZARD DUE TO DETONATION CHANGING OVER TIME
 */

import java.util.Random;

public class Routes {
    
       
    /*
     * makes Node for a vertex on the directed graph
     */ 
    
    
    /*
     * Intersection data structures
     */
    ST<Point, Intersection> joints; // indexed by detonation distance  
    ST<Point, Integer> index; // integer indices for digraph
    ST<Integer, Point> reverseIndex;
    int numIntersections;
    int numEdges;
    int population;
    
    private static EdgeWeightedDigraph evacGraph; // weighted graph of road network
    private FlowNetwork evacFlow; // desribes flow of people through routes
    
    // detonation information for method use
    private double hazardRadius; // radius of danger
    private double detX; // x-coordinate of detonation
    private double detY; // y-coordinate of detonation
    private Point det; // location of detonation
    
      
    
    /*
     * creates a graphical evacuation map
     */
    public Routes(String filename, int initPop) 
    {
        // GETTING TO ROUTES METHOD TESTED AND WORKS
        
        In in = new In(filename); // initialize source of input
        String[] alert = in.readLine().trim().split("\\s+");
        population = initPop;
        
        // ALERT LINE TESTED AND WORKS
        
        // radius of hazard region and a location of detonation
        hazardRadius = Double.parseDouble(alert[0]);
        detX = Double.parseDouble(alert[1]);
        detY = Double.parseDouble(alert[2]);
        det = new Point(detX, detY);
        
        // intersections indexed by coordinate's distance from detonation
        joints = new ST<Point, Intersection>(); 
        
        // create indices for intersections
        index = new ST<Point, Integer>();
        reverseIndex = new ST<Integer, Point>();
        
        // reads input by road name, orientation (true is horizontal,
        // false is vertical) with respect to the map,
        // 'from-intersection' coordinates, 'to-intersection' 
        // coordinates, road-width, and road-length
         final int FIELDSIZE = 7;
        String[] fields = new String[FIELDSIZE]; // holds input line
        
        // store information provided by input in arrays 
        numIntersections = 0;
        numEdges = 0;
        
        while (!in.isEmpty())
        {
            // split input by spaces seperating them & store values
            fields = in.readLine().trim().split("\\s+");
            
            // name of street and whether or not it is horizontal
            boolean orientation = Boolean.parseBoolean(fields[0]);
            
            // x and y coordinates for road's beginning and end
            double fromX = Double.parseDouble(fields[1]);
            double fromY = Double.parseDouble(fields[2]);
            Point fromPoint = new Point(fromX, fromY);
            double toX = Double.parseDouble(fields[3]);
            double toY = Double.parseDouble(fields[4]);
            Point toPoint = new Point(toX, toY);
            
            double roadWidth = Double.parseDouble(fields[5]);
            double roadLength = Double.parseDouble(fields[6]);
            double capacity = roadWidth*roadLength;
            
            for (int tt = 0; tt < fields.length; tt++) {
                StdOut.println("fieldval " + tt + ": " + fields[tt]);
            }
            
            // add to, create or overwrite connection to intersections
            double fromDist = detDist(fromPoint);
            double toDist = detDist(toPoint);
            
            Intersection from;
            // if 'from' intersection already found, initialize it as such
            if (joints.contains(fromPoint)) {
                from = joints.get(fromPoint);
            }
            // if 'from' intersection not yet found, create node for it
            else {
                from =
                    new Intersection(null, null, null, null, fromPoint);
                from.inEdges = new Queue<FlowEdge>();
                from.outEdges = new Queue<FlowEdge>();
                joints.put(fromPoint, from);
                index.put(fromPoint, numIntersections);
                reverseIndex.put(numIntersections, fromPoint);
                
                numIntersections++; // increment integer number index for new intersection
            }
            // if 'to' intersection already found, overwrite it
            Intersection to;
            if (joints.contains(toPoint)) {
                to = joints.get(toPoint);
            }
            
            // if 'to' intersection not yet found, create node for it
            else {
                to =
                    new Intersection(null, null, null, null, toPoint);
                to.inEdges = new Queue<FlowEdge>();
                to.outEdges = new Queue<FlowEdge>();
                joints.put(toPoint, to);
                index.put(toPoint, numIntersections);
                reverseIndex.put(numIntersections, toPoint);
                numIntersections++; // increment integer number index for new intersection
            }
            
            /*
             * update intersection variables stored in joints ST
             */   

            // direction of road could be an input parameter in file
            // if one way, only need to create one direction
            
            // create new edge to represent road read in
            FlowEdge edge = new FlowEdge(index.get(fromPoint), index.get(toPoint), capacity);
            
            to.inEdges.enqueue(edge);
            from.outEdges.enqueue(edge);
           
                     
            /*
             * update table of nodes to store road's existence between
             * two intersections and add neighboring intersection nodes
             */ 
            
            if ((fromX < toX) && orientation) { // join east and west
                from.east = to;                  
                to.west = from;                  
            }   
            if ((fromX > toX) && orientation) { // join east and west
                from.west = to;
                to.east = from;
            }        
            if ((fromY < toY) && !orientation) { // join north and south
                from.north = to;
                to.south = from;
            }           
            if ((fromY > toY) && !orientation) { // join north and south
                from.south = to;
                to.north = from;
            }
            
            joints.put(toPoint, to);
            joints.put(fromPoint, from);
        }    
    }
    
    
    
    /*
     * build an edge weighted digraph from intersections in joints ST
     */ 
    public EdgeWeightedDigraph graph(ST<Point, Intersection> joints) {
        
        // copying symbol table to modify it *deep copy*
        ST<Point, Intersection> j = new ST<Point, Intersection>();
        
        // possible bug with not creating deep copy
        for (Point p : joints.keys()) 
            j.put(p, joints.get(p));
            
        // create new digraph
        evacGraph = new EdgeWeightedDigraph(numEdges);
        
        // add in edges
        for (Point p2 : j.keys()) {      
            // adds edges to graph by looking at intersections
            // in a counterclockwise routine
            
            while (!j.get(p2).inEdges.isEmpty()) {
                //evacGraph.addEdge(joints.get(p2).inEdges.dequeue());  
            }
        }
        return evacGraph;
    }
    // draws intersections and roads, with thickness proportional to capacity
    public void draw() {
        StdDraw.setScale(-5, 5);
        for (Point p : joints.keys()) {
            
            Intersection i = joints.get(p);
            StdDraw.setPenRadius(0.005);
            StdDraw.point(p.x(), p.y());
            for (FlowEdge e : i.outEdges) {
                StdDraw.setPenRadius(e.capacity()*0.001);
                StdDraw.setPenColor(StdDraw.BLUE);
                StdDraw.line(p.x(), p.y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
                StdDraw.setPenRadius(e.flow()*0.001);
                StdDraw.setPenColor(StdDraw.RED);
                StdDraw.line(p.x(), p.y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
            }
            for (FlowEdge e : i.inEdges) {
                StdDraw.setPenRadius(e.capacity()*0.001);
                StdDraw.setPenColor(StdDraw.BLUE);
                StdDraw.line(p.x(), p.y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
                StdDraw.setPenRadius(e.flow()*0.001);
                StdDraw.setPenColor(StdDraw.RED);
                StdDraw.line(p.x(), p.y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
            }
        }
    }
              
    /*
     * create flow network for evacuation directed away from
     * detonation towards safe distance
     */ 
    public FlowNetwork buildNetwork(ST<Point, Intersection> joints) {
        
        // ****** not sure if we need to do the +2, but its kept until we figure out how to represent escape routes + source
        evacFlow = new FlowNetwork(numIntersections + 2);
        int detSource = numIntersections; // virtual detonation source
        int safeSink = numIntersections + 1; // virtual safe-zone sink
        
        // copying symbol table to modify it *deep copy*
        ST<Point, Intersection> j = new ST<Point, Intersection>();
        
        // ****** possible bug with not creating deep copy
        for (Point p : joints.keys()) 
            j.put(p, new Intersection(joints.get(p)));

        for (Point p : j.keys()) {
            // adds edges to graph by looking at intersections
            // in a counterclockwise routine
            
            while (!j.get(p).inEdges.isEmpty()) {
                evacFlow.addEdge(j.get(p).inEdges.dequeue());  
            }
        }
        return evacFlow;
    }
    
    
    
    /*
     * distance from detonation to given point on coordinate map
     */ 
    public double detDist(Point p) {  
        double distSq = (detX - p.x())*(detX - p.x())
            + (detY - p.y())*(detY - p.y());
        return Math.sqrt(distSq);
    }
    
    /*
     * probablility function for whether or not smart choice of direction
     * is made at each time step 
     */
    public double awareness(Point p) {
        double rand = Math.random();
        double desparation = 
            rand*hazardRadius/(hazardRadius + detDist(p));
        return desparation;
    }
    // update flownetwork
    public void nextState() {
        
        FlowNetwork nextFlow = new FlowNetwork(evacFlow);
        ST<Integer, Double> randoms = new ST<Integer, Double>();
        ST<Point, Intersection> nextJoints = new ST<Point, Intersection>();

        for (int i = 0; i < joints.size(); i++) {
            update(i, randoms, nextFlow);
        }
        
        // refresh ST and flownetwork
        joints = nextJoints;
        buildNetwork(joints);
    }
    
    /*
     * updates flow incident of a single intersection
     */
    public void update(int i, ST<Integer, Double> randoms, FlowNetwork f) {
        
        int inFlow = 0;
        // sum inflow
        for (FlowEdge e : evacFlow.incoming(i)) {
            if (e.flow() > e.capacity()) inFlow += e.capacity();
            else {
                inFlow += e.flow();
            }
        }
        
        // use detDist to determine if people die/flow is eliminated
        if (detDist(reverseIndex.get(i)) < hazardRadius) {
            population -= inFlow;
            for (FlowEdge e : f.incoming(i)) {
                e.setFlow(0);
            }
        }

        double distribution;
        double excess;
        double delta;
        double sum;
        double fractionReceived = 0;
        // model random traffic
        for (FlowEdge to : f.outgoing(i)) {
            sum = 0;
            fractionReceived = Math.random();
            for (FlowEdge from : evacFlow.incoming(i)) {
                // distribution is normalized with 1/sum
                delta = from.flow()*fractionReceived;
                to.addFlow(delta);
                from.addFlow(-1.0*delta);
            }
            
            // use awareness factor to determine preference in pseudorandom
            // dispersal of flow
            
            // apply incident flow in summation of algorithm and summation
            // of time-step's effect through input intersection
            
        }
    }
    
    /*
     * turns the road network's weighted graph into string for output
     */ 
    public String routesToString() {
        return evacGraph.toString();
    }
    
    /*
     * turns the flow network of the roads into string for output 
     */
    public String roadNetworkToString() {
        FlowNetwork evac = roadNetwork();
        return evacGraph.toString();
    }
    
    
    /*
     * gives weighted & directed graphical representation of routes
     */
    public EdgeWeightedDigraph roadGraph() {
        if (evacGraph == null) throw new RuntimeException("no graph");
        return evacGraph;
    }
    
    /* 
     * flow network for a given population along the road network
     * assuming detonation's effects only come into play for max flow
     */
    public FlowNetwork roadNetwork() {
        if (evacFlow == null) throw new RuntimeException("no network");
        return evacFlow;
    }
    
    public void setHazardRadius(double r) {
        this.hazardRadius = r;
    }
    
    /* 
     * is point on coordinate map is within a hazardous range?
     */
    public boolean isVulnerable(Point p) {
        return (detDist(p) <= hazardRadius);
    }
    
    
    /*
     * test method
     */
    public static void main(String[] args)
    {
        // gives name of file containing data for street routes
        Routes test = new Routes(args[0], Integer.parseInt(args[1]));
        test.buildNetwork(test.joints);
        FlowNetwork flow = test.roadNetwork(); // before flow
        test.draw();
        StdOut.println(flow.toString());                            
        
    }
}
