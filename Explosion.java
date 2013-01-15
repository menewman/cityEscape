 import java.lang.Math;

/* finds the radius of a gas explosion at a particular time,
 * using the Sedov Taylor equations */

// NOTE -- K is currently arbitrarily set; it needs to be
// calibrated to match the scale of the roadmap

public class Explosion {
    private static final double K = 0.0001; // shock temperature
    private static final double P = 1.225; // air density in kg/m^3
    private double E; // initial energy of the bomb

    // creates a new Explosion object with given initial energy e, in
    // megatons
    public Explosion (double e) {
        this.E = e*4.184E15; // convert to joules
    }

    // calculate the radius at some time t
    public double getRadius (int t) {
        return (K * Math.pow(E/P, 0.2) * Math.pow(t, 0.4));
    }

    // creates an Explosion object for testing, outputs blast radius
    //     over a specified number of timesteps
    public static void main(String[] args) {
        Explosion expl = new Explosion(25); // 25 megaton bomb
        int rounds = 100;

        if (args.length > 0)
            rounds = Integer.parseInt(args[0]);

        for (int i = 0; i < rounds; i++) {
            double hazardRadius = expl.getRadius(i);
            System.out.println(i + " " + hazardRadius);
        }
    }
}
