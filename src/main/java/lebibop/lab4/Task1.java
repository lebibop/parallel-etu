package lebibop.lab4;

import mpi.MPI;

public class Task1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int[] range = new int[2];
        range[0] = Integer.parseInt(System.getProperty("start", "1"));
        range[1] = Integer.parseInt(System.getProperty("end", "100"));

        MPI.COMM_WORLD.Bcast(range, 0, 2, MPI.INT, 0);

        int start = range[0];
        int end = range[1];
        int totalNumbers = end - start + 1;

        int numbersPerProcess = totalNumbers / size;
        int remainder = totalNumbers % size;

        int localStart = start + rank * numbersPerProcess + Math.min(rank, remainder);
        int localEnd = localStart + numbersPerProcess - 1;
        if (rank < remainder) {
            localEnd++;
        }

        int localSum = 0;
        if (localStart <= end) {
            for (int i = localStart; i <= localEnd; i++) {
                localSum += i;
            }
            System.out.printf("Process %2d (ID: %3d): [%5d; %5d] -> localSum = %d%n",
                    rank, Thread.currentThread().getId(), localStart, localEnd, localSum);
        } else {
            System.out.printf("Process %2d (ID: %3d): does not participate (no range assigned).%n",
                    rank, Thread.currentThread().getId());
        }

        int[] globalSum = new int[1];
        MPI.COMM_WORLD.Reduce(new int[]{localSum}, 0, globalSum, 0, 1, MPI.INT, MPI.SUM, 0);

        if (rank == 0) {
            System.out.printf("Total sum: %d", globalSum[0]);
        }

        MPI.Finalize();
    }
}