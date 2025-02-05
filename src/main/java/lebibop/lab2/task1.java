package lebibop.lab2;

import mpi.MPI;

public class task1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();

        int ai = rank + 1;
        int bi = (rank + 1) * 2;

        System.out.println("Process " + rank + " ID: " + Thread.currentThread().getId()
                + " -> ai: " + ai + ", bi: " + bi);

        int[] received_a = new int[1];
        int[] received_b = new int[1];

        int send_a_to, send_b_to;
        int recv_a_from, recv_b_from;

        switch (rank) {
            case 0:
                send_a_to = 2;
                send_b_to = 1;
                recv_a_from = 1;
                recv_b_from = 2;
                break;
            case 1:
                send_a_to = 0;
                send_b_to = 3;
                recv_a_from = 3;
                recv_b_from = 0;
                break;
            case 2:
                send_a_to = 3;
                send_b_to = 0;
                recv_a_from = 0;
                recv_b_from = 3;
                break;
            case 3:
                send_a_to = 1;
                send_b_to = 2;
                recv_a_from = 2;
                recv_b_from = 1;
                break;
            default:
                throw new IllegalStateException("Unexpected rank: " + rank);
        }

        if (rank == 0 || rank == 3) {
            MPI.COMM_WORLD.Send(new int[]{ai}, 0, 1, MPI.INT, send_a_to, 99);
            MPI.COMM_WORLD.Send(new int[]{bi}, 0, 1, MPI.INT, send_b_to, 99);
            MPI.COMM_WORLD.Recv(received_a, 0, 1, MPI.INT, recv_a_from, 99);
            MPI.COMM_WORLD.Recv(received_b, 0, 1, MPI.INT, recv_b_from, 99);
        } else {
            MPI.COMM_WORLD.Recv(received_a, 0, 1, MPI.INT, recv_a_from, 99);
            MPI.COMM_WORLD.Recv(received_b, 0, 1, MPI.INT, recv_b_from, 99);
            MPI.COMM_WORLD.Send(new int[]{ai}, 0, 1, MPI.INT, send_a_to, 99);
            MPI.COMM_WORLD.Send(new int[]{bi}, 0, 1, MPI.INT, send_b_to, 99);
        }

        System.out.println("Process " + rank  + " ID: " + Thread.currentThread().getId()
                + " received: a=" + received_a[0]
                + ", b=" + received_b[0]
                + " -> c" + rank + " = " + (received_a[0] + received_b[0]));

        MPI.Finalize();
    }
}