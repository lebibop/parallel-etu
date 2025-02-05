package lebibop.lab2;

import mpi.MPI;

public class task2 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int start = Integer.parseInt(System.getProperty("start", "1"));
        int end = Integer.parseInt(System.getProperty("end", "100"));
        int range = end - start + 1;

        if (rank >= range) {
            System.out.println("Process " + rank + " is idle (no range assigned).");
        } else {
            int localRange = range / size;
            int remainder = range % size;

            int localStart, localEnd;

            if (rank < remainder) {
                localStart = start + rank * (localRange + 1);
                localEnd = localStart + localRange;
            } else {
                localStart = start + (rank * localRange) + remainder;
                localEnd = localStart + localRange - 1;
            }

            if (localStart > localEnd) {
                int temp = localStart;
                localStart = localEnd;
                localEnd = temp;
            }

            int localSum = 0;
            for (int i = localStart; i <= localEnd; i++) {
                localSum += i;
            }

            System.out.println("Process " + rank + ": start-" + localStart + " end-" + localEnd + " sum-" + localSum);

            if (rank != 0) {
                MPI.COMM_WORLD.Send(new int[]{localSum}, 0, 1, MPI.INT, 0, 0);
            } else {
                int[] allSums = new int[size];
                allSums[0] = localSum;

                for (int i = 1; i < size; i++) {
                    if (i < range) {
                        int[] receivedSum = new int[1];
                        MPI.COMM_WORLD.Recv(receivedSum, 0, 1, MPI.INT, i, 0);
                        allSums[i] = receivedSum[0];
                    } else {
                        allSums[i] = 0;
                    }
                }

                int globalSum = 0;
                for (int i = 0; i < size; i++) {
                    globalSum += allSums[i];
                }

                System.out.println("Total sum: " + globalSum);
            }
        }

        MPI.Finalize();
    }
}