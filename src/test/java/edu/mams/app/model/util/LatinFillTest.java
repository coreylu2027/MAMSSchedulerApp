package edu.mams.app.model.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LatinFillTest {

    @Test
    void generateCreatesValidLatinGridForRequestedColumns() {
        int n = 6;
        int cols = 3;
        int[][] grid = LatinFill.generate(n, cols);

        assertLatinProperties(grid, n, cols);
    }

    @Test
    void generateWithForbiddenRespectsConstraints() {
        int n = 5;
        int cols = 3;
        boolean[][] forbidden = new boolean[n][n];
        forbidden[0][0] = true;
        forbidden[1][1] = true;

        int[][] grid = LatinFill.generate(n, cols, forbidden);

        assertLatinProperties(grid, n, cols);
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < cols; c++) {
                assertFalse(forbidden[r][grid[r][c]]);
            }
        }
    }

    @Test
    void generateFromPartialExtendsValidSquare() {
        int[][] partial = {
                {0, -1, -1},
                {-1, 2, -1},
                {-1, -1, 1}
        };

        int[][] grid = LatinFill.generateFromPartial(partial, null);

        assertLatinProperties(grid, 3, 3);
        assertEquals(0, grid[0][0]);
        assertEquals(2, grid[1][1]);
        assertEquals(1, grid[2][2]);
    }

    @Test
    void generateFromPartialRejectsInvalidInput() {
        int[][] invalid = {
                {0, 0},
                {-1, -1}
        };

        assertThrows(IllegalArgumentException.class, () -> LatinFill.generateFromPartial(invalid, null));
    }

    private static void assertLatinProperties(int[][] grid, int n, int cols) {
        assertEquals(n, grid.length);
        for (int r = 0; r < n; r++) {
            assertEquals(cols, grid[r].length);
            boolean[] rowSeen = new boolean[n];
            for (int c = 0; c < cols; c++) {
                int value = grid[r][c];
                assertTrue(value >= 0 && value < n);
                assertFalse(rowSeen[value]);
                rowSeen[value] = true;
            }
        }

        for (int c = 0; c < cols; c++) {
            boolean[] colSeen = new boolean[n];
            for (int r = 0; r < n; r++) {
                int value = grid[r][c];
                assertFalse(colSeen[value]);
                colSeen[value] = true;
            }
            for (boolean seen : colSeen) {
                assertTrue(seen);
            }
        }
    }
}
