package lebibop;

import mpi.MPI;

public class Lab1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        //task1(rank, size);
        //task2(rank, size);
        task3(rank, size);

        MPI.Finalize();
    }

    // Задание 1: Запуск программы на 2 процессах
    public static void task1(int rank, int size) {
        if (size >= 2) {
            System.out.println("Process " + rank);
        }
    }

    // Задание 2: Вывод номера процесса и его ID на 3 процессах
    public static void task2(int rank, int size) {
        if (size >= 3) {
            System.out.println("Process " + rank + " ID: " + Thread.currentThread().getId());
        }
    }

    // Задание 3: Вычисление таблицы умножения на n процессах
    public static void task3(int rank, int size) {
        if (rank == 0) {
            System.out.println("Задание 3: Таблица умножения, рассчитанная процессами:");
        }

        for (int i = rank + 1; i <= 10; i += size) {
            for (int j = 1; j <= 10; j++) {
                System.out.println("Process " + rank + ": " + i + "x" + j + "=" + (i * j));
            }
        }
    }
}
