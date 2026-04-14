package edu.mams.app.model.util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The {@code LatinFill} class provides utilities for generating Latin squares
 * with various constraints. A Latin square is an n×n grid where every row
 * and every column contains each integer value from 0 to n-1 exactly once.
 * The class supports both generating unrestricted Latin squares and constrained ones.
 */
public class LatinFill {

    // ===== Public API =====

    /** Generate an n×cols grid (cols ≤ n) where:
     *  - Each column is a permutation of 0..n-1
     *  - Each row has unique values
     *  - No extra constraints
     */
    public static int[][] generate(int n, int cols) {
        return generate(n, cols, ThreadLocalRandom.current());
    }

    /** Generate with row-number constraints:
     *  forbidden[r][v] == true  => value v cannot appear anywhere in row r
     */
    public static int[][] generate(int n, int cols, boolean[][] forbidden) {
        return generate(n, cols, forbidden, ThreadLocalRandom.current());
    }

    private static int[][] generate(int n, int cols, Random rnd) {
        checkArgs(n, cols, null);

        int[] symPerm = identity(n);  // permutation of symbols (0..n-1)
        int[] rowPerm = identity(n);  // permutation of rows
        int[] colPick = pickKPermutation(n, cols, rnd); // choose 'cols' columns

        shuffle(symPerm, rnd);
        shuffle(rowPerm, rnd);
        // colPick is already a random k-permutation

        return buildFromPerms(n, cols, symPerm, rowPerm, colPick);
    }

    private static int[][] generate(int n, int cols, boolean[][] forbidden, Random rnd) {
        checkArgs(n, cols, forbidden);
        final int MAX_TRIES = 10000;

        for (int attempt = 0; attempt < MAX_TRIES; attempt++) {
            int[] symPerm = identity(n);
            int[] rowPerm = identity(n);
            int[] colPick = pickKPermutation(n, cols, rnd);

            shuffle(symPerm, rnd);
            shuffle(rowPerm, rnd);

            if (!violates(forbidden, n, cols, symPerm, rowPerm, colPick)) {
                return buildFromPerms(n, cols, symPerm, rowPerm, colPick);
            }
        }
        throw new IllegalStateException("Could not satisfy constraints after many tries. " +
                "Your constraints may be too tight.");
    }

    // ===== Core builders / checks =====

    // Build n×cols grid from the base Latin square using permutations
    private static int[][] buildFromPerms(int n, int cols,
                                          int[] symPerm, int[] rowPerm, int[] colPick) {
        int[][] out = new int[n][cols];
        for (int r = 0; r < n; r++) {
            int rr = rowPerm[r];
            for (int j = 0; j < cols; j++) {
                int cc = colPick[j];
                // Base Latin value is (rr + cc) % n; then apply symbol permutation
                out[r][j] = symPerm[(rr + cc) % n];
            }
        }
        // Sanity: columns must be permutations (no duplicates)
        sanityCheckColumns(out, n, cols);
        return out;
    }

    /**
     * Generate an n×n Latin square that extends a partially filled square.
     * Cells with value -1 are treated as empty.
     * Respects row-level forbidden constraints.
     */
    public static int[][] generateFromPartial(int[][] partial, boolean[][] forbidden) {
        return generateFromPartial(partial, forbidden, ThreadLocalRandom.current());
    }

    private static int[][] generateFromPartial(int[][] partial, boolean[][] forbidden, Random rnd) {
        int n = partial.length;
        if (n == 0) throw new IllegalArgumentException("partial must be non-empty");
        for (int[] row : partial) {
            if (row.length != n)
                throw new IllegalArgumentException("partial must be square (n×n)");
        }
        checkArgs(n, n, forbidden);

        int[][] grid = new int[n][n];
        boolean[][] rowUsed = new boolean[n][n];
        boolean[][] colUsed = new boolean[n][n];
        int emptyCount = 0;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                int v = partial[r][c];
                if (v == -1) {
                    grid[r][c] = -1;
                    emptyCount++;
                    continue;
                }
                if (v < 0 || v >= n) {
                    throw new IllegalArgumentException("partial contains out-of-range value at (" + r + "," + c + ")");
                }
                if (rowUsed[r][v] || colUsed[c][v]) {
                    throw new IllegalArgumentException("partial violates Latin constraints at (" + r + "," + c + ")");
                }
                if (forbidden != null && forbidden[r][v]) {
                    throw new IllegalArgumentException("partial violates forbidden constraint at (" + r + "," + c + ")");
                }
                grid[r][c] = v;
                rowUsed[r][v] = true;
                colUsed[c][v] = true;
            }
        }

        if (emptyCount == 0) return grid;

        if (fillLatinSquare(grid, rowUsed, colUsed, forbidden, rnd)) {
            sanityCheckColumns(grid, n, n);
            return grid;
        }

        throw new IllegalStateException(
                "Could not extend partial square. Constraints may be incompatible.");
    }

    private static boolean fillLatinSquare(int[][] grid,
                                           boolean[][] rowUsed,
                                           boolean[][] colUsed,
                                           boolean[][] forbidden,
                                           Random rnd) {
        int n = grid.length;
        int bestR = -1;
        int bestC = -1;
        int bestCount = Integer.MAX_VALUE;

        // Choose the next empty cell with the fewest candidates (MRV)
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] != -1) continue;
                int count = 0;
                for (int v = 0; v < n; v++) {
                    if (!rowUsed[r][v] && !colUsed[c][v] && (forbidden == null || !forbidden[r][v])) {
                        count++;
                    }
                }
                if (count == 0) return false;
                if (count < bestCount) {
                    bestCount = count;
                    bestR = r;
                    bestC = c;
                    if (bestCount == 1) break;
                }
            }
            if (bestCount == 1) break;
        }

        if (bestR == -1) return true; // no empty cells

        int[] candidates = new int[bestCount];
        int idx = 0;
        for (int v = 0; v < n; v++) {
            if (!rowUsed[bestR][v] && !colUsed[bestC][v] && (forbidden == null || !forbidden[bestR][v])) {
                candidates[idx++] = v;
            }
        }

        // Randomize candidate order for variety
        for (int i = candidates.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = candidates[i];
            candidates[i] = candidates[j];
            candidates[j] = t;
        }

        for (int v : candidates) {
            grid[bestR][bestC] = v;
            rowUsed[bestR][v] = true;
            colUsed[bestC][v] = true;

            if (fillLatinSquare(grid, rowUsed, colUsed, forbidden, rnd)) return true;

            grid[bestR][bestC] = -1;
            rowUsed[bestR][v] = false;
            colUsed[bestC][v] = false;
        }

        return false;
    }


    // Check if any forbidden[r][value] would be hit by current perms/column choices
    private static boolean violates(boolean[][] forbidden, int n, int cols, int[] symPerm, int[] rowPerm, int[] colPick) {
        if (forbidden == null) return false;
        for (int r = 0; r < n; r++) {
            int rr = rowPerm[r];
            for (int j = 0; j < cols; j++) {
                int cc = colPick[j];
                int val = symPerm[(rr + cc) % n];
                if (forbidden[r][val]) return true;
            }
        }
        return false;
    }

    private static void sanityCheckColumns(int[][] grid, int n, int cols) {
        for (int c = 0; c < cols; c++) {
            boolean[] seen = new boolean[n];
            for (int r = 0; r < n; r++) {
                int v = grid[r][c];
                if (v < 0 || v >= n || seen[v]) {
                    throw new IllegalStateException("Column not a permutation at col " + c);
                }
                seen[v] = true;
            }
        }
    }

    // ===== Utilities =====

    private static void checkArgs(int n, int cols, boolean[][] forbidden) {
        if (n < 1) throw new IllegalArgumentException("n must be ≥ 1");
        if (cols < 1 || cols > n) throw new IllegalArgumentException("cols must be in [1, n]");
        if (forbidden != null) {
            if (forbidden.length != n)
                throw new IllegalArgumentException("forbidden must be n×n");
            for (boolean[] row : forbidden) {
                if (row == null || row.length != n)
                    throw new IllegalArgumentException("forbidden must be n×n");
            }
        }
    }

    private static int[] identity(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        return a;
    }

    // Fisher–Yates
    private static void shuffle(int[] a, Random rnd) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    // Random k-permutation (sample k distinct columns, in random order)
    private static int[] pickKPermutation(int n, int k, Random rnd) {
        int[] base = identity(n);
        // partial shuffle to first k positions
        for (int i = 0; i < k; i++) {
            int j = i + rnd.nextInt(n - i);
            int t = base[i]; base[i] = base[j]; base[j] = t;
        }
        return Arrays.copyOf(base, k);
    }

    // ===== Demo =====

    public static void main(String[] args) {
        Random demoRandom = new Random(20260409L);

        printHeader("LatinFill Demo");
        System.out.println("Seed: 20260409 (fixed for repeatable presentation output)");
        System.out.println();

        int unrestrictedN = 6;
        int unrestrictedCols = 4;
        int[][] unrestricted = generate(unrestrictedN, unrestrictedCols, demoRandom);
        printScenarioHeader("1. Generate a Latin rectangle");
        printInput(unrestrictedN, unrestrictedCols);
        printStandardConstraints(unrestrictedN, unrestrictedCols);
        printOutput(unrestricted, null);
        System.out.println();

        int constrainedN = 6;
        int constrainedCols = 4;
        boolean[][] forbidden = new boolean[constrainedN][constrainedN];
        forbidden[0][0] = true;
        forbidden[1][1] = true;
        forbidden[2][2] = true;
        forbidden[3][3] = true;
        forbidden[4][4] = true;
        printScenarioHeader("2. Generate with row/value constraints");
        printInput(constrainedN, constrainedCols);
        printStandardConstraints(constrainedN, constrainedCols);
        printForbiddenConstraints(forbidden);
        int[][] constrained = generate(constrainedN, constrainedCols, forbidden, demoRandom);
        printOutput(constrained, forbidden);
        System.out.println();

        int[][] partial = {
                {0, -1, -1, -1},
                {-1, 2, -1, -1},
                {-1, -1, 1, -1},
                {-1, -1, -1, 3}
        };
        printScenarioHeader("3. Complete a partial Latin square");
        System.out.println("Input:");
        System.out.println("  n = 4");
        System.out.println("  partial square uses -1 for empty cells");
        System.out.println("  preset values must stay fixed");
        printGrid(partial);
        System.out.println("Constraints:");
        System.out.println("  - fill every -1 with a value in 0..3");
        System.out.println("  - each row must contain unique values");
        System.out.println("  - each column must contain 0..3 exactly once");
        int[][] completed = generateFromPartial(partial, null, demoRandom);
        printOutput(completed, null);
        System.out.println("  - preset values preserved: " + yesNo(preservesPresetValues(partial, completed)));
        System.out.println();

        int[][] invalid = {
                {0, 0},
                {-1, -1}
        };
        printScenarioHeader("4. Reject invalid input");
        System.out.println("Input:");
        System.out.println("  partial square:");
        printGrid(invalid);
        System.out.println("Constraints:");
        System.out.println("  - duplicate values in the same row are not allowed");
        System.out.println("Output:");
        try {
            generateFromPartial(invalid, null, demoRandom);
            System.out.println("  Unexpected success");
        } catch (IllegalArgumentException ex) {
            System.out.println("  Rejected with validation error: " + ex.getMessage());
        }
    }

    private static void printHeader(String title) {
        System.out.println(title);
        System.out.println("=".repeat(title.length()));
    }

    private static void printScenarioHeader(String title) {
        System.out.println(title);
        System.out.println("-".repeat(title.length()));
    }

    private static void printInput(int n, int cols) {
        System.out.println("Input:");
        System.out.println("  n = " + n + " values (" + valueRangeLabel(n) + ")");
        System.out.println("  cols = " + cols);
    }

    private static void printStandardConstraints(int n, int cols) {
        System.out.println("Constraints:");
        System.out.println("  - each column is a permutation of " + valueRangeLabel(n));
        System.out.println("  - each row has " + cols + " distinct values");
    }

    private static void printForbiddenConstraints(boolean[][] forbidden) {
        System.out.println("  - row-specific forbidden values:");
        for (int r = 0; r < forbidden.length; r++) {
            List<Integer> blocked = new ArrayList<>();
            for (int v = 0; v < forbidden[r].length; v++) {
                if (forbidden[r][v]) blocked.add(v);
            }
            if (!blocked.isEmpty()) {
                System.out.println("    row " + r + " cannot contain " + blocked);
            }
        }
    }

    private static void printOutput(int[][] grid, boolean[][] forbidden) {
        System.out.println("Output:");
        printGrid(grid);
        System.out.println("Checks:");
        System.out.println("  - rows contain no duplicates: " + yesNo(rowsAreUnique(grid)));
        System.out.println("  - columns are full permutations: " + yesNo(columnsArePermutations(grid)));
        if (forbidden != null) {
            System.out.println("  - forbidden values avoided: " + yesNo(respectsForbidden(grid, forbidden)));
        }
    }

    private static void printGrid(int[][] grid) {
        if (grid.length == 0) {
            System.out.println("  <empty>");
            return;
        }

        int cols = grid[0].length;
        StringBuilder header = new StringBuilder("      ");
        for (int c = 0; c < cols; c++) {
            header.append(String.format("c%-4d", c));
        }
        System.out.println(header);
        for (int r = 0; r < grid.length; r++) {
            StringBuilder row = new StringBuilder(String.format("  r%-2d ", r));
            for (int c = 0; c < cols; c++) {
                row.append(String.format("%-5d", grid[r][c]));
            }
            System.out.println(row);
        }
    }

    private static boolean rowsAreUnique(int[][] grid) {
        int n = grid.length;
        for (int[] row : grid) {
            boolean[] seen = new boolean[n];
            for (int value : row) {
                if (value < 0 || value >= n || seen[value]) return false;
                seen[value] = true;
            }
        }
        return true;
    }

    private static boolean columnsArePermutations(int[][] grid) {
        int n = grid.length;
        int cols = grid[0].length;
        for (int c = 0; c < cols; c++) {
            boolean[] seen = new boolean[n];
            for (int r = 0; r < n; r++) {
                int value = grid[r][c];
                if (value < 0 || value >= n || seen[value]) return false;
                seen[value] = true;
            }
            for (boolean present : seen) {
                if (!present) return false;
            }
        }
        return true;
    }

    private static boolean respectsForbidden(int[][] grid, boolean[][] forbidden) {
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (forbidden[r][grid[r][c]]) return false;
            }
        }
        return true;
    }

    private static boolean preservesPresetValues(int[][] partial, int[][] completed) {
        for (int r = 0; r < partial.length; r++) {
            for (int c = 0; c < partial[r].length; c++) {
                if (partial[r][c] != -1 && partial[r][c] != completed[r][c]) return false;
            }
        }
        return true;
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }

    private static String valueRangeLabel(int n) {
        return "0.." + (n - 1);
    }
}
