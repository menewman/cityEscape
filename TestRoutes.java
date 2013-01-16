public class TestRoutes {

    public static void main(String[] args) {
        Routes rts = new Routes("unit_length_roads.txt", Integer.parseInt(args[0]));
        for (int i = 0; i < 20; i++) {
        rts.draw();

        rts.nextState();
        
//            StdDraw.show(100000);
        }
    }
}
