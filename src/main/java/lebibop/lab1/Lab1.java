package lebibop.lab1;

import mpi.MPI;

public class Lab1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        for (int i = rank + 1; i <= 10; i += size) {
            for (int j = 1; j <= 10; j++) {
                System.out.println("Process " + rank + " ID: " + Thread.currentThread().getId()
                        + ": " + i + "x" + j + "=" + (i * j));
            }
        }

        MPI.Finalize();
    }
}
