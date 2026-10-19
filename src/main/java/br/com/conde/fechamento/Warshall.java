package br.com.conde.fechamento;

/**
 * O ALGORITMO DE WARSHALL, de 1962, e as três maneiras erradas de escrevê-lo.
 *
 * <p>São três laços encaixados sobre os mesmos índices, e a versão certa é:
 *
 * <pre>
 *   para cada k:
 *     para cada i:
 *       para cada j:
 *         se liga[i][k] e liga[k][j], entao liga[i][j] = verdadeiro
 *   </pre>
 *
 * <p>O {@code k} precisa ser o laço de FORA. O motivo é a invariante: ao terminar a volta de
 * número {@code k}, a matriz contém exatamente os caminhos que usam como passagem apenas
 * vértices de 0 até {@code k}. Cada volta amplia o conjunto de passagens permitidas em um
 * vértice, e por isso a ordem não é convenção: é o que faz a conta fechar.
 *
 * <p>Trocar a ordem dos laços produz um programa que compila, roda, devolve uma matriz
 * plausível, acerta em muitos casos e erra em outros. Esta classe implementa as três trocas
 * de propósito, para a segunda medida contar em quantos grafos cada uma erra.
 */
public final class Warshall {

    private Warshall() {}

    /** Quantas vezes o corpo do laço mais interno rodou, para a medida de custo. */
    public static long passos;

    /** A ordem certa: k por fora, depois i, depois j. */
    public static Grafo certo(Grafo grafo) {
        return rodar(grafo, Ordem.K_I_J);
    }

    public enum Ordem {
        /** A certa. */
        K_I_J,
        /** i por fora: parece natural e está errada. */
        I_J_K,
        /** i por fora, k no meio. */
        I_K_J,
        /** j por fora. */
        J_K_I
    }

    public static Grafo rodar(Grafo grafo, Ordem ordem) {
        int n = grafo.vertices();

        boolean[][] liga = new boolean[n][n];

        for (int de = 0; de < n; de++)
            for (int para = 0; para < n; para++) liga[de][para] = grafo.tem(de, para);

        switch (ordem) {
            case K_I_J -> {
                for (int k = 0; k < n; k++)
                    for (int i = 0; i < n; i++)
                        for (int j = 0; j < n; j++) juntar(liga, i, j, k);
            }

            case I_J_K -> {
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < n; j++)
                        for (int k = 0; k < n; k++) juntar(liga, i, j, k);
            }

            case I_K_J -> {
                for (int i = 0; i < n; i++)
                    for (int k = 0; k < n; k++)
                        for (int j = 0; j < n; j++) juntar(liga, i, j, k);
            }

            case J_K_I -> {
                for (int j = 0; j < n; j++)
                    for (int k = 0; k < n; k++)
                        for (int i = 0; i < n; i++) juntar(liga, i, j, k);
            }
        }

        int saida = 0;

        for (int de = 0; de < n; de++)
            for (int para = 0; para < n; para++)
                if (liga[de][para]) saida |= 1 << (de * n + para);

        return new Grafo(n, saida);
    }

    private static void juntar(boolean[][] liga, int i, int j, int k) {
        passos++;

        if (liga[i][k] && liga[k][j]) liga[i][j] = true;
    }

    /**
     * O FECHO POR MULTIPLICAÇÃO REPETIDA, que é outro caminho inteiro.
     *
     * <p>Caminhos de comprimento até 1 são as próprias arestas. Caminhos de comprimento até 2
     * saem de juntar a matriz com ela mesma, e assim por diante. Com n vértices, nenhum caminho
     * sem repetição passa de n arestas, então repetir n vezes basta.
     *
     * <p>Serve de terceira opinião: ele não tem a forma de Warshall nem a forma da busca.
     */
    public static Grafo porMultiplicacao(Grafo grafo) {
        int n = grafo.vertices();

        boolean[][] atual = new boolean[n][n];

        for (int de = 0; de < n; de++)
            for (int para = 0; para < n; para++) atual[de][para] = grafo.tem(de, para);

        boolean[][] acumulado = new boolean[n][n];

        for (int de = 0; de < n; de++) acumulado[de] = atual[de].clone();

        for (int passo = 2; passo <= n; passo++) {
            boolean[][] proximo = new boolean[n][n];

            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    for (int meio = 0; meio < n; meio++)
                        if (atual[i][meio] && grafo.tem(meio, j)) {
                            proximo[i][j] = true;
                            break;
                        }

            atual = proximo;

            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++) acumulado[i][j] |= atual[i][j];
        }

        int saida = 0;

        for (int de = 0; de < n; de++)
            for (int para = 0; para < n; para++)
                if (acumulado[de][para]) saida |= 1 << (de * n + para);

        return new Grafo(n, saida);
    }

    public static void zerarPassos() {
        passos = 0;
    }
}
