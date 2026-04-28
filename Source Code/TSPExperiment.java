
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

/**
 * A class to run experiments on the TSP solver. This class will read in a graph
 * from a file, run the TSP solver on it, and print out the results. It will
 * also be used to compare the results of different TSP algorithms.
 *
 * @author Daniel Cater and Ryan Razzano
 * @version 4/23/2026
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
     * @param route The order of vertices in the TSP route (indices into
     * subset).
     * @param subset The actual vertex indices in the graph corresponding to the
     * route.
     * @param g The highway graph containing vertex and edge information.
     * @param filename The name of the output file to write the path to.
     */
    public static void printPath(int[] route, int[] subset, HighwayGraph g, String filename) throws IOException {
        PrintWriter pw = new PrintWriter(filename);
        boolean first = true;

        for (int i = 0; i < route.length; i++) {
            int fromSubsetIdx = route[i];                           // index into subset
            int toSubsetIdx = route[(i + 1) % route.length];        // index into subset

            List<Integer> path = g.getPath(fromSubsetIdx, toSubsetIdx, subset);

            for (int k = 0; k < path.size() - 1; k++) {
                int v = path.get(k);
                int vNext = path.get(k + 1);
                String edgeLabel = g.getEdgeLabel(v, vNext);

                if (first) {
                    pw.println("START " + g.getVertexLabel(v)
                            + " (" + g.getVertexLat(v) + "," + g.getVertexLng(v) + ")");
                    first = false;
                } else {
                    pw.println(edgeLabel + " " + g.getVertexLabel(v)
                            + " (" + g.getVertexLat(v) + "," + g.getVertexLng(v) + ")");
                }
            }
        }

        // close the loop back to start
        int lastVertex = subset[route[route.length - 1]];
        int firstVertex = subset[route[0]];
        pw.println(g.getEdgeLabel(lastVertex, firstVertex) + " " + g.getVertexLabel(firstVertex)
                + " (" + g.getVertexLat(firstVertex) + "," + g.getVertexLng(firstVertex) + ")");

        pw.close();
    }

    public static void main(String[] args) throws Exception {
        String tmgFile = args[0];
        int maxSubset = 30;
        int hkMax = 15;
        String nnOut = null, twoOptOut = null, hkOut = null, csvOut = null;

        // parse flags
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--max-subset":
                    maxSubset = Integer.parseInt(args[++i]);
                    break;
                case "--hk-max":
                    hkMax = Integer.parseInt(args[++i]);
                    break;
                case "--nn-out":
                    nnOut = args[++i];
                    break;
                case "--twoopt-out":
                    twoOptOut = args[++i];
                    break;
                case "--hk-out":
                    hkOut = args[++i];
                    break;
                case "--csv-out":
                    csvOut = args[++i];
                    break;
            }
        }

        Scanner s = new Scanner(new File(tmgFile));
        HighwayGraph g = new HighwayGraph(s);
        s.close();

        // try common highway labels in order until one gives a usable subset
        String[] highwayLabels = {"US", "I-", "NY", "SR", "ST", "RT"};
        int[] subset = new int[0];

        for (String label : highwayLabels) {
            int[] candidate = g.getConnectedSubset(g.getSubsetByHighway(label, maxSubset));
            if (candidate.length >= 4) {
                subset = candidate;
                break;
            }
        }

        // last resort: just use the first maxSubset vertices
        if (subset.length < 4) {
            subset = new int[Math.min(maxSubset, g.getVertexCount())];
            for (int i = 0; i < subset.length; i++) {
                subset[i] = i;
            }
            subset = g.getConnectedSubset(subset);
        }

        // run NN and 2-opt on full subset first
        g.buildDistanceMatrix(subset);
        TSPSolver solver = new TSPSolver(g.getDistMatrix());

        long t1 = System.currentTimeMillis();
        int[] nnRoute = solver.nearestNeighbor(0);
        long nnTime = System.currentTimeMillis() - t1;

        long t2 = t1;
        int[] twoOptRoute = solver.twoOpt(nnRoute.clone());
        long twoOptTime = System.currentTimeMillis() - t2;

// write NN and 2-opt paths BEFORE rebuilding for Held-Karp
        printPath(nnRoute, subset, g, nnOut);
        printPath(twoOptRoute, subset, g, twoOptOut);

// now rebuild for Held-Karp with trimmed subset
        int[] hkSubset = subset;
        int[] hkRoute = null;
        long hkTime = -1;

        if (subset.length > hkMax) {
            hkSubset = new int[hkMax];
            System.arraycopy(subset, 0, hkSubset, 0, hkMax);
            g.buildDistanceMatrix(hkSubset); // rebuilds predMatrix for hkSubset
        }

        TSPSolver hkSolver = new TSPSolver(g.getDistMatrix());
        long t3 = System.currentTimeMillis();
        hkRoute = hkSolver.heldKarp(0);
        hkTime = System.currentTimeMillis() - t3;

        if (hkRoute != null) {
            printPath(hkRoute, hkSubset, g, hkOut);
        }

        // write CSV
        if (csvOut != null) {
            PrintWriter pw = new PrintWriter(csvOut);
            pw.println("file,subset_size,nn_distance,nn_time_ms,twoopt_distance,twoopt_time_ms,heldkarp_distance,heldkarp_time_ms");
            pw.printf("%s,%d,%.3f,%d,%.3f,%d,%.3f,%d%n",
                    tmgFile, subset.length,
                    solver.routeLength(nnRoute), nnTime,
                    solver.routeLength(twoOptRoute), twoOptTime,
                    hkRoute != null ? solver.routeLength(hkRoute) : -1, hkTime);
            pw.close();
        }
    }
}
