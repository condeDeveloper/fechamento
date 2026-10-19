# Fecho transitivo

Fecho transitivo do zero em Java 21, com o algoritmo de Warshall de 1962 e as três maneiras erradas de escrevê-lo, medidas em **todos** os grafos dirigidos de até quatro vértices.

Todo mundo que já escreveu Floyd-Warshall sabe que o `k` tem que ser o laço de fora. Pouca gente sabe o que acontece se não for. Aqui está: nada, enquanto o grafo tiver dois vértices.

```
mvn -q exec:java -Dexec.args=ordem
```

## O domínio é a lista completa

Com `n` vértices há `n²` arestas possíveis, cada uma presente ou ausente. Um grafo de 4 vértices é um número de 16 bits, e todos os grafos de 4 vértices são os números de 0 a 65.535.

Não é uma amostra de grafos. É a lista deles.

## As medidas

### 1. Três caminhos para o mesmo fecho

| vértices | grafos | erros de Warshall | erros da multiplicação |
|---:|---:|---:|---:|
| 1 | 2 | 0 | 0 |
| 2 | 16 | 0 | 0 |
| 3 | 512 | 0 | 0 |
| 4 | 65.536 | 0 | 0 |

A busca em largura anda pelo grafo. Warshall mexe numa matriz com três laços encaixados. A multiplicação repetida junta a matriz com ela mesma até o comprimento máximo de caminho. Nenhum dos três se parece com o outro, e é isso que faz concordarem significar alguma coisa.

Um detalhe que muda as respostas: aqui o fecho é **transitivo, não reflexivo**. Um vértice só alcança a si mesmo se houver um ciclo passando por ele, nunca de graça.

### 2. O que acontece ao trocar a ordem dos laços

| vértices | grafos | k,i,j | i,j,k | i,k,j | j,k,i |
|---:|---:|---:|---:|---:|---:|
| 1 | 2 | 0 | **0** | **0** | **0** |
| 2 | 16 | 0 | **0** | **0** | **0** |
| 3 | 512 | 0 | 16 (3,1%) | 16 (3,1%) | 16 (3,1%) |
| 4 | 65.536 | 0 | 9.392 (14,3%) | 10.896 (16,6%) | 10.896 (16,6%) |

As duas primeiras linhas são o ponto deste repositório. **Com um ou dois vértices, as três versões erradas acertam tudo.** O bug só nasce no terceiro vértice, e mesmo lá aparece em 3,1% dos casos.

Quem escrever Floyd-Warshall com a ordem errada e testar num grafinho de exemplo vai ver tudo passar.

O menor grafo que denuncia é um ciclo de três:

```
grafo:          fecho certo:    i,j,k devolveu:
. 1 .           1 1 1           . 1 1
. . 1           1 1 1           1 1 1
1 . .           1 1 1           1 1 1
```

Falta uma aresta: o vértice 0 não se alcança. O `k` por dentro significa que, quando o caminho de 0 até 0 passando por 2 ia ser descoberto, a informação de que 2 chega em 0 ainda não tinha sido escrita.

### 3. Quantos grafos já são transitivamente fechados

| vértices | grafos | fechados | tabelado | com laço em todo vértice |
|---:|---:|---:|---:|---:|
| 1 | 2 | 2 | 2 | 1 |
| 2 | 16 | 13 | 13 | 4 |
| 3 | 512 | 171 | 171 | 64 |
| 4 | 65.536 | 3.994 | 3.994 | 4.096 |

Um grafo é fechado quando o fecho não acrescenta nada, o que é o mesmo que a relação de arestas ser transitiva.

A coluna "tabelado" é a sequência conhecida do número de relações transitivas num conjunto de n elementos: 2, 13, 171, 3.994. Ela foi contada por outras pessoas, por outros caminhos, e serve de segunda testemunha sobre a varredura inteira.

### 4. Quantas vezes o corpo do laço mais interno roda

| vértices | grafos | passos ao todo | por grafo | n³ |
|---:|---:|---:|---:|---:|
| 1 | 2 | 2 | 1 | 1 |
| 2 | 16 | 128 | 8 | 8 |
| 3 | 512 | 13.824 | 27 | 27 |
| 4 | 65.536 | 4.194.304 | 64 | 64 |

Warshall não tem caso bom nem caso ruim. Ele faz exatamente o mesmo trabalho num grafo vazio e num grafo completo, e a coluna "por grafo" é `n³` nos quatro tamanhos, sem variação nenhuma.

Isso é uma virtude e um defeito na mesma frase: previsível de medir, impossível de acelerar com um grafo fácil.

### 5. Quanto o fecho acrescenta

```
grafos de 4 vértices varridos        65.536
média de arestas ANTES do fecho        8,00
média de arestas DEPOIS               12,41
grafos que ganharam aresta           61.542 (93,9%)
maior crescimento num grafo só           12 arestas
```

A média de antes é exatamente metade das 16 arestas possíveis, e não por acaso: cada aresta está presente em metade dos grafos.

Depois do fecho, 93,9% dos grafos cresceram. Fechar quase nunca é de graça, e num caso o fecho acrescentou 12 arestas a um grafo que tinha poucas.

## O que as medidas me corrigiram

**"Testei e funcionou" não cobre o bug mais famoso desse algoritmo.** Eu esperava que a ordem errada falhasse em alguma fração constante dos casos. Ela falha em **zero** enquanto o grafo tiver até dois vértices, e é exatamente por isso que esse erro atravessa revisão de código: o exemplo pequeno que a pessoa usa para conferir não tem como mostrar o problema.

**A ordem dos laços não é convenção, é a invariante.** A frase "k por fora" costuma ser ensinada como regra a decorar. O que ela diz é que, ao fim da volta k, a matriz contém os caminhos que passam só por vértices até k. Trocar os laços não troca a ordem de uma conta: destrói o significado do que está na matriz no meio do caminho.

**Uma contagem conhecida vale mais que um teste meu.** Os 2, 13, 171 e 3.994 grafos fechados batem com uma sequência tabelada há décadas. Isso testa a varredura inteira de uma vez, de um jeito que nenhum teste que eu escrevesse conseguiria, porque eu escreveria o teste com a mesma cabeça com que escrevi o código.

## Rodar

```bash
mvn -q verify                              # 16 testes
mvn -q exec:java                           # todas as medidas, menos de um segundo
mvn -q exec:java -Dexec.args=correcao      # 1
mvn -q exec:java -Dexec.args=ordem         # 2
mvn -q exec:java -Dexec.args=fechados      # 3
mvn -q exec:java -Dexec.args=custo         # 4
mvn -q exec:java -Dexec.args=densidade     # 5
```

Java 21, sem dependência fora do JUnit. Nenhum número deste README foi digitado à mão: todos saem de `Medidas`, e os testes conferem os que aparecem escritos aqui.
