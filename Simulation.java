/* Runs the simulation of a city's traffic after the detonation
   of a bomb */
public class Simulation {
    private static final int MAX_ROUNDS = 500; // max simulation length

    // runs a simulation taking a roadmap file, an initial bomb energy (in megatons),
    //     and an initial population as command-line arguments
    //
    // Usage Example:
    // java Simulation roadmap.txt 25.0 1000
    public static void main(String[] args) {
        // read in a roadmap; default to unit_length test file
        String filename;
        if (args.length > 0)
            filename = args[0];
        else
            filename = "Routes_Test_Files/unit_length_roads.txt";

        // read in the bomb's initial energy in megatons; default to 25.0
        double kinetic;
        if (args.length > 1)
            kinetic = Double.parseDouble(args[1]);
        else
            kinetic = 10.0;

        // read in the initial population; default to 100000
        int initPop;
        if (args.length > 2)
            initPop = Integer.parseInt(args[2]);
        else
            initPop = 100;

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

            routes.nextState();

            // visualize results?
            routes.draw();
            StdDraw.show(300);

            // keep a killed/escaped tally?
            int alive = routes.getAlive();
            int escaped = routes.getEscaped();
            int dead = routes.getDead();
            int pop = routes.getPop();

            StdOut.println("round " + i);
            StdOut.println("Alive:    " + alive);
            StdOut.println("Dead:     " + dead);
            StdOut.println("Escaped:  " + escaped + '\n');

            // check other stop conditions?
            //   e.g., bomb radius exceeds max intersection distance,
            //         or all people are killed and/or escaped
            if (alive == 0)
                break;
        }
    }
}
