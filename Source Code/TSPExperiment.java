
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

    /**
     * Prints the route in the specified format. The first line uses "START" and
     * the vertex label, followed by the latitude and longitude. Each subsequent
     * line uses the edge label from the previous vertex to the current vertex,
     * followed by the current vertex label and its latitude and longitude.
     * Finally, it returns to the starting vertex with the appropriate edge
     * label.
     *
     * @param route the array of vertex indices representing the route
     * @param subset the subset of vertex indices used in the TSP solver, which
     * maps the route indices to the actual vertex indices in the graph
     * @param g the HighwayGraph object containing the vertex and edge
     * information
     */
    public static void printPath(int[] route, int[] subset, HighwayGraph g) {
        // first line uses START
        int firstVertex = subset[route[0]];
        System.out.println("START " + g.getVertexLabel(firstVertex)
                + " (" + g.getVertexLat(firstVertex) + "," + g.getVertexLng(firstVertex) + ")");

        // remaining lines use the edge label from previous to current
        for (int i = 1; i < route.length; i++) {
            int prevVertex = subset[route[i - 1]];
            int currVertex = subset[route[i]];
            String edgeLabel = g.getEdgeLabel(prevVertex, currVertex);
            System.out.println(edgeLabel + " " + g.getVertexLabel(currVertex)
                    + " (" + g.getVertexLat(currVertex) + "," + g.getVertexLng(currVertex) + ")");
        }

        // return to start
        int lastVertex = subset[route[route.length - 1]];
        String edgeLabel = g.getEdgeLabel(lastVertex, firstVertex);
        System.out.println(edgeLabel + " " + g.getVertexLabel(firstVertex)
                + " (" + g.getVertexLat(firstVertex) + "," + g.getVertexLng(firstVertex) + ")");
    }

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
        System.out.printf("Nearest Neighbor time: %.6f seconds%n", elapsedTimeNearestNeighbor / 1000.0);
        for (int i = 0; i < improvedRoute.length; i++) {
            System.out.print(g.getVertexLabel(subset[improvedRoute[i]]));
            if (i < improvedRoute.length - 1) {
                System.out.print(" -> ");
            }
        }
        System.out.println(" -> " + g.getVertexLabel(subset[improvedRoute[0]]));
        System.out.printf("Improved route length: %.2f miles%n", solver.routeLength(improvedRoute));
        System.out.printf("2-opt time: %.6f seconds%n", elapsedTimeTwoOpt / 1000.0);

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
