
/**
 * An undirected, adjacency-list based graph data structure developed
 * specifically for METAL highway mapping graphs.
 *
 * Adapted from the working with METAL data lab
 *
 * @author Daniel Cater and Ryan Razzano
 * @version 4/7/2026
 */
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.PriorityQueue;
import java.util.Scanner;

public class HighwayGraph {

    private static final DecimalFormat df = new DecimalFormat("#.###");

    /**
     * a simple class to represent a latitude/longitude pair, and to compute
     * distances between them. Note that the distance computation is not exact,
     * but is good enough for our purposes
     */
    private class LatLng {

        private double lat, lng;

        public LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        /**
         * compute the distance in miles from this LatLng to another
         *
         * @param other another LatLng
         * @return the distance in miles from this LatLng to other
         */
        public double distanceTo(LatLng other) {
            /**
             * radius of the Earth in statute miles
             */
            final double EARTH_RADIUS = 3963.1;

            // did we get the same point?
            if (equals(other)) {
                return 0.0;
            }

            // coordinates in radians
            double rlat1 = Math.toRadians(lat);
            double rlng1 = Math.toRadians(lng);
            double rlat2 = Math.toRadians(other.lat);
            double rlng2 = Math.toRadians(other.lng);

            return Math.acos(Math.cos(rlat1) * Math.cos(rlng1) * Math.cos(rlat2) * Math.cos(rlng2)
                    + Math.cos(rlat1) * Math.sin(rlng1) * Math.cos(rlat2) * Math.sin(rlng2)
                    + Math.sin(rlat1) * Math.sin(rlat2)) * EARTH_RADIUS;
        }

        /**
         * Compare another LatLng with this for equality, subject to the
         * specified tolerance.
         *
         * @param o the other LatLng
         * @return whether the two lat/lng pairs should be considered equal
         */
        public boolean equals(Object o) {
            final double TOLERANCE = 0.00001;
            LatLng other = (LatLng) o;

            return ((Math.abs(other.lat - lat) < TOLERANCE)
                    && (Math.abs(other.lng - lng) < TOLERANCE));
        }

        public String toString() {
            return "(" + lat + "," + lng + ")";
        }
    }

    /**
     * our private internal structure for a Vertex, which includes the vertex's
     * label, its lat/lng point, and the head of its linked list of edges
     *
     */
    private class Vertex {

        private String label;
        private LatLng point;
        private Edge head;

        public Vertex(String l, double lat, double lng) {
            label = l;
            point = new LatLng(lat, lng);
        }

    }

    // our private internal structure for an Edge
    private class Edge {

        // the edge needs to know its own label, its destination vertex (note that
        // it knows its source as which vertex's list contains this edge), an
        // optional array of points that improve the edge's shape, and its length
        // in miles, which is computed on construction
        private String label;
        private int dest;
        private LatLng[] shapePoints;
        private double length;

        // and Edge is also a linked list
        private Edge next;

        /**
         * construct an Edge with the given label, destination vertex, shape
         * points, and next pointer. The length of the edge is computed based on
         * the shape points and the start and end vertices.
         *
         * @param l the label of the edge
         * @param dst the destination vertex number of the edge
         * @param startPoint the starting point of the edge (the point of the
         * source vertex)
         * @param points an array of shape points for the edge (may be null if
         * there are no shape points)
         * @param endPoint the ending point of the edge (the point of the
         * destination vertex)
         * @param n the next edge in the linked list of edges for the source
         * vertex
         */
        public Edge(String l, int dst, LatLng startPoint, LatLng points[], LatLng endPoint, Edge n) {
            label = l;
            dest = dst;
            shapePoints = points;
            next = n;
            length = 0.0;
            LatLng prevPoint = startPoint;
            if (points != null) {
                for (int pointNum = 0; pointNum < points.length; pointNum++) {
                    length += prevPoint.distanceTo(points[pointNum]);
                    prevPoint = points[pointNum];
                }
            }
            length += prevPoint.distanceTo(endPoint);
        }
    }

    // vertices -- we know how many at the start, so these 
    // are simply in an array
    private Vertex[] vertices;

    // number of edges
    private int numEdges;

    /**
     * construct a HighwayGraph from a Scanner reading from a properly formatted
     * .tmg file. The format of the file is as follows: - a header line (which
     * we ignore for now) - a line with two integers, the number of vertices and
     * the number of edges - one line per vertex, with the format "label lat
     * lng" - one line per edge, with the format "v1 v2 label shapePoints",
     * where v1 and v2 are the integer vertex numbers of the endpoints of the
     * edge, label is the name of the edge, and shapePoints is a space-separated
     * list of lat/lng pairs that give the shape of the edge (this may be empty,
     * in which case there are no shape points for this edge)
     *
     * @param s a Scanner reading from a properly formatted .tmg file
     */
    public HighwayGraph(Scanner s) {

        // read header line -- for now assume it's OK, but should
        // check
        s.nextLine();

        // read number of vertices and edges
        int numVertices = s.nextInt();
        numEdges = s.nextInt();

        // construct our array of Vertices
        vertices = new Vertex[numVertices];

        // next numVertices lines are Vertex entries
        for (int vNum = 0; vNum < numVertices; vNum++) {
            vertices[vNum] = new Vertex(s.next(), s.nextDouble(), s.nextDouble());
        }

        // next numEdge lines are Edge entries
        for (int eNum = 0; eNum < numEdges; eNum++) {
            int v1 = s.nextInt();
            int v2 = s.nextInt();
            String label = s.next();
            // shape points take us to the end of the line, and this
            // will be just a new line char if there are none for this edge
            String shapePointText = s.nextLine().trim();
            String[] shapePointStrings = shapePointText.split(" ");
            LatLng v1Tov2[] = null;
            LatLng v2Tov1[] = null;
            if (shapePointStrings.length > 1) {
                // build arrays in both orders
                v1Tov2 = new LatLng[shapePointStrings.length / 2];
                v2Tov1 = new LatLng[shapePointStrings.length / 2];
                for (int pointNum = 0; pointNum < shapePointStrings.length / 2; pointNum++) {
                    LatLng point = new LatLng(Double.parseDouble(shapePointStrings[pointNum * 2]),
                            Double.parseDouble(shapePointStrings[pointNum * 2 + 1]));
                    v1Tov2[pointNum] = point;
                    v2Tov1[shapePointStrings.length / 2 - pointNum - 1] = point;
                }
            }

            // build our Edge structures and add to each adjacency list
            vertices[v1].head = new Edge(label, v2, vertices[v1].point, v1Tov2, vertices[v2].point, vertices[v1].head);
            vertices[v2].head = new Edge(label, v1, vertices[v2].point, v2Tov1, vertices[v1].point, vertices[v2].head);
        }
    }

    private double[][] distMatrix;

    public double[][] getDistMatrix() {
        return distMatrix;
    }

    public String getVertexLabel(int vertexIndex) {
        return vertices[vertexIndex].label;
    }

    public String getEdgeLabel(int vertexIndex, int edgeIndex) {
        Edge e = vertices[vertexIndex].head;
        for (int i = 0; i < edgeIndex; i++) {
            if (e == null) {
                throw new IllegalArgumentException("Invalid edge index");
            }
            e = e.next;
        }
        if (e == null) {
            throw new IllegalArgumentException("Invalid edge index");
        }
        return e.label;
    }

    public int[] getAllVertexIndices() {
        int[] all = new int[vertices.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = i;
        }
        return all;
    }

    // helper class for Dijkstra's priority queue
    private class Node implements Comparable<Node> {

        int vertex;
        double dist;

        public Node(int vertex, double dist) {
            this.vertex = vertex;
            this.dist = dist;
        }

        public int compareTo(Node other) {
            return Double.compare(this.dist, other.dist);
        }
    }

    /**
     * Run Dijkstra's from a single source vertex across the full graph. Returns
     * an array of shortest distances from src to every vertex.
     *
     * @param src the index of the source vertex
     * @return double[] where result[i] is the shortest distance from src to i
     */
    public double[] dijkstra(int src) {
        double[] dist = new double[vertices.length];
        boolean[] visited = new boolean[vertices.length];

        // initialize all distances to infinity
        for (int i = 0; i < dist.length; i++) {
            dist[i] = Double.MAX_VALUE;
        }
        dist[src] = 0.0;

        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(src, 0.0));

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            int u = curr.vertex;

            if (visited[u]) {
                continue;
            }
            visited[u] = true;

            // traverse adjacency list
            Edge e = vertices[u].head;
            while (e != null) {
                int v = e.dest;
                double newDist = dist[u] + e.length;
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    pq.add(new Node(v, newDist));
                }
                e = e.next;
            }
        }
        return dist;
    }

    /**
     * Build a distance matrix for a subset of vertices using all-pairs
     * Dijkstra. distMatrix[i][j] gives the shortest road distance between
     * subset[i] and subset[j].
     *
     * @param subset array of vertex indices to include
     */
    public void buildDistanceMatrix(int[] subset) {
        int n = subset.length;
        distMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            // run Dijkstra from subset[i] across the whole graph
            double[] fullDist = dijkstra(subset[i]);

            // extract only the distances to other subset vertices
            for (int j = 0; j < n; j++) {
                distMatrix[i][j] = fullDist[subset[j]];
            }
        }
    }

    /**
     * Helper method to get a subset of vertices that are reachable from a given
     * vertex. This is useful for filtering out disconnected vertices before
     * running TSP algorithms, since disconnected vertices will just cause
     * problems.
     *
     * @param subset an array of vertex indices to filter
     * @return an array of vertex indices from the input subset that are
     * reachable from the first vertex in the subset
     */
    public int[] getConnectedSubset(int[] subset) {
        // run Dijkstra from the first vertex
        double[] dist = dijkstra(subset[0]);

        // count reachable vertices
        int count = 0;
        for (int i = 0; i < subset.length; i++) {
            if (dist[subset[i]] < Double.MAX_VALUE) {
                count++;
            }
        }

        // build filtered subset
        int[] connected = new int[count];
        int idx = 0;
        for (int i = 0; i < subset.length; i++) {
            if (dist[subset[i]] < Double.MAX_VALUE) {
                connected[idx++] = i;
            }
        }
        return connected;
    }

    /**
     * Helper method to get a subset of vertices that are on a particular
     * highway.
     *
     * @param highway the name of the highway to filter by (e.g. "I-80")
     * @return an array of vertex indices for vertices whose labels contain the
     * given highway name
     */
    public int[] getSubsetByHighway(String highway) {
        // first pass: count matches
        int count = 0;
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].label.contains(highway)) {
                count++;
            }
        }

        // second pass: fill array
        int[] subset = new int[count];
        int idx = 0;
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].label.contains(highway)) {
                subset[idx++] = i;
            }
        }
        return subset;
    }

    // find a vertex whose label most closely matches a target
    public int findVertex(String labelFragment) {
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].label.contains(labelFragment)) {
                return i;
            }
        }
        return -1; // not found
    }

    /**
     * toString method to print out the graph in a readable format. This is not
     * required, but is useful for debugging and understanding the graph
     * structure.
     *
     * @return a string representation of the graph
     */
    public String toString() {

        StringBuilder s = new StringBuilder();
        s.append("|V|=" + vertices.length + ", |E|=" + numEdges + "\n");
        for (Vertex v : vertices) {
            s.append(v.label + " " + v.point + "\n");
            Edge e = v.head;
            while (e != null) {
                Vertex o = vertices[e.dest];
                s.append("  to " + o.label + " " + o.point + " on " + e.label);
                if (e.shapePoints != null) {
                    s.append(" via");
                    for (int pointNum = 0; pointNum < e.shapePoints.length; pointNum++) {
                        s.append(" " + e.shapePoints[pointNum]);
                    }
                }
                s.append(" length " + df.format(e.length) + "\n");
                e = e.next;
            }
        }

        return s.toString();
    }

    public static void main(String args[]) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java HighwayGraph tmgfile");
            System.exit(1);
        }

        // read in the file to construct the graph
        Scanner s = new Scanner(new File(args[0]));
        HighwayGraph g = new HighwayGraph(s);
        s.close();

        // print summary of the graph
        System.out.println(g);

        // ADD CODE HERE TO COMPLETE LAB TASKS
        // Search for extreme vertices and longest/shortest labels
        Vertex north = g.vertices[0], south = g.vertices[0], east = g.vertices[0], west = g.vertices[0];
        Vertex longest = g.vertices[0], shortest = g.vertices[0];

        for (Vertex v : g.vertices) {
            if (v.point.lat > north.point.lat) {
                north = v;
            }
            if (v.point.lat < south.point.lat) {
                south = v;
            }
            if (v.point.lng > east.point.lng) {
                east = v;
            }
            if (v.point.lng < west.point.lng) {
                west = v;
            }
            if (v.label.length() > longest.label.length()) {
                longest = v;
            }
            if (v.label.length() < shortest.label.length()) {
                shortest = v;
            }

        }

        System.out.println("Vertex extremes:");
        System.out.println("North extreme: " + north.label);
        System.out.println("South extreme: " + south.label);
        System.out.println("East extreme: " + east.label);
        System.out.println("West extreme: " + west.label);
        System.out.println("Longest label: " + longest.label + " length: " + longest.label.length());
        System.out.println("Shortest label: " + shortest.label + " length: " + shortest.label.length() + "\n");

        // Search for extreme edges and longest/shortest labels
        Edge longestEdge = null, shortestEdge = null, longestLabelEdge = null, shortestLabelEdge = null;
        int count = 0;
        double totalLength = 0.0;

        for (int check = 0; check < g.vertices.length; check++) {
            Vertex v = g.vertices[check];
            Edge e = v.head;
            while (e != null) {
                // Skip edges we've already checked
                if (e.dest < check) {
                    e = e.next;
                    continue;
                }
                totalLength += e.length;
                count++;
                if (longestEdge == null || e.length > longestEdge.length) {
                    longestEdge = e;
                }
                if (shortestEdge == null || e.length < shortestEdge.length) {
                    shortestEdge = e;
                }
                if (longestLabelEdge == null || e.label.length() > longestLabelEdge.label.length()) {
                    longestLabelEdge = e;
                }
                if (shortestLabelEdge == null || e.label.length() < shortestLabelEdge.label.length()) {
                    shortestLabelEdge = e;
                }

                e = e.next;
            }
        }

        System.out.println("Edge extremes:");
        System.out.println("Longest edge: " + longestEdge.label + " length: " + df.format(longestEdge.length));
        System.out.println("Shortest edge: " + shortestEdge.label + " length: " + df.format(shortestEdge.length));
        System.out.println("Longest label edge: " + longestLabelEdge.label + " length: " + longestLabelEdge.label.length());
        System.out.println("Shortest label edge: " + shortestLabelEdge.label + " length: " + shortestLabelEdge.label.length());
        System.out.println("Total number of edges: " + g.numEdges);
        System.out.println("Number of edges considered: " + count);
        System.out.println("Total length of edges: " + df.format(totalLength));
    }
}
