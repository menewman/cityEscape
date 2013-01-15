/* Routes of Evacuation
 * Author: David Paulk
 * Partners: Allan Jabri and Michael Newman
 * 
 * Compilation: javac Routes.java
 * Execution: java Routes roads.txt
 * 
 * Dependencies:
 * In.java, FlowNetwork.java, ST.java, EdgeWeightedDigraph.java
 * Intersection.java, Explosion.java, drawExplosion.java, Point.java
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
import java.awt.Font.*;
import java.util.*;

public class Routes {
    // Intersection data structures
    ST<Point, Intersection> joints; // indexed by detonation distance  
    ST<Point, Integer> index; // integer indices for digraph
    ST<Integer, Point> reverseIndex;
    
    private int numIntersections;
    private int numEdges;
    private int population;
    private int alive;
    private int dead;
    private int escaped;

    private EdgeWeightedDigraph evacGraph; // weighted graph of road network
    private FlowNetwork evacFlow; // desribes flow of people through routes
    
    // detonation information for method use
    private Explosion exp; // instance of detonation
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
        alive = population;
        dead = 0;
        escaped = 0;

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
            
//            for (int tt = 0; tt < fields.length; tt++) {
//                StdOut.println("fieldval " + tt + ": " + fields[tt]);
//            }
            
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
        this.buildNetwork(joints);
        this.populate(initPop);
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

    // accessor methods
    public int getAlive() { return alive; }
    public int getDead()  { return dead;  }
    public int getEscaped() { return escaped; }
    public int getPop() { return population; }

    // helper method
    public Point midpoint(double x1, double y1, double x2, double y2)
    {
        return new Point(0.5*(x1 + x2), 0.5*(y1 + y2));
    }
    // draws intersections and roads, with thickness proportional to capacity
    public void draw() {
        StdDraw.clear();
        Point midpoint;
//        StdDraw.Font f = new Font("SansSerif", Font.PLAIN, 10);
//        StdDraw.setFont(f);
        for (int i = 0; i < reverseIndex.size(); i++) {
            
            StdDraw.setPenRadius(0.01);
            StdDraw.point(reverseIndex.get(i).x(), reverseIndex.get(i).y());
            for (FlowEdge e : evacFlow.outgoing(i)) {
                StdDraw.setPenRadius(e.capacity()*0.001);
                StdDraw.setPenColor(StdDraw.BLUE);
                StdDraw.line(reverseIndex.get(i).x(), reverseIndex.get(i).y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
                StdDraw.setPenRadius(e.flow()*0.001);
                StdDraw.setPenColor(StdDraw.RED);
                StdDraw.line(reverseIndex.get(i).x(), reverseIndex.get(i).y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
                String stats = e.flow() + " / " + e.capacity();
                midpoint = midpoint(reverseIndex.get(i).x(), reverseIndex.get(i).y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
                StdDraw.setPenColor(StdDraw.BLACK);
                StdDraw.text(midpoint.x(), midpoint.y(), stats);
            }
//            for (FlowEdge e : evacFlow.incoming(i)) {
//                StdDraw.setPenRadius(e.capacity()*0.001);
//                StdDraw.setPenColor(StdDraw.BLUE);
//                StdDraw.line(reverseIndex.get(e.from()).x(), reverseIndex.get(e.from()).y(), reverseIndex.get(i).x(), reverseIndex.get(i).y());
//                StdDraw.setPenRadius(e.flow()*0.001);
//                StdDraw.setPenColor(StdDraw.RED);
//                StdDraw.line(reverseIndex.get(e.from()).x(), reverseIndex.get(e.from()).y(), reverseIndex.get(i).x(), reverseIndex.get(i).y());
//                String stats = e.flow() + " / " + e.capacity();
//                midpoint = midpoint(reverseIndex.get(e.from()).x(), reverseIndex.get(e.from()).y(), reverseIndex.get(i).x(), reverseIndex.get(i).y());
//                StdDraw.text(midpoint.x(), midpoint.y(), stats);
//            }
        }

        // draws update of hazard-radius with respect to detonation point    
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.05);
        StdDraw.point(0, 0);

        StdDraw.setPenRadius(0.025);
        StdDraw.circle(0, 0, hazardRadius);
        StdDraw.show(300);
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

        // set explosion and standard draw window to illustrate full magnitude
        exp = new Explosion(5.0); // 5 megaton explosion initialized
        StdDraw.setXscale(-5, 5);
        StdDraw.setYscale(-5, 5);

        return evacFlow;
    }
    
    /*
     * distribute pseudorandom flow across flow network
     * (as of now, no safety against going over-capacity in initial
     *  distribution of population)
     */
    private void populate(int population) {
        Random rand = new Random();
        Iterable<FlowEdge> list = evacFlow.edges();
        FlowEdge[] edgeArray = new FlowEdge[evacFlow.E()];
        int i = 0;
        for (FlowEdge e : list) {
            edgeArray[i] = e;
            i++;
        }
        
        // add flow to edges until flow represents entire population
        int added = 0;
        while (added < population) {
            // random int on domain [0, # of edges)
            i = rand.nextInt(edgeArray.length);
            
            // pick an edge and increment its flow
            edgeArray[i].addFlow(1);
            added++;
        }
        StdOut.println("populate has added: " + added); // DEBUG
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
    
    /*
     * update road network by iteratively transfering population flow between roads
     */
    public void nextState() {
        // copy the road network with same vertices/edges/capacity but zero flow
        FlowNetwork nextFlow = new FlowNetwork(evacFlow);
        
        for (int i = 0; i < joints.size(); i++) {
            update(i, nextFlow);
        }
        //for (int i = 0; i < evacFlow.V(); i++)
            //update(i, nextFlow);

        //StdOut.println(nextFlow);
        evacFlow = nextFlow;
    }
    
    /*
     * updates flow incident of a single intersection
     */
    public void update(int i, FlowNetwork f) {
        double inFlow = 0;
        int outs = 0; // out edge count
        // sum inflow
        for (FlowEdge e : evacFlow.incoming(i)) {
            // DEBUG <- the below code doesn't account for over-capacity edges,
            //  but the code previously just didn't do anything with the extra flow!
            inFlow += e.flow();
        }
        if (inFlow == 0)
            return;
        
        // are these people dead?        
        if (detDist(reverseIndex.get(i)) <= hazardRadius) {
            //StdOut.println("PEOPLE SHOULD BE DYING: " + inFlow); // DEBUG
            // DEBUG -> this doesn't account for people waiting at overcapacity edges

            dead += inFlow;
            alive -= inFlow;
            /*for (FlowEdge e : f.incoming(i)) {
                // this does it edge by edge and accounts for all cars at the intersection
                // possible bug; counting for cars at the intersection is delayed
                // because the time step may gloss over distances.
                e.setFlow(0);
            }*/
            return;
        }

        // have people escaped the city?
        if (detDist(reverseIndex.get(i)) > this.hazardLimit()) {
            //StdOut.println("PEOPLE ARE ESCAPING: " + inFlow); // DEBUG
            escaped += inFlow;
            alive -= inFlow;
            for (FlowEdge e : f.incoming(i)) {
                e.setFlow(0); // changed it to set to zero just to see if it works
            }
            return;
        }

        //if (inFlow > 0)
            //StdOut.println("THESE PEOPLE ARE ALIVE: " + inFlow); // DEBUG

        double dist; //distribution
        double specialK; // if we want to change the range from which random probabilities can be picked
        double delta;
        double sum = 0;
        int counter = 0;
        
        for (FlowEdge to : f.outgoing(i)) {
            outs++;
        }

        double[] distribution = new double[outs];

        // fraction of an incoming street going to each outgoing street
        for (int j = 0; j < outs; j++) {
            dist = Math.random();
            distribution[j] = dist;
            sum += dist;
        }

        // calculate how much flow goes to each edge out
        double[] outflow = new double[outs];
        double outflowSum = 0;
        for (int j = 0; j < outs; j++) {
            outflow[j] = inFlow * distribution[j] / sum;
            outflowSum += outflow[j];
        }
        if ((outflowSum < inFlow) && (outs != 0))
            outflow[0] += (inFlow - outflowSum);
        else if ((outflowSum < inFlow) && (outs == 0)) {
            Iterator<FlowEdge> itr1 = evacFlow.incoming(i).iterator();
            Iterator<FlowEdge> itr2 = f.incoming(i).iterator();
            while (itr1.hasNext())
                itr2.next().setFlow(itr1.next().flow());
            return;
        }

        // model random traffic
        counter = 0;
        for (FlowEdge to : f.outgoing(i)) {
                // distribution is normalized with 1/sum
                
                //from.addFlow(-1.0*delta*distribution[counter]/sum);
                //to.addFlow(delta*distribution[counter]/sum);
            //StdOut.println("counter: " + counter);
            to.addFlow(outflow[counter]);
            counter++;
        }
            
            // use awareness factor to determine preference in pseudorandom
            // dispersal of flow
            
            // apply incident flow in summation of algorithm and summation
            // of time-step's effect through input intersection

        //StdOut.println("People just shuffled: " + inFlow); // DEBUG
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
     * hazard never goes past 2/3's distance of farthest intersection
     */
    public double hazardLimit() {
        return detDist(joints.max()) * (0.67) ;
    }
    
    /* 
     * is point on coordinate map is within a hazardous range?
     */
    public boolean isVulnerable(Point p) {
        return (detDist(p) <= hazardRadius);
    }
     
    /*
     * test method (simulation test)
     */
    public static void main(String[] args)
    {
        // gives name of file containing data for street routes
        int population = Integer.parseInt(args[1]);
        Routes test = new Routes(args[0], population);
        
        // build the flow network
        test.buildNetwork(test.joints);
        
        // save, print and draw copy of road network before adding any flow
        FlowNetwork emptyNetwork = test.roadNetwork();
        StdOut.println("Flow-empty Network: ");
        StdOut.println(emptyNetwork.toString());
        StdOut.println();
        test.draw();
        
        // save, print and draw copy of flow-initialized road network       
        test.populate(population); // add randomized initial flow to road network
        FlowNetwork initNetwork = test.roadNetwork();
        StdOut.println("Flow-initialized Network: ");
        StdOut.println(initNetwork.toString());
        StdOut.println();
        
        /*
        // iterate through time-steps until final scenario has been determined
        int t = 0;
        test.setHazardRadius(0);
        double limit = test.hazardLimit();
        while (test.hazardRadius < limit) {
            test.nextState();
            t++;
            test.setHazardRadius(t);
        }
        */
    }
}
