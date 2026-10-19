package br.com.conde.fechamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Os números que aparecem escritos no README, travados em teste. */
class MedidasTest {

    @Test
    @DisplayName("warshall certo e a multiplicacao acertam os 65.536, sem excecao")
    void correcao() {
        for (Medidas.Correcao linha : Medidas.correcao()) {
            assertEquals(0, linha.errosDoWarshall(), linha.vertices() + " vertices");
            assertEquals(0, linha.errosDaMultiplicacao(), linha.vertices() + " vertices");
        }

        assertEquals(65536, Medidas.correcao(4).grafos());
    }

    @Test
    @DisplayName("A ORDEM ERRADA NAO ERRA NUNCA com um ou dois vertices")
    void bugInvisivelEmGrafoPequeno() {
        for (int vertices = 1; vertices <= 2; vertices++)
            for (Warshall.Ordem qual : Warshall.Ordem.values())
                assertEquals(0, Medidas.ordem(vertices, qual).erros(),
                        qual + " com " + vertices + " vertices");
    }

    @Test
    @DisplayName("e so aparece a partir de tres: 3,1% la, 14,3% em quatro")
    void oBugAparece() {
        assertEquals(16, Medidas.ordem(3, Warshall.Ordem.I_J_K).erros());
        assertEquals(9392, Medidas.ordem(4, Warshall.Ordem.I_J_K).erros());
        assertEquals(10896, Medidas.ordem(4, Warshall.Ordem.I_K_J).erros());
        assertEquals(10896, Medidas.ordem(4, Warshall.Ordem.J_K_I).erros());

        assertEquals(0.031, Medidas.ordem(3, Warshall.Ordem.I_J_K).fracao(), 0.001);
        assertEquals(0.143, Medidas.ordem(4, Warshall.Ordem.I_J_K).fracao(), 0.001);

        /* A ordem certa nao erra em tamanho nenhum. */
        for (int vertices = 1; vertices <= Medidas.ATE; vertices++)
            assertEquals(0, Medidas.ordem(vertices, Warshall.Ordem.K_I_J).erros(),
                    vertices + " vertices");
    }

    @Test
    @DisplayName("o menor contraexemplo e um ciclo de tres vertices")
    void menorContraexemplo() {
        Grafo menor = Medidas.menorContraexemplo(Warshall.Ordem.I_J_K);

        assertEquals(3, menor.vertices());
        assertEquals(3, menor.quantasArestas());

        /* Ciclo: todo vertice com uma saida e uma entrada. */
        for (int vertice = 0; vertice < 3; vertice++) {
            int saindo = 0;
            int entrando = 0;

            for (int outro = 0; outro < 3; outro++) {
                if (menor.tem(vertice, outro)) saindo++;
                if (menor.tem(outro, vertice)) entrando++;
            }

            assertEquals(1, saindo, "vertice " + vertice);
            assertEquals(1, entrando, "vertice " + vertice);
        }

        assertEquals(9, menor.fechoPorBusca().quantasArestas());
        assertTrue(Warshall.rodar(menor, Warshall.Ordem.I_J_K).quantasArestas() < 9);
    }

    @Test
    @DisplayName("OS FECHADOS BATEM com a sequencia tabelada: 2, 13, 171, 3.994")
    void fechadosBatemComATabela() {
        int[] tabeladas = Medidas.tabeladas();

        for (Medidas.Fechados linha : Medidas.fechados())
            assertEquals(tabeladas[linha.vertices() - 1], linha.fechados(),
                    linha.vertices() + " vertices");

        assertEquals(3994, Medidas.fechados(4).fechados());
    }

    @Test
    @DisplayName("os reflexivos sao dois elevado a n ao quadrado menos n")
    void reflexivos() {
        for (Medidas.Fechados linha : Medidas.fechados()) {
            int n = linha.vertices();

            assertEquals(1 << (n * n - n), linha.reflexivos(), n + " vertices");
        }
    }

    @Test
    @DisplayName("warshall faz n ao cubo passos por grafo, sem caso bom nem ruim")
    void custoFixo() {
        for (Medidas.Custo linha : Medidas.custo())
            assertEquals(linha.nAoCubo(), linha.porGrafo(), linha.vertices() + " vertices");

        assertEquals(4_194_304, Medidas.custo(4).passos());
        assertEquals(64, Medidas.custo(4).porGrafo());
    }

    @Test
    @DisplayName("quase todo grafo cresce ao fechar: 93,9%")
    void densidade() {
        Medidas.Densidade d = Medidas.densidade();

        assertEquals(65536, d.grafos());
        assertEquals(8.0, d.arestasAntes(), 1e-9);
        assertEquals(12.41, d.arestasDepois(), 0.01);
        assertEquals(61542, d.cresceram());

        assertTrue(d.fracaoQueCresce() > 0.93);

        /* A media de antes e metade das arestas possiveis, exata. */
        assertEquals(16 / 2.0, d.arestasAntes(), 1e-9);
    }
}
