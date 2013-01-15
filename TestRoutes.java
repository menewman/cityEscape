public class TestRoutes {

    public static void main(String[] args) {
        Routes rts = new Routes("Routes_Test_Files/unit_length_roads.txt", Integer.parseInt(args[0]));
        for (int i = 0; i < 10; i++) {
        rts.draw();
        rts.nextState();
            StdDraw.show(100000);
        }
    }
}
