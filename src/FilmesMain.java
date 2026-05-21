import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FilmesMain {

    static class ResultadoFilme implements Comparable<ResultadoFilme> {
        String titulo;
        float nota;
        float popularidade;
        float ano;
        float score;
        String avaliacao;

        public ResultadoFilme(String titulo, float nota, float popularidade, float ano, float score, String avaliacao) {
            this.titulo = titulo;
            this.nota = nota;
            this.popularidade = popularidade;
            this.ano = ano;
            this.score = score;
            this.avaliacao = avaliacao;
        }

        @Override
        public int compareTo(ResultadoFilme outro) {
            // Regra 1: Ordenar pelo Maior Score Fuzzy
            int compareScore = Float.compare(outro.score, this.score);
            if (compareScore != 0) {
                return compareScore;
            }

            // Regra 2 (Desempate): Se o Score for igual, ganha o que tem Maior Nota Real
            int compareNota = Float.compare(outro.nota, this.nota);
            if (compareNota != 0) {
                return compareNota;
            }

            // Regra 3 : Se a Nota for igual, ganha o mais Popular
            return Float.compare(outro.popularidade, this.popularidade);
        }
    }

    public static void main(String[] args) {

        // 1. Avaliação do Filme (Nota)
        VariavelFuzzy notaRuim = new VariavelFuzzy("Ruim", 0, 0, 4.5f, 5.5f);
        VariavelFuzzy notaMedia = new VariavelFuzzy("Media", 4.5f, 5.5f, 6.5f, 7.5f);
        VariavelFuzzy notaBoa = new VariavelFuzzy("Boa", 6.5f, 7.5f, 8.5f, 9.0f);
        VariavelFuzzy notaExcelente = new VariavelFuzzy("Excelente", 8.0f, 9.0f, 10.0f, 10.0f);

        GrupoVariaveis armazenaNotas = new GrupoVariaveis();
        armazenaNotas.addVariavel(notaRuim);
        armazenaNotas.addVariavel(notaMedia);
        armazenaNotas.addVariavel(notaBoa);
        armazenaNotas.addVariavel(notaExcelente);

        // 2. Popularidade do Filme
        VariavelFuzzy popularidadeBaixa = new VariavelFuzzy("Baixa", 0, 0, 20, 40);
        VariavelFuzzy popularidadeMedia = new VariavelFuzzy("Pop_Media", 30, 45, 70, 90);
        VariavelFuzzy popularidadeAlta = new VariavelFuzzy("Alta", 75, 100, 500, 500);

        GrupoVariaveis grupoPopularidade = new GrupoVariaveis();
        grupoPopularidade.addVariavel(popularidadeBaixa);
        grupoPopularidade.addVariavel(popularidadeMedia);
        grupoPopularidade.addVariavel(popularidadeAlta);

        // 3. Duração do Filme (Runtime)
        VariavelFuzzy runtimeCurto = new VariavelFuzzy("Curto", 0, 0, 80, 95);
        VariavelFuzzy runtimeMedio = new VariavelFuzzy("Duracao_Media", 85, 100, 120, 135);
        VariavelFuzzy runtimeLongo = new VariavelFuzzy("Longo", 125, 140, 300, 300);

        GrupoVariaveis grupoRuntime = new GrupoVariaveis();
        grupoRuntime.addVariavel(runtimeCurto);
        grupoRuntime.addVariavel(runtimeMedio);
        grupoRuntime.addVariavel(runtimeLongo);

        // 4. Época de Lançamento
        VariavelFuzzy filmeClassico = new VariavelFuzzy("Classico", 1900, 1900, 1980, 1990);
        VariavelFuzzy filmeRetro = new VariavelFuzzy("Retro", 1985, 1995, 2005, 2010);
        VariavelFuzzy filmeRecente = new VariavelFuzzy("Recente", 2008, 2015, 2026, 2030);

        GrupoVariaveis grupoLancamento = new GrupoVariaveis();
        grupoLancamento.addVariavel(filmeClassico);
        grupoLancamento.addVariavel(filmeRetro);
        grupoLancamento.addVariavel(filmeRecente);

        List<ResultadoFilme> listaResultados = new ArrayList<>();

        try {
            BufferedReader buffer = new BufferedReader(new FileReader(new File("movie_dataset.csv")));
            String leitor = buffer.readLine(); // Ignorar o cabeçalho

            String splitheder[] = leitor.split(",");
            for (int i = 0; i < splitheder.length; i++) {
                System.out.println("" + i + " " + splitheder[i]);
            }
            System.out.println("--------------------------------------------------");

            String linha = "";
            int limite = 0;

            while((linha = buffer.readLine()) != null) {

                String spl[] = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                HashMap<String,Float> valores = new HashMap<String,Float>();

                try {
                    String tituloFilme = spl[18].replace("\"", "").trim();
                    if(tituloFilme.length() > 25) tituloFilme = tituloFilme.substring(0, 22) + "...";

                    float rating = Float.parseFloat(spl[19].trim());
                    float popularidade = Float.parseFloat(spl[9].trim());

                    float runtime = 0f;
                    if(spl.length > 14 && !spl[14].trim().isEmpty()) {
                        runtime = Float.parseFloat(spl[14].trim());
                    }

                    float anoLancamento = 0f;
                    if (spl.length > 12) {
                        String dataCompleta = spl[12].replace("\"", "").trim();
                        if (!dataCompleta.isEmpty() && dataCompleta.length() >= 4) {
                            anoLancamento = Float.parseFloat(dataCompleta.substring(0, 4));
                        }
                    }

                    // Fuzzificação
                    armazenaNotas.fuzzifica(rating, valores);
                    grupoPopularidade.fuzzifica(popularidade, valores);
                    grupoRuntime.fuzzifica(runtime, valores);
                    grupoLancamento.fuzzifica(anoLancamento, valores);

                    // Inicialização dos graus de saída
                    valores.put("NA", 0f); // Não Assistir
                    valores.put("A", 0f);  // Assistir
                    valores.put("MA", 0f); // Muito Assistir

                    // --- REGRAS DE INFERÊNCIA ---
                    rodaRegraE(valores, "Excelente", "Alta", "MA");
                    rodaRegraE(valores, "Excelente", "Pop_Media", "MA");
                    rodaRegraE(valores, "Boa", "Alta", "MA");

                    rodaRegraE(valores, "Boa", "Pop_Media", "A");
                    rodaRegraE(valores, "Media", "Alta", "A");
                    rodaRegraE(valores, "Boa", "Baixa", "A");
                    rodaRegraE(valores, "Media", "Pop_Media", "A");

                    rodaRegraE(valores, "Ruim", "Baixa", "NA");
                    rodaRegraE(valores, "Ruim", "Pop_Media", "NA");
                    rodaRegraE(valores, "Ruim", "Alta", "NA");
                    rodaRegraE(valores, "Media", "Baixa", "NA");

                    // Regras Adicionais
                    rodaRegraE(valores, "Excelente", "Recente", "MA");
                    rodaRegraE(valores, "Ruim", "Longo", "NA");

                    float NA = valores.get("NA");
                    float A = valores.get("A");
                    float MA = valores.get("MA");

                    // Definir o texto da avaliação final com base no maior peso
                    String avaliacaoScore = "Desconhecido";
                    float maxOutputFuzzy = Math.max(NA, Math.max(A, MA));

                    if (maxOutputFuzzy > 0) {
                        if (maxOutputFuzzy == MA) {
                            avaliacaoScore = "Excelente";
                        } else if (maxOutputFuzzy == A) {
                            avaliacaoScore = "Bom";
                        } else if (maxOutputFuzzy == NA) {
                            avaliacaoScore = "Ruim";
                        }
                    }

                    // Defuzzificação
                    float pesoNA = 2.0f;
                    float pesoA = 6.0f;
                    float pesoMA = 9.5f;

                    float denominador = (NA + A + MA);
                    float score = 0;

                    if (denominador > 0) {
                        score = (NA * pesoNA + A * pesoA + MA * pesoMA) / denominador;
                    }

                    System.out.printf("Filme: %-25s | Nota: %4.1f | Pop: %6.1f | Ano: %4.0f | Score: %4.1f (%-9s) \n",
                            tituloFilme, rating, popularidade, anoLancamento, score, avaliacaoScore);

                    listaResultados.add(new ResultadoFilme(tituloFilme, rating, popularidade, anoLancamento, score, avaliacaoScore));

                } catch (Exception e) {
                }

                limite++;
            }
            buffer.close();

            Collections.sort(listaResultados);

            System.out.println("                           MELHORES FILMES                                 ");

            int limiteImpressao = Math.min(20, listaResultados.size());

            for (int i = 0; i < limiteImpressao; i++) {
                ResultadoFilme f = listaResultados.get(i);
                System.out.printf("%2dº | Filme: %-25s | Nota: %4.1f | Pop: %6.1f | Ano: %4.0f | Score: %4.1f (%-9s) \n",
                        (i + 1), f.titulo, f.nota, f.popularidade, f.ano, f.score, f.avaliacao);
            }
            System.out.println("==========================================================================================");

        } catch (FileNotFoundException e) {
            System.out.println("Arquivo CSV não encontrado!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void rodaRegraE(HashMap<String, Float> valores, String a, String b, String c) {
        float v = Math.min(valores.getOrDefault(a, 0f), valores.getOrDefault(b, 0f));
        if(valores.keySet().contains(c)) {
            float vatual = valores.get(c);
            valores.put(c, Math.max(vatual, v));
        } else {
            valores.put(c, v);
        }
    }

    private static void rodaRegraOU(HashMap<String, Float> asVariaveis, String a, String b, String c) {
        float v = Math.max(asVariaveis.getOrDefault(a, 0f), asVariaveis.getOrDefault(b, 0f));
        if(asVariaveis.keySet().contains(c)) {
            float vatual = asVariaveis.get(c);
            asVariaveis.put(c, Math.max(vatual, v));
        } else {
            asVariaveis.put(c, v);
        }
    }
}