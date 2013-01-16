/* Routes.java
 * Compilation: javac Routes.java
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
    private double population;
    private double alive;
    private double dead;
    private double escaped;

    private EdgeWeightedDigraph evacGraph; // weighted graph of road network
    private FlowNetwork evacFlow; // desribes flow of people through routes
    
    // detonation information for method use
    private Explosion exp; // instance of detonation
    private double hazardRadius; // radius of danger
    private double detX; // x-coordinate of detonation
    private double detY; // y-coordinate of detonation
    private Point det; // location of detonation
    private final int MULT = 6; // multiplier of input capacity to characterize roads
    
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
        hazardRadius = 0;
        int mapVersion = Integer.parseInt(alert[0]); // map version
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
            double capacity = roadWidth*roadLength*((double) MULT);

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
        //this.evacFlow = new FlowNetwork(evacFlow);
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
    public double getAlive() { return alive; }
    public double getDead()  { return dead;  }
    public double getEscaped() { return escaped; }
    public double getPop() { return population; }

    // sums up the flow on the graph
    public double calculateLiveFlow() {
        double aliveCounter = 0;
        for (FlowEdge e : evacFlow.edges()) {
            aliveCounter += e.flow();
        }
        return aliveCounter;
    }

    // helper method
    public Point midpoint(double x1, double y1, double x2, double y2)
    {
        return new Point(0.5*(x1 + x2), 0.5*(y1 + y2));
    }
    // draws intersections and roads, with thickness proportional to capacity
    public void draw() {
        StdDraw.setScale(-5, 5);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        Point midpoint;
//        StdDraw.setCanvasSize(800, 600);
//        StdDraw.Font f = new Font("SansSerif", Font.PLAIN, 10);
//        StdDraw.setFont(f);
        for (int i = 0; i < reverseIndex.size(); i++) {
            
            StdDraw.setPenRadius(0.01);
            StdDraw.point(reverseIndex.get(i).x(), reverseIndex.get(i).y());
            for (FlowEdge e : evacFlow.outgoing(i)) {
                StdDraw.setPenColor(StdDraw.GRAY);
                StdDraw.setPenRadius(e.capacity()*0.005);
                StdDraw.line(reverseIndex.get(i).x(), reverseIndex.get(i).y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
                StdDraw.setPenColor(StdDraw.WHITE);
                StdDraw.setPenRadius(e.flow()*0.005);
                StdDraw.line(reverseIndex.get(i).x(), reverseIndex.get(i).y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
                String stats = e.flow() + " / " + e.capacity();
                midpoint = midpoint(reverseIndex.get(i).x(), reverseIndex.get(i).y(), reverseIndex.get(e.to()).x(), reverseIndex.get(e.to()).y());
                
                StdDraw.text(midpoint.x(), midpoint.y(), stats);
            }
        }

        // draws update of hazard-radius with respect to detonation point    
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setPenRadius(0.005);
        StdDraw.point(0, 0);

        StdDraw.setPenRadius(0.0025);
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
        //exp = new Explosion(5.0); // 5 megaton explosion initialized
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
     * distance from detonation's center to given point on coordinate map
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
        double desperation = 
            rand*hazardRadius/(hazardRadius + detDist(p));
        return desperation;
    }

    /*
     * update road network by iteratively transfering population flow between roads
     */
    public void nextState() {
        FlowNetwork nextFlow = new FlowNetwork(evacFlow);
        for (int i = 0; i < joints.size(); i++) {
            double awarenessLevel = awareness(reverseIndex.get(i));
            update(i, nextFlow, awarenessLevel);
        }
        evacFlow = nextFlow;
    }
    
    /*
     * updates flow incident of a single intersection
     */
    public void update(int i, FlowNetwork f, double awarenessLevel) {
        double inFlow = 0;
        double totalInflow = 0;
        int outs = 0; // out edge count
        boolean isDead = false;
        boolean isEscaped = false;
        
        double tempdead = 0;
        double tempescaped = 0;

        // count outgoing edges
        for (FlowEdge to : f.outgoing(i)) {
            outs++;
        }

        if (detDist(reverseIndex.get(i)) <= hazardRadius)
            isDead = true;
        if (detDist(reverseIndex.get(i)) > hazardLimit())
            isEscaped = true;
        
        // sum inflow
        Iterator<FlowEdge> iter1 = evacFlow.incoming(i).iterator();
        Iterator<FlowEdge> iter2 = f.incoming(i).iterator();
        while (iter1.hasNext()) {
            FlowEdge oldEdge = iter1.next();
            FlowEdge newEdge = iter2.next();
                     
            // put excess flow back onto the incoming edges
            if (oldEdge.flow() <= oldEdge.capacity() || isDead) {
                inFlow += oldEdge.flow();
            }
            else {
                inFlow += oldEdge.capacity();
                newEdge.addFlow(oldEdge.flow() - oldEdge.capacity());;
            }
            totalInflow += oldEdge.flow();
        }
        
        if (isDead) {
            tempdead += totalInflow;
            dead += tempdead;
            alive -= tempdead;
            return;
        }

//        if (isEscaped) {
//            escaped += inFlow;
//            alive -= inFlow;
//            return;
//        }

        if (totalInflow == 0)
            return;
        
        double distr; //distribution
        double sum = 0;

        // find the "best" edge
        int counter = 0;
        double bestDist = 0;
        int bestIndex = -1;
        for (FlowEdge fe : f.outgoing(i)) {
            double dist = detDist(reverseIndex.get(fe.to()));
            if (dist > bestDist) {
                bestDist = dist;
                bestIndex = counter;
            }
            counter++;
        }

        // calculate the proportion of flow going to each outgoing edge
        double[] distribution = new double[outs];
        for (int j = 0; j < outs; j++) {
            distr = Math.random();

            // we use awareness to calculate the chance that drivers going to a
            // 'worse' edge will instead choose to go to the 'best' edge
            double smart = distr * awarenessLevel;
            double dumb = distr - smart;

            distribution[j] += dumb;
            distribution[bestIndex] += smart;
            sum += (smart + dumb);
        }

        // calculate how much flow goes to each edge out
        double[] outflow = new double[outs];
        double outflowSum = 0;
        for (int j = 0; j < outs; j++) {
            outflow[j] = Math.floor(inFlow * (distribution[j] / sum));
            outflowSum += outflow[j];
        }
        if ((outflowSum < inFlow) && (outs != 0))
            outflow[0] += (inFlow - outflowSum);
        else if (outs == 0) {
            // send flow back the way it came, if there are no outgoing paths
            Iterator<FlowEdge> itr1 = evacFlow.incoming(i).iterator();
            Iterator<FlowEdge> itr2 = f.incoming(i).iterator();
            while (itr2.hasNext()) {
                FlowEdge oldEdge = itr1.next();
                FlowEdge newEdge = itr2.next();
                if (oldEdge.flow() <= oldEdge.capacity())
                    newEdge.addFlow(oldEdge.flow());
                else
                    newEdge.addFlow(oldEdge.capacity());
            }
            return;
        }

        // model random traffic
        counter = 0;
        for (FlowEdge to : f.outgoing(i)) {
            to.addFlow(outflow[counter]);
            counter++;
        }
 
        // TODO - awareness
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
     
    public static void main(String[] args) {}
}
