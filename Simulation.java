/* Runs the simulation of a city's traffic after the detonation
   of a bomb */
public class Simulation {
    private final int MAX_ROUNDS = 10000; // max simulation length

    // runs a simulation taking a roadmap file, an initial bomb energy (in megatons),
    //     and an initial population as command-line arguments
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
            kinetic = 25.0;

        // read in the initial population; default to 100000
        int pop;
        if (args.length > 2)
            pop = Integer.parseInt(args[2]);
        else
            pop = 100000;

        // set up a new road system/flow network
        Routes routes = new Routes(filename, pop);

        // create the explosion
        Explosion expl = new Explosion(kinetic); // 25 megaton bomb

        // run the simulation
        for (int i = 0; i < MAX_ROUNDS; i++) {
            // find the new hazard radius
            double hazardRadius = expl.getRadius(i);
            // update the hazard radius in Routes
            routes.setHazardRadius(hazardRadius);

            // as far as I know, this isn't actually a method yet
            routes.updateGraph();

            // visualize results?

            // keep a killed/escaped tally?

            // check other stop conditions?
            //   e.g., bomb radius exceeds max intersection distance,
            //         or all people are killed and/or escaped
        }
    }
}
