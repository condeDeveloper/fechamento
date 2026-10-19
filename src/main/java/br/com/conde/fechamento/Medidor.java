package br.com.conde.fechamento;

import java.util.Locale;

/** O MEDIDOR: roda as varreduras e imprime as tabelas do README. */
public final class Medidor {

    private Medidor() {}

    public static void main(String[] argumentos) {
        Locale.setDefault(Locale.US);

        String passo = argumentos.length > 0 ? argumentos[0] : "tudo";

        switch (passo) {
            case "correcao" -> correcao();
            case "ordem" -> ordem();
            case "fechados" -> fechados();
            case "custo" -> custo();
            case "densidade" -> densidade();
            default -> {
                correcao();
                ordem();
                fechados();
                custo();
                densidade();
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("=".repeat(texto.length()));
        System.out.println();
    }

    static void correcao() {
        titulo("1. tres caminhos para o mesmo fecho");

        System.out.println("vertices  grafos  erros de warshall  erros da multiplicacao");

        for (Medidas.Correcao linha : Medidas.correcao())
            System.out.printf("%8d  %6d  %17d  %22d%n", linha.vertices(), linha.grafos(),
                    linha.errosDoWarshall(), linha.errosDaMultiplicacao());

        System.out.println();
        System.out.println("a busca anda pelo grafo, warshall mexe numa matriz, a multiplicacao");
        System.out.println("junta a matriz com ela mesma. nenhum se parece com o outro.");
    }

    static void ordem() {
        titulo("2. o que acontece ao trocar a ordem dos lacos");

        System.out.println("vertices  grafos  K,I,J   I,J,K           I,K,J           J,K,I");

        for (int v = 1; v <= Medidas.ATE; v++) {
            System.out.printf("%8d  %6d  %5d  %6d (%4.1f%%)  %6d (%4.1f%%)  %6d (%4.1f%%)%n", v,
                    Grafo.quantos(v),
                    Medidas.ordem(v, Warshall.Ordem.K_I_J).erros(),
                    Medidas.ordem(v, Warshall.Ordem.I_J_K).erros(),
                    100 * Medidas.ordem(v, Warshall.Ordem.I_J_K).fracao(),
                    Medidas.ordem(v, Warshall.Ordem.I_K_J).erros(),
                    100 * Medidas.ordem(v, Warshall.Ordem.I_K_J).fracao(),
                    Medidas.ordem(v, Warshall.Ordem.J_K_I).erros(),
                    100 * Medidas.ordem(v, Warshall.Ordem.J_K_I).fracao());
        }

        System.out.println();
        System.out.println("com UM ou DOIS vertices as tres versoes erradas acertam TUDO.");
        System.out.println("o bug so aparece a partir de tres, e em 3,1% dos casos.");

        Grafo menor = Medidas.menorContraexemplo(Warshall.Ordem.I_J_K);

        System.out.println();
        System.out.println("o menor grafo em que i,j,k erra e um ciclo de tres:");
        System.out.println();
        System.out.print(menor.mostrar());
        System.out.println("o fecho certo liga todo mundo a todo mundo:");
        System.out.println();
        System.out.print(menor.fechoPorBusca().mostrar());
        System.out.println("i,j,k devolveu:");
        System.out.println();
        System.out.print(Warshall.rodar(menor, Warshall.Ordem.I_J_K).mostrar());
    }

    static void fechados() {
        titulo("3. quantos grafos ja sao transitivamente fechados");

        System.out.println("vertices  grafos  fechados  tabelado  com laco em todo vertice");

        int[] tabeladas = Medidas.tabeladas();

        for (Medidas.Fechados linha : Medidas.fechados())
            System.out.printf("%8d  %6d  %8d  %8d  %24d%n", linha.vertices(), linha.grafos(),
                    linha.fechados(), tabeladas[linha.vertices() - 1], linha.reflexivos());

        System.out.println();
        System.out.println("2, 13, 171 e 3.994 e a sequencia conhecida do numero de relacoes");
        System.out.println("transitivas num conjunto de n elementos. ela foi contada por outras");
        System.out.println("pessoas, por outros caminhos, e serve de segunda testemunha.");
    }

    static void custo() {
        titulo("4. quantas vezes o corpo do laco mais interno roda");

        System.out.println("vertices  grafos  passos ao todo  por grafo  n ao cubo");

        for (Medidas.Custo linha : Medidas.custo())
            System.out.printf("%8d  %6d  %14d  %9d  %9d%n", linha.vertices(), linha.grafos(),
                    linha.passos(), linha.porGrafo(), linha.nAoCubo());

        System.out.println();
        System.out.println("warshall nao tem caso bom nem caso ruim: ele faz o mesmo trabalho");
        System.out.println("num grafo vazio e num grafo completo, e a coluna por grafo e n^3.");
    }

    static void densidade() {
        titulo("5. quanto o fecho acrescenta");

        Medidas.Densidade d = Medidas.densidade();

        System.out.printf("grafos de %d vertices varridos        %d%n", d.vertices(), d.grafos());
        System.out.printf("media de arestas ANTES do fecho       %.2f%n", d.arestasAntes());
        System.out.printf("media de arestas DEPOIS               %.2f%n", d.arestasDepois());
        System.out.printf("grafos que ganharam aresta            %d (%.1f%%)%n", d.cresceram(),
                100 * d.fracaoQueCresce());
        System.out.printf("maior crescimento num grafo so        %d arestas%n",
                d.maiorCrescimento());

        System.out.println();
        System.out.println("a media de antes e exatamente metade das arestas possiveis, porque");
        System.out.println("cada aresta esta presente em metade dos grafos. depois do fecho,");
        System.out.println("quase todo grafo cresce: fechar quase nunca e de graca.");
    }
}
