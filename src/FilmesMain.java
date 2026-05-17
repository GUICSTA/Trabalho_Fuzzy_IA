import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class FilmesMain {
    public static void main(String[] args) {


        //Avaliação do Filme
        VariavelFuzzy notaRuim = new VariavelFuzzy("Ruim", 0, 0, 4.5f, 5.5f);
        VariavelFuzzy notaMedia = new VariavelFuzzy("Media", 4.5f, 5.5f, 6.5f, 7.5f);
        VariavelFuzzy notaBoa = new VariavelFuzzy("Boa", 6.5f, 7.5f, 8.5f, 9.0f);
        VariavelFuzzy notaExcelente = new VariavelFuzzy("Excelente", 8.0f, 9.0f, 10.0f, 10.0f);

        GrupoVariaveis armazenaNotas = new GrupoVariaveis();
        armazenaNotas.addVariavel(notaRuim);
        armazenaNotas.addVariavel(notaMedia);
        armazenaNotas.addVariavel(notaBoa);
        armazenaNotas.addVariavel(notaExcelente);

        // Popularidade do Filme
        VariavelFuzzy popularidadeBaixa = new VariavelFuzzy("Baixa", 0, 0, 20, 40);
        VariavelFuzzy popularidadeMedia = new VariavelFuzzy("Pop_Media", 30, 45, 70, 90);
        VariavelFuzzy popularidadeAlta = new VariavelFuzzy("Alta", 75, 100, 500, 500); // Até 500 para pegar os blockbusters

        GrupoVariaveis grupoPopularidade = new GrupoVariaveis();
        grupoPopularidade.addVariavel(popularidadeBaixa);
        grupoPopularidade.addVariavel(popularidadeMedia);
        grupoPopularidade.addVariavel(popularidadeAlta);


        try {
            BufferedReader buffer = new BufferedReader(new FileReader(new File("movie_dataset.csv")));


            String leitor = buffer.readLine();
            String splitheder[] = leitor.split(",");
            for (int i = 0; i < splitheder.length; i++) {
                System.out.println("" + i + " " + splitheder[i]);
            }
            System.out.println("--------------------------------------------------");

            String linha = "";
            int limite = 0;

            while((linha = buffer.readLine()) != null && limite < 100) {


                String spl[] = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                HashMap<String,Float> valores = new HashMap<String,Float>();

                try {
                    String tituloFilme = spl[18].replace("\"", "").trim();

                    float rating = Float.parseFloat(spl[19].trim());
                    float popularidade = Float.parseFloat(spl[9].trim());

                    // fuzzi
                    armazenaNotas.fuzzifica(rating, valores);
                    grupoPopularidade.fuzzifica(popularidade, valores);

                    //graus de saída
                    valores.put("NA", 0f);
                    valores.put("A", 0f);
                    valores.put("MA", 0f);

                    //REGRAS
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

                    float NA = valores.get("NA");
                    float A = valores.get("A");
                    float MA = valores.get("MA");

                    float pesoNA = 2.0f;
                    float pesoA = 6.0f;
                    float pesoMA = 9.5f;

                    float denominador = (NA + A + MA);
                    float score = 0;

                    if (denominador > 0) {
                        score = (NA * pesoNA + A * pesoA + MA * pesoMA) / denominador;
                    }

                    System.out.printf("Filme: %-30s | Nota: %4.1f | Popularidade: %6.1f | Score Fuzzy: %4.1f \n",
                            tituloFilme, rating, popularidade, score);

                } catch (Exception e) {
                    System.err.println("Erro ao processar linha " + limite + ": " + e.getMessage());
                }

                limite++;
            }

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
        }else {
            valores.put(c, v);
        }
    }

    private static void rodaRegraOU(HashMap<String, Float> asVariaveis, String a, String b, String c) {
        float v = Math.max(asVariaveis.getOrDefault(a, 0f), asVariaveis.getOrDefault(b, 0f));
        if(asVariaveis.keySet().contains(c)) {
            float vatual = asVariaveis.get(c);
            asVariaveis.put(c, Math.max(vatual, v));
        }else {
            asVariaveis.put(c, v);
        }
    }
}