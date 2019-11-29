import java.io.*;

public class Main {
    private static String readPrivateKey() throws IOException {
        File file = new File("./keys/id_rsa");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String st;
            StringBuilder key = new StringBuilder();

            while ((st = br.readLine()) != null)
                key.append(st);

            return key.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(readPrivateKey());
    }
}
