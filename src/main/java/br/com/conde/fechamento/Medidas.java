package br.com.conde.fechamento;

import java.util.ArrayList;
import java.util.List;

/**
 * AS MEDIDAS: tudo o que o repositório afirma sai de varrer TODOS os grafos dirigidos de até
 * quatro vértices, que são 65.536, e não uma amostra deles.
 */
public final class Medidas {

    private Medidas() {}

    public static final int ATE = 4;

    /* ===================== 1. correcao ===================== */

    public record Correcao(int vertices, int grafos, int errosDoWarshall,
            int errosDaMultiplicacao) {}

    private static List<Correcao> correcao;

    /**
     * TRÊS CAMINHOS PARA O MESMO FECHO.
     *
     * <p>A busca em largura anda pelo grafo. Warshall mexe numa matriz com três laços
     * encaixados. A multiplicação repetida junta a matriz com ela mesma até o comprimento
     * máximo de caminho. Nenhum dos três se parece com os outros, e é isso que faz a
     * concordância significar alguma coisa.
     */
    public static List<Correcao> correcao() {
        if (correcao != null) return correcao;

        List<Correcao> saida = new ArrayList<>();

        for (int vertices = 1; vertices <= ATE; vertices++) {
            int grafos = Grafo.quantos(vertices);

            int errosWarshall = 0;
            int errosMultiplicacao = 0;

            for (int bits = 0; bits < grafos; bits++) {
                Grafo grafo = new Grafo(vertices, bits);

                int juiz = grafo.fechoPorBusca().arestas();

                if (Warshall.certo(grafo).arestas() != juiz) errosWarshall++;

                if (Warshall.porMultiplicacao(grafo).arestas() != juiz) errosMultiplicacao++;
            }

            saida.add(new Correcao(vertices, grafos, errosWarshall, errosMultiplicacao));
        }

        return correcao = List.copyOf(saida);
    }

    public static Correcao correcao(int vertices) {
        return correcao().stream().filter(x -> x.vertices() == vertices)
                .findFirst().orElseThrow();
    }

    /* ===================== 2. ordem ===================== */

    public record Troca(int vertices, Warshall.Ordem ordem, int grafos, int erros) {

        public double fracao() {
            return erros / (double) grafos;
        }
    }

    private static List<Troca> ordem;

    /**
     * O QUE ACONTECE AO TROCAR A ORDEM DOS LAÇOS.
     *
     * <p>O {@code k} precisa ser o laço de fora, porque a invariante é "caminhos que passam só
     * por vértices até k", e cada volta amplia esse conjunto em um vértice. Trocar a ordem
     * produz um programa que compila, roda e devolve uma matriz plausível.
     *
     * <p>A tabela mostra o detalhe que torna esse bug caro: ele é INVISÍVEL em grafo pequeno.
     * Com um ou dois vértices as três versões erradas acertam tudo.
     */
    public static List<Troca> ordem() {
        if (ordem != null) return ordem;

        List<Troca> saida = new ArrayList<>();

        for (int vertices = 1; vertices <= ATE; vertices++) {
            int grafos = Grafo.quantos(vertices);

            for (Warshall.Ordem qual : Warshall.Ordem.values()) {
                int erros = 0;

                for (int bits = 0; bits < grafos; bits++) {
                    Grafo grafo = new Grafo(vertices, bits);

                    if (Warshall.rodar(grafo, qual).arestas() != grafo.fechoPorBusca().arestas())
                        erros++;
                }

                saida.add(new Troca(vertices, qual, grafos, erros));
            }
        }

        return ordem = List.copyOf(saida);
    }

    public static Troca ordem(int vertices, Warshall.Ordem qual) {
        return ordem().stream()
                .filter(x -> x.vertices() == vertices && x.ordem() == qual)
                .findFirst().orElseThrow();
    }

    /** O menor grafo em que a ordem errada erra, para quem quiser ver com os olhos. */
    public static Grafo menorContraexemplo(Warshall.Ordem qual) {
        for (int vertices = 1; vertices <= ATE; vertices++)
            for (int bits = 0; bits < Grafo.quantos(vertices); bits++) {
                Grafo grafo = new Grafo(vertices, bits);

                if (Warshall.rodar(grafo, qual).arestas() != grafo.fechoPorBusca().arestas())
                    return grafo;
            }

        return null;
    }

    /* ===================== 3. fechados ===================== */

    public record Fechados(int vertices, int grafos, int fechados, int reflexivos) {}

    private static List<Fechados> fechados;

    /**
     * QUANTOS GRAFOS JÁ SÃO TRANSITIVAMENTE FECHADOS.
     *
     * <p>Um grafo é fechado quando o fecho não acrescenta nada: ele já tem toda aresta que
     * deveria. Isso é o mesmo que a relação de arestas ser transitiva.
     *
     * <p>A contagem dá 2, 13, 171 e 3.994, que é uma sequência conhecida e tabelada: o número
     * de relações transitivas num conjunto de n elementos. Ela serve de segunda testemunha
     * sobre a varredura inteira, porque foi contada por outras pessoas, por outros caminhos.
     */
    public static List<Fechados> fechados() {
        if (fechados != null) return fechados;

        List<Fechados> saida = new ArrayList<>();

        for (int vertices = 1; vertices <= ATE; vertices++) {
            int grafos = Grafo.quantos(vertices);

            int quantos = 0;
            int comLaco = 0;

            for (int bits = 0; bits < grafos; bits++) {
                Grafo grafo = new Grafo(vertices, bits);

                if (grafo.fechado()) quantos++;

                if (grafo.reflexivo()) comLaco++;
            }

            saida.add(new Fechados(vertices, grafos, quantos, comLaco));
        }

        return fechados = List.copyOf(saida);
    }

    public static Fechados fechados(int vertices) {
        return fechados().stream().filter(x -> x.vertices() == vertices)
                .findFirst().orElseThrow();
    }

    /** A sequência tabelada das relações transitivas, para comparar. */
    public static int[] tabeladas() {
        return new int[] {2, 13, 171, 3994};
    }

    /* ===================== 4. custo ===================== */

    public record Custo(int vertices, int grafos, long passos, long porGrafo, long nAoCubo) {}

    private static List<Custo> custo;

    /**
     * QUANTAS VEZES O CORPO DO LAÇO MAIS INTERNO RODA.
     *
     * <p>Warshall é famoso por ser {@code n³} sem exceção: ele não tem caso bom nem caso ruim,
     * não tem saída antecipada, e faz o mesmo trabalho num grafo vazio e num grafo completo.
     * A tabela confirma isso contando, e a última coluna é a conta de cabeça.
     */
    public static List<Custo> custo() {
        if (custo != null) return custo;

        List<Custo> saida = new ArrayList<>();

        for (int vertices = 1; vertices <= ATE; vertices++) {
            int grafos = Grafo.quantos(vertices);

            Warshall.zerarPassos();

            for (int bits = 0; bits < grafos; bits++) Warshall.certo(new Grafo(vertices, bits));

            long cubo = (long) vertices * vertices * vertices;

            saida.add(new Custo(vertices, grafos, Warshall.passos, Warshall.passos / grafos,
                    cubo));
        }

        return custo = List.copyOf(saida);
    }

    public static Custo custo(int vertices) {
        return custo().stream().filter(x -> x.vertices() == vertices).findFirst().orElseThrow();
    }

    /* ===================== 5. densidade ===================== */

    public record Densidade(int vertices, int grafos, double arestasAntes, double arestasDepois,
            int cresceram, int maiorCrescimento) {

        public double fracaoQueCresce() {
            return cresceram / (double) grafos;
        }
    }

    private static Densidade densidade;

    /**
     * QUANTO O FECHO ACRESCENTA.
     *
     * <p>A média de arestas antes é exatamente metade das possíveis, porque todo grafo aparece
     * uma vez e cada aresta está presente em metade deles. Depois do fecho a média sobe, e a
     * coluna que interessa é quantos grafos crescem: quase todos.
     */
    public static Densidade densidade() {
        if (densidade != null) return densidade;

        int grafos = Grafo.quantos(ATE);

        long antes = 0;
        long depois = 0;

        int cresceram = 0;
        int maior = 0;

        for (int bits = 0; bits < grafos; bits++) {
            Grafo grafo = new Grafo(ATE, bits);

            int temAntes = grafo.quantasArestas();
            int temDepois = grafo.fechoPorBusca().quantasArestas();

            antes += temAntes;
            depois += temDepois;

            if (temDepois > temAntes) cresceram++;

            maior = Math.max(maior, temDepois - temAntes);
        }

        return densidade = new Densidade(ATE, grafos, antes / (double) grafos,
                depois / (double) grafos, cresceram, maior);
    }
}
