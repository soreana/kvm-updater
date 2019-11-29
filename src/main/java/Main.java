import java.io.*;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;

public class Main {
    private static String readPrivateKey() throws IOException {
        File file = new File("./keys/id_rsa");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String st;
            StringBuilder key = new StringBuilder();

            while ((st = br.readLine()) != null)
                key.append(st).append("\n");

            return key.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        Shell shell = new Ssh("kashipazha.ir", 22, "asa", readPrivateKey());
        String stdout = new Shell.Plain(shell).exec("echo 'Hello, world!'");
        System.out.println(stdout);
    }
}
