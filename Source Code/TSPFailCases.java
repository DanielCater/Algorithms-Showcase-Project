public class TSPFailCases {

    public static void main(String[] args) {

        runTest("Bridge Trap", getBridgeFailCase());
        runTest("Chain Trap", getChainFailCase());
        runTest("Direction Trap", getDirectionFailCase());
    }

    private static void runTest(String name, double[][] dist) {
        System.out.println("\n=== " + name + " ===");

        TSPSolver solver = new TSPSolver(dist);

        int[] nn = solver.nearestNeighbor(0);
        int[] twoOpt = solver.twoOpt(nn.clone());
        int[] optimal = solver.heldKarp(0);

        printRoute("Nearest Neighbor", nn, solver);
        printRoute("2-opt", twoOpt, solver);
        printRoute("Held-Karp", optimal, solver);
    }

    private static void printRoute(String label, int[] route, TSPSolver solver) {
        System.out.print(label + ": ");
        for (int i = 0; i < route.length; i++) {
            System.out.print(route[i]);
            if (i < route.length - 1) System.out.print(" -> ");
        }
        System.out.println(" -> " + route[0]);
        System.out.printf("Length: %.2f%n", solver.routeLength(route));
    }

    private static double[][] getBridgeFailCase() {
        return new double[][] {
            {0,2,2,50,50,50,3},
            {2,0,2,50,50,50,3},
            {2,2,0,50,50,50,3},
            {50,50,50,0,2,2,3},
            {50,50,50,2,0,2,3},
            {50,50,50,2,2,0,3},
            {3,3,3,3,3,3,0}
        };
    }

    private static double[][] getChainFailCase() {
        return new double[][] {
            {0,2,9,10,10,10,10},
            {2,0,2,10,10,10,10},
            {9,2,0,2,10,10,10},
            {10,10,2,0,2,10,10},
            {10,10,10,2,0,2,10},
            {10,10,10,10,2,0,2},
            {10,10,10,10,10,2,0}
        };
    }

    private static double[][] getDirectionFailCase() {
        return new double[][] {
            {0,2,9,9,9,9,3},
            {2,0,2,9,9,9,9},
            {9,2,0,2,9,9,9},
            {9,9,2,0,2,9,9},
            {9,9,9,2,0,2,9},
            {9,9,9,9,2,0,2},
            {3,9,9,9,9,2,0}
        };
    }
}
