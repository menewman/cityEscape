/* Runs the simulation of a city's traffic after the detonation
   of a bomb */
public class Simulation {
    private static final int MAX_ROUNDS = 100; // max simulation length

    // runs a simulation taking a roadmap file, an initial bomb energy (in megatons),
    //     and an initial population as command-line arguments
    //
    // Usage Example:
    // java Simulation initPop roadmap.txt 25.0 megatons
    public static void main(String[] args) {
        int initPop;
        if (args.length > 0)
            initPop = Integer.parseInt(args[0]);
        else
            initPop = 200;
        
        // read in a roadmap; default to unit_length test file
        String filename;
        if (args.length > 1)
            filename = args[1];
        else
            filename = "Routes_Test_Files/unit_length_roads.txt";

        // read in the bomb's initial energy in megatons; default to 25.0
        double kinetic;
        if (args.length > 2)
            kinetic = Double.parseDouble(args[2]);
        else
            kinetic = 10.0;

        // set up a new road system/flow network
        Routes routes = new Routes(filename, initPop);

        // create the explosion
        Explosion expl = new Explosion(kinetic); // 25 megaton bomb

        // run the simulation
        for (int i = 0; i < MAX_ROUNDS; i++) {
            // find the new hazard radius
            double hazardRadius = expl.getRadius(i);
            // update the hazard radius in Routes
            routes.setHazardRadius(hazardRadius);
            //StdOut.println(routes.calculateLiveFlow()); // DEBUG
            routes.nextState();

            // visualize results?
            routes.draw();
            StdDraw.show(300);

            // keep a killed/escaped tally?
            double remainingFlow = routes.calculateLiveFlow();
            double alive = routes.getAlive();
            double escaped = routes.getEscaped();
            double dead = routes.getDead();
            double pop = routes.getPop();
            double total = alive+escaped+dead;

            StdOut.println("round " + i);
            StdOut.println("Live Flow: " + remainingFlow);
            StdOut.println("Alive:     " + alive);
            StdOut.println("Dead:      " + dead);
            StdOut.println("Escaped:   " + escaped);
            StdOut.println("Total:     " + total + '\n');
            // check other stop conditions?
            //   e.g., bomb radius exceeds max intersection distance,
            //         or all people are killed and/or escaped
            if (remainingFlow <= 0) {
                StdOut.println("\nSIMULATION COMPLETE");
                /*StdOut.println("Alive:   " + alive);
                StdOut.println("Dead:    " + dead);
                StdOut.println("Escaped: " + escaped + '\n');
                StdOut.println("Total:   " + escaped + '\n');*/
                break;
            }
        }
    }
}
