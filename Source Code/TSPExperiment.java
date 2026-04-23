
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * A class to run experiments on the TSP solver. This class will read in a graph
 * from a file, run the TSP solver on it, and print out the results. It will
 * also be used to compare the results of different TSP algorithms.
 *
 * @author Daniel Cater and Ryan Razzano
 * @version 4/7/2026
 */
public class TSPExperiment {

    // TODO: Add methods to run experiments on the TSP solver here, such as reading in a graph from a file,
    // running the TSP solver on it, and printing out the results.
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 1) {
            System.out.println("Usage: java TSPExperiment <graph_file>");
            return;
        }

        Scanner s = new Scanner(new File(args[0]));
        HighwayGraph g = new HighwayGraph(s);
        s.close();

        int[] subset = g.getAllVertexIndices();

        // build the distance matrix using all-pairs Dijkstra
        subset = g.getConnectedSubset(subset);
        g.buildDistanceMatrix(subset);

        // now pass g.distMatrix into your TSP algorithms
        TSPSolver solver = new TSPSolver(g.getDistMatrix());
        long startTime = System.currentTimeMillis();
        int[] route = solver.nearestNeighbor(0);
        long elapsedTimeNearestNeighbor = System.currentTimeMillis() - startTime;
        int[] improvedRoute = route.clone();
        startTime = System.currentTimeMillis();
        improvedRoute = solver.twoOpt(improvedRoute);
        long elapsedTimeTwoOpt = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        int[] heldKarpRoute = solver.heldKarp(0);
        long elapsedTimeHeldKarp = System.currentTimeMillis() - startTime;

        for (int i = 0; i < route.length; i++) {
            System.out.print(g.getVertexLabel(subset[route[i]]));
            if (i < route.length - 1) {
                System.out.print(" -> ");
            }
        }
        System.out.println(" -> " + g.getVertexLabel(subset[route[0]]));
        System.out.printf("Route length: %.2f miles%n", solver.routeLength(route));
        System.out.printf("Nearest Neighbor time: %.2f seconds%n", elapsedTimeNearestNeighbor / 1000.0);

        for (int i = 0; i < improvedRoute.length; i++) {
            System.out.print(g.getVertexLabel(subset[improvedRoute[i]]));
            if (i < improvedRoute.length - 1) {
                System.out.print(" -> ");
            }
        }
        System.out.println(" -> " + g.getVertexLabel(subset[improvedRoute[0]]));
        System.out.printf("Improved route length: %.2f miles%n", solver.routeLength(improvedRoute));
        System.out.printf("2-opt time: %.2f seconds%n", elapsedTimeTwoOpt / 1000.0);

        for (int i = 0; i < heldKarpRoute.length; i++) {
            System.out.print(g.getVertexLabel(subset[heldKarpRoute[i]]));
            if (i < heldKarpRoute.length - 1) {
                System.out.print(" -> ");
            }
        }
        System.out.println(" -> " + g.getVertexLabel(subset[heldKarpRoute[0]]));
        System.out.printf("Held-Karp route length: %.2f miles%n", solver.routeLength(heldKarpRoute));
        System.out.printf("Held-Karp time: %.2f seconds%n", elapsedTimeHeldKarp / 1000.0);
    }
}
