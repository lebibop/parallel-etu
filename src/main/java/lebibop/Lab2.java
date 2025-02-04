package lebibop;

import mpi.*;

public class Lab2 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        //task1(rank);
        task2(rank, size);

        MPI.Finalize();
    }

    public static void task1(int rank) {
        int ai = rank + 1;
        int bi = (rank + 1) * 2;

        System.out.println("Process " + rank + " -> ai: " + ai + ", bi: " + bi);

        int[] received_a = new int[1];
        int[] received_b = new int[1];

        Request sendRequest1 = null, sendRequest2 = null;
        Request recvRequest1 = null, recvRequest2 = null;

        switch (rank) {
            case 0:
                sendRequest1 = MPI.COMM_WORLD.Isend(new int[]{ai}, 0, 1, MPI.INT, 2, 99);
                sendRequest2 = MPI.COMM_WORLD.Isend(new int[]{bi}, 0, 1, MPI.INT, 1, 99);
                recvRequest1 = MPI.COMM_WORLD.Irecv(received_a, 0, 1, MPI.INT, 1, 99);
                recvRequest2 = MPI.COMM_WORLD.Irecv(received_b, 0, 1, MPI.INT, 2, 99);
                break;
            case 1:
                sendRequest1 = MPI.COMM_WORLD.Isend(new int[]{ai}, 0, 1, MPI.INT, 0, 99);
                sendRequest2 = MPI.COMM_WORLD.Isend(new int[]{bi}, 0, 1, MPI.INT, 3, 99);
                recvRequest1 = MPI.COMM_WORLD.Irecv(received_a, 0, 1, MPI.INT, 3, 99);
                recvRequest2 = MPI.COMM_WORLD.Irecv(received_b, 0, 1, MPI.INT, 0, 99);
                break;
            case 2:
                sendRequest1 = MPI.COMM_WORLD.Isend(new int[]{ai}, 0, 1, MPI.INT, 3, 99);
                sendRequest2 = MPI.COMM_WORLD.Isend(new int[]{bi}, 0, 1, MPI.INT, 0, 99);
                recvRequest1 = MPI.COMM_WORLD.Irecv(received_a, 0, 1, MPI.INT, 0, 99);
                recvRequest2 = MPI.COMM_WORLD.Irecv(received_b, 0, 1, MPI.INT, 3, 99);
                break;
            case 3:
                sendRequest1 = MPI.COMM_WORLD.Isend(new int[]{ai}, 0, 1, MPI.INT, 1, 99);
                sendRequest2 = MPI.COMM_WORLD.Isend(new int[]{bi}, 0, 1, MPI.INT, 2, 99);
                recvRequest1 = MPI.COMM_WORLD.Irecv(received_a, 0, 1, MPI.INT, 2, 99);
                recvRequest2 = MPI.COMM_WORLD.Irecv(received_b, 0, 1, MPI.INT, 1, 99);
                break;
        }

        assert sendRequest1 != null;
        sendRequest1.Wait();
        sendRequest2.Wait();
        recvRequest1.Wait();
        recvRequest2.Wait();

        System.out.println("Process " + rank
                + " received: a=" + received_a[0]
                + ", b=" + received_b[0]
                + " -> c" + rank + " = " + (received_a[0] + received_b[0]));
    }

    public static void task2(int rank, int size) {
        int start = 0;
        int end = 5;
        int range = end - start + 1;

        // Разбиваем диапазон на части для каждого процесса
        int localStart = start + rank * (range / size);
        int localEnd = (rank + 1) * (range / size) - 1;
        if (rank == size - 1) {
            localEnd = end;  // последний процесс берет остаток
        }

        // Вычисляем локальную сумму
        int localSum = 0;
        for (int i = localStart; i <= localEnd; i++) {
            localSum += i;
        }

        // Массив для сбора всех локальных сумм на процессе с рангом 0
        int[] allSums = new int[size];

        // Процесс 0 будет собирать суммы от всех процессов
        if (rank == 0) {
            // Процесс 0 уже знает свою локальную сумму
            allSums[0] = localSum;

            // Получаем локальные суммы от остальных процессов
            for (int i = 1; i < size; i++) {
                int[] receivedSum = new int[1];
                MPI.COMM_WORLD.Recv(receivedSum, 0, 1, MPI.INT, i, 0);
                allSums[i] = receivedSum[0];
            }

            // Суммируем все локальные суммы
            int globalSum = 0;
            for (int i = 0; i < size; i++) {
                globalSum += allSums[i];
            }

            // Выводим итоговую сумму
            System.out.println("Total sum: " + globalSum);
        } else {
            // Все другие процессы отправляют свои локальные суммы на процесс 0
            MPI.COMM_WORLD.Send(new int[]{localSum}, 0, 1, MPI.INT, 0, 0);
        }
    }
}
