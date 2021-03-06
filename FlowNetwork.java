/*************************************************************************
 *  Compilation:  javac FlowNetwork.java
 *  Execution:    java FlowNetwork V E
 *  Dependencies: Bag.java FlowEdge.java
 *
 *  A capacitated flow network, implemented using adjacency lists.
 *
 *************************************************************************/

public class FlowNetwork {
    private final int V;
    private int E;
    private Bag<FlowEdge>[] adj;
    
    // empty graph with V vertices
    public FlowNetwork(int V) {
        this.V = V;
        this.E = 0;
        adj = (Bag<FlowEdge>[]) new Bag[V];
        for (int v = 0; v < V; v++)
            adj[v] = new Bag<FlowEdge>();
    }
    
    // graph with the same vertices, edges, and capacities as input graph,
    // but flow of zero on all edges
    public FlowNetwork(FlowNetwork fnet) {
        this(fnet.V);
        //for (Bag<FlowEdge> b : fnet.adj) {
        for (int i = 0; i < V; i++) {
            //Bag<FlowEdge> b = fnet.adj[i];
            for (FlowEdge fe : fnet.incoming(i)) {
                int v = fe.from();
                int w = fe.to();
                double capacity = fe.capacity();
                addEdge(new FlowEdge(v, w, capacity));
            }
        }
    }    

    // random graph with V vertices and E edges
    public FlowNetwork(int V, int E) {
        this(V);
        for (int i = 0; i < E; i++) {
            int v = StdRandom.uniform(V);
            int w = StdRandom.uniform(V);
            double capacity = StdRandom.uniform(100);
            addEdge(new FlowEdge(v, w, capacity));
        }
    }

    // graph, read from input stream
    public FlowNetwork(In in) {
        this(in.readInt());
        int E = in.readInt();
        for (int i = 0; i < E; i++) {
            int v = in.readInt();
            int w = in.readInt();
            double capacity = in.readDouble();
            addEdge(new FlowEdge(v, w, capacity));
        }
    }

    // number of vertices and edges
    public int V() { return V; }
    public int E() { return E; }

    // add edge e in both v's and w's adjacency lists
    public void addEdge(FlowEdge e) {
        E++;
        int v = e.from();
        int w = e.to();
        adj[v].add(e);
        adj[w].add(e);
    }

    // return list of edges incident to v
    public Iterable<FlowEdge> adj(int v) {
        return adj[v];
    }

    // return list of edges incoming to v
    public Iterable<FlowEdge> incoming(int v) {
        Bag<FlowEdge> in = new Bag<FlowEdge>();
        for (FlowEdge fe : adj[v]) {
            if (fe.to() == v)
                in.add(fe);
        }
        return in;
    }

    // return list of edges outgoing from v
    public Iterable<FlowEdge> outgoing(int v) {
        Bag<FlowEdge> out = new Bag<FlowEdge>();
        for (FlowEdge fe : adj[v]) {
            if (fe.from() == v)
                out.add(fe);
        }
        return out;
    }

    // return list of all edges - excludes self loops
    public Iterable<FlowEdge> edges() {
        Bag<FlowEdge> list = new Bag<FlowEdge>();
        for (int v = 0; v < V; v++)
            for (FlowEdge e : adj(v)) {
                if (e.to() != v)
                    list.add(e);
            }
        return list;
    }


    // string representation of Graph (excludes self loops) - takes quadratic time
    public String toString() {
        String NEWLINE = System.getProperty("line.separator");
        StringBuilder s = new StringBuilder();
        s.append(V + " " + E + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ":  ");
            for (FlowEdge e : adj[v]) {
                if (e.to() != v) s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    // test client
    public static void main(String[] args) {
        In in = new In(args[0]);
        FlowNetwork G = new FlowNetwork(in);
        StdOut.println(G);
    }

}
