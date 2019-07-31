
public class ShortestPath {
    private final int height;
    private final int width;
    private final double[][] energy;
    private double[][] distTo;
    private int[][] vertexFrom;

    public ShortestPath(double[][] energy, int height, int width) {
        this.energy = energy;
        this.height = height;
        this.width = width;
        distTo = new double[height][width];
        vertexFrom = new int[height][width];

        // initialize distance matrix
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                distTo[i][j] = Double.POSITIVE_INFINITY;
            }
        }
    }

    public int[] findShortestPath() {
        relaxEdges();
        return findMinPath();
    }

    private void relaxEdges() {
        // initialize first row
        for (int i = 0; i < width; i++) {
            distTo[0][i] = energy[0][i];
        }

        // relax all other edges
        for (int i = 1; i < height; i++) {
            for (int j = 0; j < width; j++) {
                visit(i, j);
            }
        }
    }

    private void visit(int r, int c) {
        if (c != 0)          relaxEdge(r - 1, c - 1, r, c);
        relaxEdge(r - 1, c, r, c);
        if (c != width - 1)  relaxEdge(r - 1, c + 1, r, c);
    }

    private void relaxEdge(int rFrom, int cFrom, int r, int c) {
        if (distTo[r][c] > energy[r][c] + distTo[rFrom][cFrom]) {
            distTo[r][c] = energy[r][c] + distTo[rFrom][cFrom];
            vertexFrom[r][c] = cFrom;
        }
    }

    // fill the path index array from bottom to top
    private int[] findMinPath() {
        int[] path = new int[height];
        int minIdx = 0;
        double minDist = Double.POSITIVE_INFINITY;

        // find minimum energy sum in bottom row
        for (int i = 0; i < width; i++) {
            if (distTo[height - 1][i] < minDist) {
                minDist = distTo[height - 1][i];
                minIdx = i;
            }
        }
        path[height - 1] = minIdx;

        // go up from last row to find the path
        for (int i = height - 2; i >= 0; i--) {
            int col = vertexFrom[i + 1][path[i + 1]];
            path[i] = col;
        }
        return path;
    }
}
