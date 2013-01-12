public class DrawExplosion {

    public static void main(String[] args) {
        Explosion exp = new Explosion(25.0); // 25 megaton explosion

        StdDraw.setXscale(-25, 25);
        StdDraw.setYscale(-25, 25);

        // loops infinitely
        for (int t = 0; ; t++) {
            StdDraw.clear();
            
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius(0.05);
            StdDraw.point(0, 0);

            StdDraw.setPenRadius(0.025);
            StdDraw.circle(0, 0, exp.getRadius(t));
            StdDraw.show(300);
        }
        
    }

}
