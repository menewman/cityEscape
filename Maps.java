public class Maps{
    public static void main(String[] args) {
        int d = Integer.parseInt(args[0]);
        StdOut.println("69 0 0 " + d);
        
        
        for (int j = -d; j < d + 1; j++) {
                    for (int i = -d; i < d; i++) {
                        StdOut.println("true" + " " + (i) + " " + j + " " + (1 + i) + " " + j + " " + "1" + " " + "1" + " " + "15");
                        StdOut.println("true" + " " + (i+1) + " " + j + " " + (i) + " " + j + " " + "1" + " " + "1" + " " + "15");
                    }
        }
        
        for (int i = -d; i < d + 1; i++) {
            for (int j = -d; j < d; j++) {
                        StdOut.println("false" + " " + (i) + " " + (j) + " " + (i) + " " + (j+1) + " " + "1" + " " + "1" + " " + "15");
                        StdOut.println("false" + " " + (i) + " " + (j+1) + " " + (i) + " " + (j) + " " + "1" + " " + "1" + " " + "15");
            }
        }

        
//    int N = Integer.parseInt(args[0]);
//    boolean orientation = true;
//    double dimension = 20;
//    double fromx = 0;
//    double fromy = 0;
//    double tox = 0;
//    double toy = 0; 
//    double w = 1;
//    double h = 1;
//    double cap = 15;
//    double lr;
//    double ud;
//    
//    for (int i = 0; i < N; i++) {
//        lr = Math.random();
//        ud = Math.random();
//        
//        if (lr >= 0.5 && orientation) {
//            
//            if (ud >= 0.5) {
//            tox = fromx;
//            toy += 1;
//            }
//            else {
//                tox = fromx;
//                toy -= 1;
//            }
//            orientation = !orientation;
//        }
//        else if (lr < 0.5 && orientation) {
//            
//            if (ud >= 0.5) {
//            fromx = tox;
//            toy += 1;
//            }
//            else {
//                fromx = tox;
//                toy -= 1;
//            }
//            orientation = !orientation;
//        }
//        else if (lr < 0.5 && !orientation) {
//            
//            if (ud >= 0.5) {
//            tox -= 1;
//            toy = fromy;
//            }
//            else {
//                tox += 1;
//                toy = fromy;
//            }
//            orientation = !orientation;
//        }
//        else if (lr >= 0.5 && orientation) {
//            
//            if (ud >= 0.5) {
//            tox -= 1;
//            toy = fromy;
//            }
//            else {
//                tox += 1;
//                toy = fromy;
//            }
//            orientation = !orientation;
//        }
//        
//        
//        StdOut.println(!orientation + " " + fromx + " " + fromy + " " + tox + " " + toy + " " + w + " " + h + " " + cap);
        
    }
    
    
    
    
    }
