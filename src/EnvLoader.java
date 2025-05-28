import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {
    public static Map<String, String> loadEnv(String path) {
        Map<String, String> env = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();

                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue;
                }
                String[] partes = linha.split("=", 2);
                if (partes.length == 2) {
                    env.put(partes[0].trim(), partes[1].trim());
                }
            }
        } catch (IOException e) {

            System.err.println("ATENÇÃO: Erro ao ler o arquivo .env em '" + path + "'. " + e.getMessage());
            System.err.println("Verifique se o arquivo .env existe no local correto e se as permissões de leitura estão corretas.");

        }
        return env;
    }
}