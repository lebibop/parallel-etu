package lebibop.lab5;

import mpi.MPI;
import mpi.MPIException;

import java.util.Arrays;
import java.util.Random;

public class LSTCalculator {

    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        final int n = Integer.parseInt(System.getProperty("n", "500"));  // Количество операций (вершин)
        final int p = Integer.parseInt(System.getProperty("p", "100"));    // Плотность графа (чем больше p, тем больше рёбер)

        int[] durations = new int[n];      // Длительности операций
        boolean[] isFinal = new boolean[n]; // Массив для отметки финальных операций (нет исходящих рёбер)
        int[] flatGraph = new int[n * n];  // Граф в виде "плоской" матрицы смежности

        if (rank == 0) {
            generateGraph(flatGraph, durations, isFinal, n, p); // Генерация случайного ацикличного графа
        }

        // Рассылаем длительности и финальные метки всем процессам
        MPI.COMM_WORLD.Bcast(durations, 0, n, MPI.INT, 0);
        MPI.COMM_WORLD.Bcast(isFinal, 0, n, MPI.BOOLEAN, 0);

        // Распределяем строки графа между процессами
        int[] sendCounts = new int[size], displs = new int[size];
        int[] rowsPerProcess = new int[size], rowOffsets = new int[size];
        distribute(n, size, sendCounts, displs, rowsPerProcess, rowOffsets);

        int localRows = rowsPerProcess[rank];
        int[] localGraph = new int[localRows * n];
        MPI.COMM_WORLD.Scatterv(flatGraph, 0, sendCounts, displs, MPI.INT, localGraph, 0, localRows * n, MPI.INT, 0);

        int globalRowStart = rowOffsets[rank];
        int globalRowEnd = globalRowStart + localRows - 1;
        System.out.printf("Process %d recieved vert %d - %d\n", rank, globalRowStart, globalRowEnd);

        long startTime = 0, endTime;
        if (rank == 0) startTime = System.nanoTime();

        // Вычисляем ранние сроки завершения (EFT) по графу вперёд
        int[] localEFT = new int[localRows];
        Arrays.fill(localEFT, 0);
        int[] globalEFT = new int[n];
        computeEFT(localGraph, durations, localEFT, globalEFT, rowsPerProcess, rowOffsets, localRows, globalRowStart, n);

        // Инициализируем поздние сроки окончания (LFT) максимумом по EFT для финальных операций
        int[] localLFT = new int[localRows];
        Arrays.fill(localLFT, Integer.MAX_VALUE);
        for (int i = 0; i < localRows; i++) {
            int globalIdx = globalRowStart + i;
            if (isFinal[globalIdx]) {
                // Если операция финальная, её LFT = максимальный EFT по всему графу
                localLFT[i] = Arrays.stream(globalEFT).max().orElse(0);
            }
        }

        // Вычисляем поздние сроки окончания (LFT) по графу назад
        int[] globalLFT = new int[n];
        computeLFT(localGraph, durations, localLFT, globalLFT, rowsPerProcess, rowOffsets, localRows, globalRowStart, n);

        // Собираем LFT всех вершин от процессов
        int[] finalLFT = new int[n];
        MPI.COMM_WORLD.Allgatherv(localLFT, 0, localRows, MPI.INT, finalLFT, 0, rowsPerProcess, rowOffsets, MPI.INT);

        // Вычисляем LST (поздний срок начала) для каждой операции
        int[] finalLST = new int[n];
        for (int i = 0; i < n; i++) {
            finalLST[i] = finalLFT[i] - durations[i];
        }

        // Вывод результатов
        if (rank == 0) {
            printAllLST(finalLST);
            endTime = System.nanoTime();
            double elapsedTime = (endTime - startTime) / 100_000_000.0;
            System.out.printf("\n[RESULT] Time taken to compute LST: %.2f ms\n", elapsedTime);
        }

        MPI.Finalize();
    }

    // Вывод всех LST для наглядности
    private static void printAllLST(int[] array) {
        System.out.println("\n[RESULT] Full Late Start Times (LST) for all operations:");
        for (int i = 0; i < array.length; i++) {
            System.out.printf("Operation %d: LST = %d\n", i, array[i]);
        }
    }

    // Вычисление ранних сроков завершения (EFT)
    private static void computeEFT(int[] localGraph, int[] durations, int[] localEFT, int[] globalEFT,
                                   int[] recvCounts, int[] recvDispls, int localRows, int globalRowStart, int n) throws MPIException {
        boolean updated;
        do {
            updated = false;
            // Обмен текущими значениями EFT между процессами
            MPI.COMM_WORLD.Allgatherv(localEFT, 0, localRows, MPI.INT, globalEFT, 0, recvCounts, recvDispls, MPI.INT);

            // Обновление EFT по зависимостям
            for (int i = 0; i < localRows; i++) {
                int globalRow = globalRowStart + i;
                int maxPred = 0;
                for (int j = 0; j < n; j++) {
                    int weight = localGraph[i * n + j];
                    if (weight > 0) {
                        // Выбираем максимальный предшественник
                        maxPred = Math.max(maxPred, globalEFT[j] + weight);
                    }
                }
                int newEFT = maxPred + durations[globalRow];
                if (newEFT > localEFT[i]) {
                    localEFT[i] = newEFT;
                    updated = true;
                }
            }

            // Проверяем, были ли обновления на каком-либо процессе
            boolean[] globalUpdated = new boolean[1];
            MPI.COMM_WORLD.Allreduce(new boolean[]{updated}, 0, globalUpdated, 0, 1, MPI.BOOLEAN, MPI.LOR);
            updated = globalUpdated[0];
        } while (updated); // Повторяем, пока есть изменения
    }

    // Вычисление поздних сроков завершения (LFT) в обратном направлении
    private static void computeLFT(int[] localGraph, int[] durations, int[] localLFT, int[] globalLFT,
                                   int[] recvCounts, int[] recvDispls, int localRows, int globalRowStart, int n) throws MPIException {
        boolean updated;
        do {
            updated = false;
            // Обмен текущими значениями LFT между процессами
            MPI.COMM_WORLD.Allgatherv(localLFT, 0, localRows, MPI.INT, globalLFT, 0, recvCounts, recvDispls, MPI.INT);

            // Обновление LFT по зависимостям (двигаемся от потомков к предкам)
            for (int i = 0; i < localRows; i++) {
                int globalIdx = globalRowStart + i;
                int minSucc = Integer.MAX_VALUE;
                boolean hasSucc = false;
                for (int j = 0; j < n; j++) {
                    int weight = localGraph[i * n + j];
                    if (weight > 0) {
                        // Выбираем минимальный LFT потомка
                        int candidate = globalLFT[j] - weight - durations[globalIdx];
                        minSucc = Math.min(minSucc, candidate);
                        hasSucc = true;
                    }
                }
                if (hasSucc && minSucc < localLFT[i]) {
                    localLFT[i] = minSucc;
                    updated = true;
                }
            }

            // Проверяем, были ли обновления
            boolean[] globalUpdated = new boolean[1];
            MPI.COMM_WORLD.Allreduce(new boolean[]{updated}, 0, globalUpdated, 0, 1, MPI.BOOLEAN, MPI.LOR);
            updated = globalUpdated[0];
        } while (updated); // Повторяем до сходимости
    }

    // Генерация случайного ацикличного графа
    private static void generateGraph(int[] flatGraph, int[] durations, boolean[] isFinal, int n, int p) {
        Random rand = new Random();
        int[][] graph = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (rand.nextDouble() < (double) p / 100) {
                    graph[i][j] = rand.nextInt(10) + 1; // Вес ребра
                }
            }
        }
        for (int i = 0; i < n; i++) durations[i] = rand.nextInt(5) + 1;
        for (int i = 0; i < n; i++) durations[i] = rand.nextInt(5) + 1;

        // Определяем финальные вершины (нет исходящих рёбер)
        for (int i = 0; i < n; i++) {
            boolean hasOutgoing = false;
            for (int j = 0; j < n; j++) {
                if (graph[i][j] > 0) {
                    hasOutgoing = true;
                    break;
                }
            }
            isFinal[i] = !hasOutgoing;
        }

        // Преобразуем 2D граф в плоский массив
        for (int i = 0; i < n; i++) {
            System.arraycopy(graph[i], 0, flatGraph, i * n, n);
        }

    }

    // Распределение строк графа по процессам
    private static void distribute(int n, int size, int[] sendCounts, int[] displs, int[] rowsPerProcess, int[] rowOffsets) {
        int base = n / size, extra = n % size, offset = 0, rowOffset = 0;
        for (int i = 0; i < size; i++) {
            int rows = base + (i < extra ? 1 : 0);
            sendCounts[i] = rows * n;
            displs[i] = offset;
            rowsPerProcess[i] = rows;
            rowOffsets[i] = rowOffset;
            offset += sendCounts[i];
            rowOffset += rows;
        }
    }
}
