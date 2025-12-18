package edu.mams.app.model.util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LatinFill {

    // ===== Public API =====

    /** Generate an n×cols grid (cols ≤ n) where:
     *  - Each column is a permutation of 0..n-1
     *  - Each row has unique values
     *  - No extra constraints
     */
    public static int[][] generate(int n, int cols) {
        checkArgs(n, cols, null);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        int[] symPerm = identity(n);  // permutation of symbols (0..n-1)
        int[] rowPerm = identity(n);  // permutation of rows
        int[] colPick = pickKPermutation(n, cols, rnd); // choose 'cols' columns

        shuffle(symPerm, rnd);
        shuffle(rowPerm, rnd);
        // colPick is already a random k-permutation

        return buildFromPerms(n, cols, symPerm, rowPerm, colPick);
    }

    /** Generate with row-number constraints:
     *  forbidden[r][v] == true  => value v cannot appear anywhere in row r
     */
    public static int[][] generate(int n, int cols, boolean[][] forbidden) {
        checkArgs(n, cols, forbidden);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
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
        int n = partial.length;
        if (n == 0) throw new IllegalArgumentException("partial must be non-empty");
        for (int[] row : partial) {
            if (row.length != n)
                throw new IllegalArgumentException("partial must be square (n×n)");
        }
        checkArgs(n, n, forbidden);

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        final int MAX_TRIES = 20000;

        for (int attempt = 0; attempt < MAX_TRIES; attempt++) {
            int[] symPerm = identity(n);
            int[] rowPerm = identity(n);
            int[] colPerm = identity(n);

            shuffle(symPerm, rnd);
            shuffle(rowPerm, rnd);
            shuffle(colPerm, rnd);

            boolean ok = true;
            outer:
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    int given = partial[r][c];
                    if (given != -1) {
                        int rr = rowPerm[r];
                        int cc = colPerm[c];
                        int val = symPerm[(rr + cc) % n];
                        if (val != given) {
                            ok = false;
                            break outer;
                        }
                        if (forbidden != null && forbidden[r][val]) {
                            ok = false;
                            break outer;
                        }
                    }
                }
            }

            if (!ok) continue;

            int[][] out = new int[n][n];
            for (int r = 0; r < n; r++) {
                int rr = rowPerm[r];
                for (int c = 0; c < n; c++) {
                    int cc = colPerm[c];
                    out[r][c] = symPerm[(rr + cc) % n];
                }
            }

            sanityCheckColumns(out, n, n);
            return out;
        }

        throw new IllegalStateException(
                "Could not extend partial square after many tries. Constraints may be incompatible.");
    }


    // Check if any forbidden[r][value] would be hit by current perms/column choices
    private static boolean violates(boolean[][] forbidden, int n, int cols,
                                    int[] symPerm, int[] rowPerm, int[] colPick) {
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
        // 6 rows (0..5), 3 columns, no constraints
        int[][] a = generate(6, 3);
        System.out.println("No constraints:");
        for (int[] row : a) System.out.println(Arrays.toString(row));

        // Example constraints: forbid 2 in row 4, forbid 0 in row 0
        boolean[][] forbidden = new boolean[6][6];
        forbidden[4][2] = true;
        forbidden[0][0] = true;

        int[][] b = generate(6, 3, forbidden);
        System.out.println("\nWith constraints:");
        for (int[] row : b) System.out.println(Arrays.toString(row));
    }
}