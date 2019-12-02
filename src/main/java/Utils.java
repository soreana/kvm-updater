import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;

import java.io.*;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

public class Utils {

    static String toURLFriendly(String s){
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ignored){
            throw new RuntimeException("UTF-8 encoding was missed !!!!");
        }
    }

    static String toParametersString(Map<String, String> map){
        StringBuilder sb = new StringBuilder();

        for (String current: map.keySet()){
            sb.append(current)
                    .append("=")
                    .append(toURLFriendly(map.get(current)))
                    .append("&");
        }

        sb.delete(sb.length()-1,sb.length());

        return sb.toString();
    }

    static String calculateSignature(String key, Map<String, String> commands){

        Map<String, String> sortedCommands = new TreeMap<>(commands);
        String parameters = Utils.toParametersString(sortedCommands).toLowerCase();
        byte[] keyBytes = key.getBytes();
        byte[] parametersBytes = parameters.getBytes();

        return new String(Base64.encodeBase64(HmacUtils.getHmacSha1(keyBytes).doFinal(parametersBytes))).trim();
    }

    static String readPrivateKey() throws IOException {
        File file = new File("./keys/id_rsa");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String st;
            StringBuilder key = new StringBuilder();

            while ((st = br.readLine()) != null)
                key.append(st).append("\n");

            return key.toString();
        }
    }
}
