package lebibop.lab4;

import mpi.MPI;
import mpi.MPIException;

import java.util.Random;

public class Task2 {
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
        int[] flatMatrix = new int[rows * cols];

        if (rank == 0) {
            Random rand = new Random();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = rand.nextInt(30) + 1;
                }
            }
            System.out.println("Matrix:");
            printMatrix(matrix);

            for (int i = 0; i < rows; i++) {
                System.arraycopy(matrix[i], 0, flatMatrix, i * cols, cols);
            }
        }

        int baseRows = rows / size;
        int extra = rows % size;
        int[] sendCounts = new int[size];
        int[] displs = new int[size];
        int offset = 0;
        for (int i = 0; i < size; i++) {
            int myRows = baseRows + (i < extra ? 1 : 0);
            sendCounts[i] = myRows * cols;
            displs[i] = offset;
            offset += sendCounts[i];
        }

        int recvCount = sendCounts[rank];
        int recvRows = recvCount / cols;
        int[] recvBuffer = new int[recvCount];

        MPI.COMM_WORLD.Scatterv(flatMatrix, 0, sendCounts, displs, MPI.INT, recvBuffer, 0, recvCount, MPI.INT, 0);

        int[] lastElementBuffer = new int[1];
        if (rank == 0) {
            lastElementBuffer[0] = matrix[rows - 1][cols - 1];
        }
        MPI.COMM_WORLD.Bcast(lastElementBuffer, 0, 1, MPI.INT, 0);
        int lastElement = lastElementBuffer[0];

        double localSum = 0;
        int localCount = 0;
        int globalRowStart = displs[rank] / cols;

        for (int i = 0; i < recvRows; i++) {
            for (int j = 0; j < cols; j++) {
                int value = recvBuffer[i * cols + j];
                int deviation = value - lastElement;
                if (deviation > 0) {
                    localSum += deviation;
                    localCount++;
                    System.out.printf("Process %2d (Thread ID: %3d): row=%2d, col=%2d -> %2d - %2d = %2d%n",
                            rank, Thread.currentThread().getId(), globalRowStart + i, j, value, lastElement, deviation);
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

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int num : row) {
                System.out.printf("%3d ", num);
            }
            System.out.println();
        }
    }
}
