public class TestRoutes {

    public static void main(String[] args) {
        Routes rts = new Routes("Routes_Test_Files/unit_length_roads.txt", 10);

        rts.draw();
        rts.populate(10); // is this even meant to be a public method?
        rts.draw();
    }
}
