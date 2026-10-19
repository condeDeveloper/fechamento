package br.com.conde.fechamento;

/**
 * UM GRAFO DIRIGIDO, guardado como uma matriz de bits dentro de um inteiro.
 *
 * <p>Com {@code n} vértices há {@code n²} arestas possíveis, cada uma presente ou ausente.
 * Então um grafo de 4 vértices é um número de 16 bits, e **todos** os grafos de 4 vértices são
 * os números de 0 a 65.535. Essa é a frase que sustenta o repositório inteiro: o domínio não é
 * uma amostra de grafos, é a lista completa deles.
 *
 * <p>A aresta de {@code a} para {@code b} é o bit {@code a*n + b}. A diagonal faz parte: o laço
 * de um vértice nele mesmo é uma aresta como outra qualquer, e incluí-la ou não muda a resposta
 * do fecho, o que é assunto da terceira medida.
 */
public record Grafo(int vertices, int arestas) {

    public Grafo {
        if (vertices < 1 || vertices > 5)
            throw new IllegalArgumentException("vertices entre 1 e 5: " + vertices);
    }

    /** Quantos grafos dirigidos existem com este tanto de vértices: dois elevado a n². */
    public static int quantos(int vertices) {
        return 1 << (vertices * vertices);
    }

    public boolean tem(int de, int para) {
        return (arestas & (1 << (de * vertices + para))) != 0;
    }

    public Grafo com(int de, int para) {
        return new Grafo(vertices, arestas | (1 << (de * vertices + para)));
    }

    public Grafo sem(int de, int para) {
        return new Grafo(vertices, arestas & ~(1 << (de * vertices + para)));
    }

    public int quantasArestas() {
        return Integer.bitCount(arestas);
    }

    /**
     * O FECHO TRANSITIVO PELA DEFINIÇÃO: existe caminho de a para b?
     *
     * <p>Uma busca em largura a partir de cada vértice, sem nenhuma esperteza. É o juiz deste
     * repositório, e é juiz porque não tem a mesma forma do algoritmo que julga: Warshall mexe
     * numa matriz por três laços encaixados, e isto aqui anda pelo grafo.
     *
     * <p>Um detalhe que muda tudo: aqui o caminho tem comprimento pelo menos 1. O vértice só
     * alcança a si mesmo se houver um ciclo passando por ele, e não de graça. É o fecho
     * transitivo, não o reflexivo e transitivo.
     */
    public Grafo fechoPorBusca() {
        int resultado = 0;

        for (int origem = 0; origem < vertices; origem++) {
            boolean[] visto = new boolean[vertices];

            java.util.ArrayDeque<Integer> fila = new java.util.ArrayDeque<>();

            /* Comeca pelos vizinhos diretos, nao pela origem: um passo ja foi dado. */
            for (int destino = 0; destino < vertices; destino++)
                if (tem(origem, destino) && !visto[destino]) {
                    visto[destino] = true;
                    fila.add(destino);
                }

            while (!fila.isEmpty()) {
                int atual = fila.poll();

                for (int destino = 0; destino < vertices; destino++)
                    if (tem(atual, destino) && !visto[destino]) {
                        visto[destino] = true;
                        fila.add(destino);
                    }
            }

            for (int destino = 0; destino < vertices; destino++)
                if (visto[destino]) resultado |= 1 << (origem * vertices + destino);
        }

        return new Grafo(vertices, resultado);
    }

    /** O grafo já é transitivamente fechado: tem toda aresta que o fecho teria. */
    public boolean fechado() {
        return fechoPorBusca().arestas() == arestas;
    }

    /** Toda aresta do fecho já estava aqui, e nenhuma a mais. */
    public boolean reflexivo() {
        for (int vertice = 0; vertice < vertices; vertice++)
            if (!tem(vertice, vertice)) return false;

        return true;
    }

    public String mostrar() {
        StringBuilder saida = new StringBuilder();

        for (int de = 0; de < vertices; de++) {
            for (int para = 0; para < vertices; para++) saida.append(tem(de, para) ? '1' : '.');

            saida.append('\n');
        }

        return saida.toString();
    }

    @Override
    public String toString() {
        return "grafo " + arestas + " de " + vertices + " vertices";
    }
}
