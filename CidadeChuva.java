// Samuell Félix - RM 571589
import java.util.Random;
import java.util.Scanner;

public class CidadeChuva {
    public static void main(String[] args) {
        // Primeiro pego o tamanho da equipe e os RMs.
        Scanner entrada = new Scanner(System.in);
        int qtd;
        do {
            System.out.println("Quantidade de integrantes (1 a 3):");
            qtd = entrada.nextInt();
        } while (qtd < 1 || qtd > 3);

        int[] rms = new int[qtd];
        for (int i = 0; i < qtd; i++) {
            System.out.println("RM do integrante " + (i + 1) + ":");
            rms[i] = entrada.nextInt();
        }

        int rm = calcularRmEquipe(rms);
        int[] parametros = calcularParametros(rm);
        int n = parametros[0];
        int m = parametros[1];
        int k = parametros[2];
        int limiar = parametros[3];
        int ordem = parametros[4];

        System.out.println("RM da equipe: " + rm);
        System.out.println("N=" + n + " M=" + m + " K=" + k + " L=" + limiar + " ordem=" + ordem);

        Random gerador = new Random(rm);
        int[][] altitude = gerarAltitude(gerador, n, m);
        int[][] lamina = gerarMatriz(gerador, n, m, 0, 10);
        int[] deslocamentoLinha = criarDeslocamentoLinha(ordem);
        int[] deslocamentoColuna = criarDeslocamentoColuna(ordem);

        imprimir(altitude, "Altitude (cm):");
        imprimir(lamina, "Lâmina inicial (cm):");

        int[][] laminaInicial = copiar(lamina);
        int[][] laminaParaD1 = copiar(lamina);
        int aguaInicial = somar(lamina);
        System.out.println("Água total no início: " + aguaInicial + " cm");

        for (int passo = 1; passo <= k; passo++) {
            int aguaAntes = somar(lamina);
            lamina = simularPasso(altitude, lamina, deslocamentoLinha, deslocamentoColuna);
            int aguaDepois = somar(lamina);
            System.out.println("Passo " + passo + " - água antes: " + aguaAntes + " cm; água depois: " + aguaDepois + " cm");
            imprimir(lamina, "Lâmina após o passo " + passo + " (cm):");

            boolean encontrouAlerta = false;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (lamina[i][j] >= limiar) {
                        System.out.println("ALERTA no ciclo " + passo + ": célula (" + i + "," + j + ") com " + lamina[i][j] + " cm");
                        encontrouAlerta = true;
                    }
                }
            }
            if (!encontrouAlerta) {
                System.out.println("Nenhuma célula em alerta no ciclo " + passo + ".");
            }

            if (passo == 3) {
                laminaParaD1 = copiar(lamina);
            }
        }

        int aguaFinal = somar(lamina);
        System.out.println("Água total no fim: " + aguaFinal + " cm");
        boolean[][] alerta = marcarAlerta(lamina, limiar);
        int totalAlerta = contarAlerta(alerta);
        System.out.println("Células em alerta: " + totalAlerta);

        imprimirMapaAlerta(alerta);
        int[] regioes = analisarRegioes(alerta);
        System.out.println("Regiões alagadas: " + regioes[0]);
        System.out.println("Maior região: " + regioes[1] + " células");
        int[] aguaPorLinha = calcularAguaPorLinha(lamina);
        System.out.println("Água por linha (cm):");
        for (int i = 0; i < aguaPorLinha.length; i++) {
            System.out.println("Linha " + i + ": " + aguaPorLinha[i]);
        }
        int linhaCritica = encontrarLinhaCritica(aguaPorLinha);
        System.out.println("Linha crítica: " + linhaCritica + " (" + aguaPorLinha[linhaCritica] + " cm)");

        int[][] laminaIngenua = copiar(laminaInicial);
        for (int passo = 1; passo <= 3; passo++) {
            laminaIngenua = simularPassoIngenuo(altitude, laminaIngenua, deslocamentoLinha, deslocamentoColuna);
        }
        int a = rm % n;
        int b = (rm / 7) % m;
        int d1 = laminaParaD1[a][b];
        int d2 = laminaIngenua[a][b];

        System.out.println("RESUMO RM=" + rm);
        System.out.println("ALERTA=" + totalAlerta);
        System.out.println("CRITICA=" + linhaCritica + " AGUA=" + aguaPorLinha[linhaCritica]);
        System.out.println("D1=" + d1 + " D2=" + d2);
        entrada.close();
    }

    // Junto os RMs para formar o RM da equipe.
    public static int calcularRmEquipe(int[] rms) {
        int soma = 0;
        for (int i = 0; i < rms.length; i++) {
            soma += rms[i];
        }
        return soma;
    }

    // Aqui calculo N, M, K, L e a ordem.
    public static int[] calcularParametros(int rm) {
        int[] parametros = new int[5];
        parametros[0] = 8 + rm % 3;
        parametros[1] = 10 + (rm / 10) % 3;
        parametros[2] = 4 + (rm / 100) % 3;
        parametros[3] = 10 + 2 * (rm % 4);
        parametros[4] = rm % 2;
        return parametros;
    }

    // O vale fica mais baixo perto do meio.
    public static int[][] gerarAltitude(Random gerador, int n, int m) {
        int[][] altitude = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                altitude[i][j] = 3 * Math.abs(i - n / 2) + gerador.nextInt(5);
            }
        }
        return altitude;
    }

    // Sorteio valores dentro da faixa passada.
    public static int[][] gerarMatriz(Random gerador, int n, int m, int minimo, int maximo) {
        int[][] matriz = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matriz[i][j] = minimo + gerador.nextInt(maximo - minimo + 1);
            }
        }
        return matriz;
    }

    // A ordem do RM decide esta sequência vertical.
    public static int[] criarDeslocamentoLinha(int ordem) {
        int[] deslocamentoLinha = new int[4];
        if (ordem == 0) {
            deslocamentoLinha[0] = -1;
            deslocamentoLinha[1] = 0;
            deslocamentoLinha[2] = 1;
            deslocamentoLinha[3] = 0;
        } else {
            deslocamentoLinha[0] = 0;
            deslocamentoLinha[1] = 1;
            deslocamentoLinha[2] = 0;
            deslocamentoLinha[3] = -1;
        }
        return deslocamentoLinha;
    }

    // As colunas acompanham a mesma sequência.
    public static int[] criarDeslocamentoColuna(int ordem) {
        int[] deslocamentoColuna = new int[4];
        if (ordem == 0) {
            deslocamentoColuna[0] = 0;
            deslocamentoColuna[1] = 1;
            deslocamentoColuna[2] = 0;
            deslocamentoColuna[3] = -1;
        } else {
            deslocamentoColuna[0] = -1;
            deslocamentoColuna[1] = 0;
            deslocamentoColuna[2] = 1;
            deslocamentoColuna[3] = 0;
        }
        return deslocamentoColuna;
    }

    // Vejo se a posição existe antes de usar.
    public static boolean dentro(int i, int j, int n, int m) {
        return i >= 0 && i < n && j >= 0 && j < m;
    }

    // Copio célula por célula para guardar a foto do passo.
    public static int[][] copiar(int[][] matriz) {
        int[][] copia = new int[matriz.length][matriz[0].length];
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                copia[i][j] = matriz[i][j];
            }
        }
        return copia;
    }

    // Mostro a matriz com as colunas separadas por tab.
    public static void imprimir(int[][] matriz, String titulo) {
        System.out.println(titulo);
        for (int i = 0; i < matriz.length; i++) {
            String linha = "";
            for (int j = 0; j < matriz[i].length; j++) {
                if (j > 0) {
                    linha += "\t";
                }
                linha += matriz[i][j];
            }
            System.out.println(linha);
        }
    }

    // Esta soma ajuda a conferir se a água se conservou.
    public static int somar(int[][] matriz) {
        int soma = 0;
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                soma += matriz[i][j];
            }
        }
        return soma;
    }

    // Leio a matriz antiga e escrevo o resultado em outra.
    public static int[][] simularPasso(int[][] altitude, int[][] lamina, int[] deslocamentoLinha, int[] deslocamentoColuna) {
        int n = altitude.length;
        int m = altitude[0].length;
        int[][] nova = copiar(lamina);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int t = lamina[i][j] / 2;
                if (t > 0) {
                    int menorNivel = altitude[i][j] + lamina[i][j];
                    int vizinhoI = -1;
                    int vizinhoJ = -1;
                    for (int d = 0; d < 4; d++) {
                        int vi = i + deslocamentoLinha[d];
                        int vj = j + deslocamentoColuna[d];
                        if (dentro(vi, vj, n, m)) {
                            int nivelVizinho = altitude[vi][vj] + lamina[vi][vj];
                            if (nivelVizinho < menorNivel) {
                                menorNivel = nivelVizinho;
                                vizinhoI = vi;
                                vizinhoJ = vj;
                            }
                        }
                    }
                    if (vizinhoI >= 0) {
                        nova[i][j] -= t;
                        nova[vizinhoI][vizinhoJ] += t;
                    }
                }
            }
        }
        return nova;
    }

    // A versão ingênua mexe na matriz enquanto percorre.
    public static int[][] simularPassoIngenuo(int[][] altitude, int[][] lamina, int[] deslocamentoLinha, int[] deslocamentoColuna) {
        int n = altitude.length;
        int m = altitude[0].length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int t = lamina[i][j] / 2;
                if (t > 0) {
                    int nivelAtual = altitude[i][j] + lamina[i][j];
                    int menorNivel = nivelAtual;
                    int vizinhoI = -1;
                    int vizinhoJ = -1;
                    for (int d = 0; d < 4; d++) {
                        int vi = i + deslocamentoLinha[d];
                        int vj = j + deslocamentoColuna[d];
                        if (dentro(vi, vj, n, m)) {
                            int nivelVizinho = altitude[vi][vj] + lamina[vi][vj];
                            if (nivelVizinho < menorNivel) {
                                menorNivel = nivelVizinho;
                                vizinhoI = vi;
                                vizinhoJ = vj;
                            }
                        }
                    }
                    if (vizinhoI >= 0) {
                        lamina[i][j] -= t;
                        lamina[vizinhoI][vizinhoJ] += t;
                    }
                }
            }
        }
        return lamina;
    }

    // A célula entra em alerta quando atinge L.
    public static boolean[][] marcarAlerta(int[][] lamina, int limiarAlerta) {
        boolean[][] alerta = new boolean[lamina.length][lamina[0].length];
        for (int i = 0; i < lamina.length; i++) {
            for (int j = 0; j < lamina[i].length; j++) {
                alerta[i][j] = lamina[i][j] >= limiarAlerta;
            }
        }
        return alerta;
    }

    // Conto os valores true do mapa.
    public static int contarAlerta(boolean[][] alerta) {
        int total = 0;
        for (int i = 0; i < alerta.length; i++) {
            for (int j = 0; j < alerta[i].length; j++) {
                if (alerta[i][j]) {
                    total++;
                }
            }
        }
        return total;
    }

    // # é alagado e ponto é normal.
    public static void imprimirMapaAlerta(boolean[][] alerta) {
        System.out.println("Mapa de alerta (# = alagado, . = normal):");
        for (int i = 0; i < alerta.length; i++) {
            String linha = "";
            for (int j = 0; j < alerta[i].length; j++) {
                if (j > 0) {
                    linha += "\t";
                }
                if (alerta[i][j]) {
                    linha += "#";
                } else {
                    linha += ".";
                }
            }
            System.out.println(linha);
        }
    }

    // Aqui junto os alertas vizinhos pelos lados.
    public static int[] analisarRegioes(boolean[][] alerta) {
        int n = alerta.length;
        int m = alerta[0].length;
        boolean[][] visitada = new boolean[n][m];
        int[] deslocamentoLinha = {-1, 0, 1, 0};
        int[] deslocamentoColuna = {0, 1, 0, -1};
        int[] pilhaI = new int[n * m];
        int[] pilhaJ = new int[n * m];
        int qtdRegioes = 0;
        int maiorRegiao = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (alerta[i][j] && !visitada[i][j]) {
                    qtdRegioes++;
                    int topo = 0;
                    int tamanho = 0;
                    pilhaI[topo] = i;
                    pilhaJ[topo] = j;
                    visitada[i][j] = true;

                    while (topo >= 0) {
                        int atualI = pilhaI[topo];
                        int atualJ = pilhaJ[topo];
                        topo--;
                        tamanho++;

                        for (int d = 0; d < 4; d++) {
                            int vi = atualI + deslocamentoLinha[d];
                            int vj = atualJ + deslocamentoColuna[d];
                            if (dentro(vi, vj, n, m) && alerta[vi][vj] && !visitada[vi][vj]) {
                                topo++;
                                pilhaI[topo] = vi;
                                pilhaJ[topo] = vj;
                                visitada[vi][vj] = true;
                            }
                        }
                    }

                    if (tamanho > maiorRegiao) {
                        maiorRegiao = tamanho;
                    }
                }
            }
        }

        int[] resultado = {qtdRegioes, maiorRegiao};
        return resultado;
    }

    // Somo os valores de cada linha.
    public static int[] calcularAguaPorLinha(int[][] lamina) {
        int[] aguaPorLinha = new int[lamina.length];
        for (int i = 0; i < lamina.length; i++) {
            for (int j = 0; j < lamina[i].length; j++) {
                aguaPorLinha[i] += lamina[i][j];
            }
        }
        return aguaPorLinha;
    }

    // Se empatar, mantenho a linha que apareceu primeiro.
    public static int encontrarLinhaCritica(int[] aguaPorLinha) {
        int linhaCritica = 0;
        for (int i = 1; i < aguaPorLinha.length; i++) {
            if (aguaPorLinha[i] > aguaPorLinha[linhaCritica]) {
                linhaCritica = i;
            }
        }
        return linhaCritica;
    }
}
