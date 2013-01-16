public class TestRoutes {

    public static void main(String[] args) {
        In in = new In();
        
        Routes rts = new Routes("test5lines.txt", Integer.parseInt(args[0]));
        StdOut.println(rts.getEvacFlow().V());
        int entered = 1;
        StdDraw.setScale(-1.0*rts.getScale(), rts.getScale());
        while (entered == 1) {
        
            
                for (int i = 0; i < 1; i++) {
                    
                    rts.draw();
                    rts.nextState();
        
//            StdDraw.show(100000);
                    entered = in.readInt();
                }
            }
        }
    }

