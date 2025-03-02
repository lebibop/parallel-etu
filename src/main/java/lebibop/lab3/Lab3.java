package lebibop.lab3;

import mpi.MPI;
import mpi.MPIException;

import java.util.Random;

public class Lab3 {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int rows = Integer.parseInt(System.getProperty("row", "10"));
        int cols = Integer.parseInt(System.getProperty("col", "11"));

        if (rows == cols) {
            cols++;
        }

        int[][] matrix = new int[rows][cols];

        if (rank == 0) {
            Random rand = new Random();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = rand.nextInt(30) + 1;
                }
            }
            System.out.println("Initial matrix:");
            printMatrix(matrix);
        }

        for (int i = 0; i < rows; i++) {
            int responsibleProcess = i % size;
            int[] rowBuffer = new int[cols];

            if (rank == 0) {
                System.arraycopy(matrix[i], 0, rowBuffer, 0, cols);
                if (responsibleProcess != 0) {
                    MPI.COMM_WORLD.Send(rowBuffer, 0, cols, MPI.INT, responsibleProcess, i);
                }
            }

            if (rank == responsibleProcess) {
                if (rank != 0) {
                    MPI.COMM_WORLD.Recv(rowBuffer, 0, cols, MPI.INT, 0, i);
                }
                System.out.printf("Process %2d (ID: %3d) is modifying row - %2d%n",
                        rank, Thread.currentThread().getId(), i);
                modifyRow(rowBuffer);

                if (rank != 0) {
                    MPI.COMM_WORLD.Send(rowBuffer, 0, cols, MPI.INT, 0, i);
                }
            }

            if (rank == 0 && responsibleProcess != 0) {
                MPI.COMM_WORLD.Recv(rowBuffer, 0, cols, MPI.INT, responsibleProcess, i);
            }

            if (rank == 0) {
                System.arraycopy(rowBuffer, 0, matrix[i], 0, cols);
            }
        }

        for (int i = 0; i < rows; i++) {
            MPI.COMM_WORLD.Bcast(matrix[i], 0, cols, MPI.INT, 0);
        }

        if (rank == 0) {
            System.out.println("Modified matrix:");
            printMatrix(matrix);
        }

        int[] lastElementBuffer = new int[1];

        if (rank == 0) {
            lastElementBuffer[0] = matrix[rows - 1][cols - 1];
        }

        MPI.COMM_WORLD.Bcast(lastElementBuffer, 0, 1, MPI.INT, 0);

        int lastElement = lastElementBuffer[0];

        double localSum = 0;
        int localCount = 0;

        for (int i = rank; i < rows; i += size) {
            for (int j = 0; j < cols; j++) {
                int deviation = matrix[i][j] - lastElement;
                if (deviation > 0) {
                    localSum += deviation;
                    localCount++;
                    System.out.printf("Process %2d (ID: %3d): row=%2d, col=%2d -> %2d - %2d = %2d%n",
                            rank, Thread.currentThread().getId(), i, j, matrix[i][j], lastElement, deviation);
                }
            }
        }

        double[] globalSum = new double[1];
        int[] globalCount = new int[1];

        MPI.COMM_WORLD.Reduce(new double[]{localSum}, 0, globalSum, 0, 1, MPI.DOUBLE, MPI.SUM, 0);
        MPI.COMM_WORLD.Reduce(new int[]{localCount}, 0, globalCount, 0, 1, MPI.INT, MPI.SUM, 0);

        if (rank == 0) {
            System.out.printf("Global sum: %.1f%n", globalSum[0]);
            System.out.printf("Global count: %2d%n", globalCount[0]);
            double finalResult = (globalCount[0] > 0) ? (globalSum[0] / globalCount[0]) : 0;
            System.out.printf("Final average positive deviation: %.2f%n", finalResult);
        }

        MPI.Finalize();
    }

    private static void modifyRow(int[] row) {
        for (int i = 0; i < row.length; i++) {
            row[i] = makeDivisibleBy2And3(row[i]);
        }
    }

    private static int makeDivisibleBy2And3(int num) {
        int lower = (num / 6) * 6;
        int upper = lower + 6;
        return (num - lower <= upper - num) ? lower : upper;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int num : row) {
                System.out.printf("%3d ", num);
            }
            System.out.println();
        }
    }
}
