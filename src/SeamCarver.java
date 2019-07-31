import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    private int[][] colors;       // store colors as encoded 32-bits int
    private double[][] energies;  // energy matrix
    private int height;
    private int width;
    private boolean isHorizontal;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("Null input.");
        }

        height = picture.height();
        width = picture.width();

        energies = new double[height][width];
        colors = new int[height][width];

        buildColorMatrix(picture);
        buildEnergyMatrix();
    }

    private void buildColorMatrix(Picture picture) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                colors[i][j] = picture.getRGB(j, i); // col, row is reversed in class Picture
            }
        }
    }

    private void buildEnergyMatrix() {

        for (int i = 0; i < height; i++) {
            energies[i][0] = 1000;
            energies[i][width - 1] = 1000;
        }
        for (int i = 0; i < width; i++) {
            energies[0][i] = 1000;
            energies[height - 1][i] = 1000;
        }

        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                energies[i][j] = calculateEnergy(i, j);
            }
        }
    }

    // calculate energy value for pixel row i, col j
    private double calculateEnergy(int i, int j) {
        if (i == 0 || j == 0 || i == height - 1 || j == width - 1) {
            return 1000;
        }

        double energy = 0.0;

        int[] left  = decodeColor(colors[i][j - 1]);
        int[] right = decodeColor(colors[i][j + 1]);
        int[] up    = decodeColor(colors[i - 1][j]);
        int[] down  = decodeColor(colors[i + 1][j]);

        for (int k = 0; k < 3; k++) {
            energy += (left[k] - right[k]) * (left[k] - right[k]);
            energy += (up[k] - down[k]) * (up[k] - down[k]);
        }
        return Math.sqrt(energy);
    }

    private int[] decodeColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >>  8) & 0xFF;
        int b = (color >>  0) & 0xFF;
        return new int[]{r, g, b};
    }

    // only create a new Picture object when asked
    public Picture picture() {
        if (isHorizontal) transpose();

        Picture newPicture = new Picture(width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                newPicture.setRGB(j, i, colors[i][j]);
            }
        }

        return newPicture;
    }

    // width of current picture
    public int width() {
        return isHorizontal ? height : width;
    }

    // height of current picture
    public int height() {
        return isHorizontal ? width : height;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        validateInput(x, y);
        return isHorizontal ? energies[x][y] : energies[y][x];
    }

    // col x, row y
    private void validateInput(int x, int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Negative input.");
        }
        if (isHorizontal && (x >= height || y >= width)) {
            throw new IllegalArgumentException("Out of bound.");
        }
        if (!isHorizontal && (x >= width || y >= height)) {
            throw new IllegalArgumentException("Out of bound.");
        }
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        if (!isHorizontal) transpose();
        ShortestPath shortestPath = new ShortestPath(energies, height, width);
        return shortestPath.findShortestPath();
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        if (isHorizontal) transpose();
        ShortestPath shortestPath = new ShortestPath(energies, height, width);
        return shortestPath.findShortestPath();
    }

    // transpose colors and energies matrix, and swap width, height value
    private void transpose() {
        isHorizontal = !isHorizontal;
        int temp = width;
        width = height;
        height = temp;

        energies = transposeMatrix(energies);
        colors = transposeMatrix(colors);
    }

    // transpose int matrix
    private int[][] transposeMatrix(int[][] arr) {
        int d1 = arr.length;
        int d2 = arr[0].length;
        int[][] trans = new int[d2][d1];

        for (int i = 0; i < d2; i++) {
            for (int j = 0; j < d1; j++) {
                trans[i][j] = arr[j][i];
            }
        }
        return trans;
    }

    // transpose double matrix
    private double[][] transposeMatrix(double[][] arr) {
        int d1 = arr.length;
        int d2 = arr[0].length;
        double[][] trans = new double[d2][d1];

        for (int i = 0; i < d2; i++) {
            for (int j = 0; j < d1; j++) {
                trans[i][j] = arr[j][i];
            }
        }
        return trans;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (!isHorizontal) transpose();
        validateSeam(seam);
        removeSeam(seam);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (isHorizontal) transpose();
        validateSeam(seam);
        removeSeam(seam);
    }

    private void validateSeam(int[] seam) {
        if (seam == null)  throw new IllegalArgumentException("Null input.");
        if (seam.length != height) {
            throw new IllegalArgumentException("Invalid seam input.");
        }
        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] >= width) {
                throw new IllegalArgumentException("Contains seam element out of bound");
            }
            if (i != seam.length - 1) {
                if (Math.abs(seam[i] - seam[i + 1]) > 1) {
                    throw new IllegalArgumentException("Invalid seam input.");
                }
            }
        }
    }

    private void removeSeam(int[] seam) {
        for (int i = 0; i < height; i++) {
            int toRemove = seam[i];
            for (int j = toRemove; j < width - 1; j++) {
                colors[i][j] = colors[i][j + 1];
                energies[i][j] = energies[i][j + 1];
            }
        }
        updateEnergyMatrix(seam);
        width--;
    }

    // update energy values which are effected by seam removal
    private void updateEnergyMatrix(int[] seam) {
        for (int i = 0; i < height; i++) {
            int removed = seam[i];
            if (i != 0)                energies[i - 1][removed] = calculateEnergy(i - 1, removed);
            if (i != height - 1)       energies[i + 1][removed] = calculateEnergy(i + 1, removed);
            if (removed != 0)          energies[i][removed - 1] = calculateEnergy(i, removed - 1);
            if (removed != width - 1)  energies[i][removed + 1] = calculateEnergy(i, removed + 1);
        }
    }
}