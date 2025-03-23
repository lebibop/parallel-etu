package lebibop.lab5;

import java.util.Arrays;
import java.util.Random;

public class LSTCalculatorSequential {

    public static void main(String[] args) {
        final int n = 100;  // Количество операций (вершин)
        final int p = 10;  // Плотность графа (чем больше p, тем больше рёбер)

        int[] durations = new int[n];      // Длительности операций
        boolean[] isFinal = new boolean[n]; // Массив для отметки финальных операций (нет исходящих рёбер)
        int[][] graph = new int[n][n];     // Граф в виде матрицы смежности

        generateGraph(graph, durations, isFinal, n, p); // Генерация случайного ацикличного графа

        long startTime = System.nanoTime();

        // Вычисляем ранние сроки завершения (EFT) по графу вперёд
        int[] eft = new int[n];
        Arrays.fill(eft, 0);
        computeEFT(graph, durations, eft, n);

        // Инициализируем поздние сроки окончания (LFT) максимумом по EFT для финальных операций
        int[] lft = new int[n];
        Arrays.fill(lft, Integer.MAX_VALUE);
        int maxEFT = Arrays.stream(eft).max().orElse(0);
        for (int i = 0; i < n; i++) {
            if (isFinal[i]) {
                lft[i] = maxEFT;
            }
        }

        // Вычисляем поздние сроки окончания (LFT) по графу назад
        computeLFT(graph, durations, lft, n);

        // Вычисляем LST (поздний срок начала) для каждой операции
        int[] lst = new int[n];
        for (int i = 0; i < n; i++) {
            lst[i] = lft[i] - durations[i];
        }

        // Вывод результатов
        printAllLST(lst);
        long endTime = System.nanoTime();
        double elapsedTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("\n[RESULT] Time taken to compute LST: %.2f ms\n", elapsedTime);
    }

    // Вывод всех LST для наглядности
    private static void printAllLST(int[] array) {
        System.out.println("\n[RESULT] Full Late Start Times (LST) for all operations:");
        for (int i = 0; i < array.length; i++) {
            System.out.printf("Operation %d: LST = %d\n", i, array[i]);
        }
    }

    // Вычисление ранних сроков завершения (EFT)
    private static void computeEFT(int[][] graph, int[] durations, int[] eft, int n) {
        boolean updated;
        do {
            updated = false;
            for (int i = 0; i < n; i++) {
                int maxPred = 0;
                for (int j = 0; j < n; j++) {
                    if (graph[j][i] > 0) {  // Проверяем, есть ли ребро от j к i
                        maxPred = Math.max(maxPred, eft[j] + graph[j][i]);
                    }
                }
                int newEFT = maxPred + durations[i];
                if (newEFT > eft[i]) {
                    eft[i] = newEFT;
                    updated = true;
                }
            }
        } while (updated); // Повторяем, пока есть изменения
    }

    // Вычисление поздних сроков завершения (LFT) в обратном направлении
    private static void computeLFT(int[][] graph, int[] durations, int[] lft, int n) {
        boolean updated;
        do {
            updated = false;
            for (int i = n - 1; i >= 0; i--) {
                int minSucc = Integer.MAX_VALUE;
                boolean hasSucc = false;
                for (int j = 0; j < n; j++) {
                    if (graph[i][j] > 0) {  // Проверяем, есть ли ребро от i к j
                        int candidate = lft[j] - graph[i][j] - durations[i];
                        minSucc = Math.min(minSucc, candidate);
                        hasSucc = true;
                    }
                }
                if (hasSucc && minSucc < lft[i]) {
                    lft[i] = minSucc;
                    updated = true;
                }
            }
        } while (updated); // Повторяем до сходимости
    }

    // Генерация случайного ацикличного графа
    private static void generateGraph(int[][] graph, int[] durations, boolean[] isFinal, int n, int p) {
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (rand.nextDouble() < (double) p / 100) {
                    graph[i][j] = rand.nextInt(10) + 1; // Вес ребра
                }
            }
        }
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
    }
}