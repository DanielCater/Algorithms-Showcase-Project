
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

    public int[] twoOpt(int[] route) {
        int n = route.length;
        boolean improved = true;

        while (improved) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) { // ensure we don't break the route
                    double currentDist = distMatrix[route[i]][route[i + 1]] + distMatrix[route[j]][route[(j + 1) % n]];
                    double newDist = distMatrix[route[i]][route[j]] + distMatrix[route[i + 1]][route[(j + 1) % n]];

                    if (newDist < currentDist) {
                        // Perform the 2-opt swap
                        reverse(route, i + 1, j);
                        improved = true;
                    }
                }
            }
        }

        return route;
    }

    private void reverse(int[] route, int i, int j) {
        while (i < j) {
            int temp = route[i];
            route[i] = route[j];
            route[j] = temp;
            i++;
            j--;
        }
    }

    public int[] heldKarp(int start) {
        int n = distMatrix.length;
        int[][] dp = new int[1 << n][n];
        for (int i = 0; i < (1 << n); i++) {
            for (int j = 0; j < n; j++) {
                dp[i][j] = Integer.MAX_VALUE / 2; // avoid overflow
            }
        }
        dp[1 << start][start] = 0;

        for (int mask = 0; mask < (1 << n); mask++) {
            for (int u = 0; u < n; u++) {
                if ((mask & (1 << u)) != 0) { // if u is in the subset
                    for (int v = 0; v < n; v++) {
                        if ((mask & (1 << v)) == 0) { // if v is not in the subset
                            dp[mask | (1 << v)][v] = Math.min(dp[mask | (1 << v)][v], dp[mask][u] + (int) distMatrix[u][v]);
                        }
                    }
                }
            }
        }

        int minCost = Integer.MAX_VALUE;
        int lastCity = -1;
        for (int j = 0; j < n; j++) {
            if (j != start) {
                int cost = dp[(1 << n) - 1][j] + (int) distMatrix[j][start];
                if (cost < minCost) {
                    minCost = cost;
                    lastCity = j;
                }
            }
        }

        // Reconstruct the path
        int[] route = new int[n];
        boolean[] visited = new boolean[n];
        route[n - 1] = start;
        visited[start] = true;

        for (int i = n - 2, mask = (1 << n) - 1; i >= 0; i--) {
            route[i] = lastCity;
            visited[lastCity] = true;
            mask ^= (1 << lastCity);

            int nextCity = -1;
            for (int j = 0; j < n; j++) {
                if (!visited[j]) {
                    int cost = dp[mask][j] + (int) distMatrix[j][lastCity];
                    if (cost == dp[mask | (1 << lastCity)][lastCity]) {
                        nextCity = j;
                        break;
                    }
                }
            }
            lastCity = nextCity;
        }

        return route;
    }

}
