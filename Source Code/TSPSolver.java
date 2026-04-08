
/**
 * A class to solve the Traveling Salesman Problem (TSP) for a given graph. Contains nearestNeighbor, 2-opt, and
 * heldKarp methods to solve the problem.
 *
 * @author Daniel Cater and Ryan Razzano
 * @version 4/7/2026
 */
public class TSPSolver {

    private double[][] distMatrix;

    public TSPSolver(double[][] distMatrix) {
        this.distMatrix = distMatrix;
    }

    public double routeLength(int[] route) {
        double total = 0;
        for (int i = 0; i < route.length - 1; i++) {
            total += distMatrix[route[i]][route[i + 1]];
        }
        total += distMatrix[route[route.length - 1]][route[0]]; // return to start
        return total;
    }

    public String routeToString(int[] route, String[] labels) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < route.length; i++) {
            sb.append(labels[route[i]]);
            if (i < route.length - 1) {
                sb.append(" -> ");
            }
        }
        sb.append(" -> " + labels[route[0]]); // return to start
        return sb.toString();
    }

    // TODO: Add methods to solve the TSP problem here, such as nearestNeighbor, 2-opt, and heldKarp.
    // a route is just an array of vertex indices, e.g. [0, 4, 2, 7, 1, ...]
    /**
     * Nearest Neighbor heuristic for TSP. Starts at the given index and always
     * moves to the closest unvisited city.
     *
     * @param start the index in the distance matrix to start from
     * @return an array of indices representing the route
     */
    public int[] nearestNeighbor(int start) {
        int n = distMatrix.length;
        boolean[] visited = new boolean[n];
        int[] route = new int[n];

        route[0] = start;
        visited[start] = true;

        for (int step = 1; step < n; step++) {
            int current = route[step - 1];
            double bestDist = Double.MAX_VALUE;
            int bestNext = -1;

            for (int j = 0; j < n; j++) {
                if (!visited[j] && distMatrix[current][j] < bestDist) {
                    bestDist = distMatrix[current][j];
                    bestNext = j;
                }
            }

            route[step] = bestNext;
            visited[bestNext] = true;
        }

        return route;
    }
}
