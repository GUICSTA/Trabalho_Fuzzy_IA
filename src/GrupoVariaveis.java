import java.util.ArrayList;
import java.util.HashMap;

public class GrupoVariaveis {
    ArrayList<VariavelFuzzy> listaDeVariaveis;
    public GrupoVariaveis() {
        listaDeVariaveis = new ArrayList<>();
    }

    public void addVariavel(VariavelFuzzy var) {
        listaDeVariaveis.add(var);
    }

    public void fuzzifica(float v, HashMap<String,Float> variveisfuzzy) {
        for(int i = 0; i < listaDeVariaveis.size(); i++) {
            VariavelFuzzy var = listaDeVariaveis.get(i);
            float val = var.fuzzifica(v);
            variveisfuzzy.put(var.nome, val);
        }
    }

    public ArrayList<VariavelFuzzy> getListaDeVariaveis() {
        return listaDeVariaveis;
    }

    public void setListaDeVariaveis(ArrayList<VariavelFuzzy> listaDeVariaveis) {
        this.listaDeVariaveis = listaDeVariaveis;
    }

    public VariavelFuzzy getVariavel(String nome) {
        for (VariavelFuzzy var : listaDeVariaveis) {
            if (var.nome.equalsIgnoreCase(nome)) {
                return var;
            }
        }
        return null;
    }
}