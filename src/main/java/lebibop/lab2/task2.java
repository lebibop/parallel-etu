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
            System.out.printf("Process %2d (ID: %3d) is idle (no range assigned).%n",
                    rank, Thread.currentThread().getId());
        } else {
            int[] localRange = new int[2];

            if (rank == 0) {
                int localRangeSize = range / size;
                int remainder = range % size;

                for (int i = 0; i < size; i++) {
                    if (i >= range) {
                        continue;
                    }

                    int localStart, localEnd;

                    if (i < remainder) {
                        localStart = start + i * (localRangeSize + 1);
                        localEnd = localStart + localRangeSize;
                    } else {
                        localStart = start + (i * localRangeSize) + remainder;
                        localEnd = localStart + localRangeSize - 1;
                    }

                    if (i == 0) {
                        localRange[0] = localStart;
                        localRange[1] = localEnd;
                    } else {
                        MPI.COMM_WORLD.Send(new int[]{localStart, localEnd}, 0, 2, MPI.INT, i, 0);
                    }
                }
            } else {
                MPI.COMM_WORLD.Recv(localRange, 0, 2, MPI.INT, 0, 0);
            }

            if (localRange[0] > localRange[1]) {
                int temp = localRange[0];
                localRange[0] = localRange[1];
                localRange[1] = temp;
            }

            int localSum = 0;
            for (int i = localRange[0]; i <= localRange[1]; i++) {
                localSum += i;
            }

            System.out.printf("Process %2d (ID: %3d): start=%4d, end=%4d, sum=%d%n",
                    rank, Thread.currentThread().getId(), localRange[0], localRange[1], localSum);

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

                System.out.printf("Total sum: %d", globalSum);
            }
        }

        MPI.Finalize();
    }
}