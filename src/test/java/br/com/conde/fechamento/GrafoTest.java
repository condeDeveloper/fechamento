package br.com.conde.fechamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O grafo e o juiz.
 *
 * <p>A busca em largura é o juiz de tudo neste repositório, então ela é julgada primeiro, e
 * pelo caso que mais engana: o fecho aqui é TRANSITIVO, não reflexivo. Um vértice só alcança
 * a si mesmo se houver ciclo passando por ele.
 */
class GrafoTest {

    @Test
    @DisplayName("sao dois elevado a n ao quadrado grafos")
    void quantosGrafos() {
        assertEquals(2, Grafo.quantos(1));
        assertEquals(16, Grafo.quantos(2));
        assertEquals(512, Grafo.quantos(3));
        assertEquals(65536, Grafo.quantos(4));
    }

    @Test
    @DisplayName("fora da faixa de vertices nao existe grafo")
    void foraDaFaixa() {
        assertThrows(IllegalArgumentException.class, () -> new Grafo(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Grafo(6, 0));
    }

    @Test
    @DisplayName("por e tirar aresta mexe so naquela aresta")
    void mexerNumaArestaSo() {
        Grafo vazio = new Grafo(3, 0);

        Grafo comUma = vazio.com(0, 2);

        assertTrue(comUma.tem(0, 2));
        assertEquals(1, comUma.quantasArestas());

        for (int de = 0; de < 3; de++)
            for (int para = 0; para < 3; para++)
                if (de != 0 || para != 2) assertFalse(comUma.tem(de, para), de + "->" + para);

        assertEquals(vazio, comUma.sem(0, 2));
    }

    @Test
    @DisplayName("O FECHO AQUI E TRANSITIVO, NAO REFLEXIVO: sem ciclo, ninguem se alcanca")
    void fechoNaoEhReflexivo() {
        /* Caminho 0 -> 1 -> 2, sem volta. */
        Grafo caminho = new Grafo(3, 0).com(0, 1).com(1, 2);

        Grafo fecho = caminho.fechoPorBusca();

        assertTrue(fecho.tem(0, 2), "o fecho acrescenta o atalho");
        assertTrue(fecho.tem(0, 1));
        assertTrue(fecho.tem(1, 2));

        for (int vertice = 0; vertice < 3; vertice++)
            assertFalse(fecho.tem(vertice, vertice),
                    "vertice " + vertice + " nao devia se alcancar");
    }

    @Test
    @DisplayName("mas com ciclo todo mundo se alcanca, inclusive a si mesmo")
    void comCicloTodosSeAlcancam() {
        Grafo ciclo = new Grafo(3, 0).com(0, 1).com(1, 2).com(2, 0);

        Grafo fecho = ciclo.fechoPorBusca();

        for (int de = 0; de < 3; de++)
            for (int para = 0; para < 3; para++)
                assertTrue(fecho.tem(de, para), de + "->" + para);

        assertEquals(9, fecho.quantasArestas());
    }

    @Test
    @DisplayName("o grafo vazio tem fecho vazio, e o completo e seu proprio fecho")
    void extremos() {
        Grafo vazio = new Grafo(4, 0);

        assertEquals(0, vazio.fechoPorBusca().quantasArestas());
        assertTrue(vazio.fechado());

        Grafo completo = new Grafo(4, Grafo.quantos(4) - 1);

        assertEquals(16, completo.quantasArestas());
        assertTrue(completo.fechado());
        assertTrue(completo.reflexivo());
    }

    @Test
    @DisplayName("O FECHO E IDEMPOTENTE: fechar o fecho nao muda nada, nos 65.536")
    void fecharDuasVezes() {
        for (int bits = 0; bits < Grafo.quantos(4); bits++) {
            Grafo fecho = new Grafo(4, bits).fechoPorBusca();

            assertEquals(fecho.arestas(), fecho.fechoPorBusca().arestas(), "grafo " + bits);
            assertTrue(fecho.fechado(), "grafo " + bits);
        }
    }

    @Test
    @DisplayName("o fecho nunca TIRA aresta, so acrescenta")
    void fechoSoAcrescenta() {
        for (int bits = 0; bits < Grafo.quantos(4); bits++) {
            Grafo grafo = new Grafo(4, bits);
            Grafo fecho = grafo.fechoPorBusca();

            assertEquals(grafo.arestas(), grafo.arestas() & fecho.arestas(),
                    "grafo " + bits + " perdeu aresta");
        }
    }
}
