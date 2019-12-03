package tools;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class CloudStack {
    private final String API_KEY ;
    private final String BASE_URL;
    private final String KEY;

    public CloudStack(String baseURL, String key, String apiKey){
        this.BASE_URL = baseURL;
        this.KEY = key;
        this.API_KEY = apiKey;
    }

    public static String calculateSignature(String key, Map<String, String> commands){

        Map<String, String> sortedCommands = new TreeMap<>(commands);
        String parameters = toParametersString(sortedCommands).toLowerCase();
        byte[] keyBytes = key.getBytes();
        byte[] parametersBytes = parameters.getBytes();

        return new String(Base64.encodeBase64(HmacUtils.getHmacSha1(keyBytes).doFinal(parametersBytes))).trim();
    }

    private static String toURLFriendly(String s){
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ignored){
            throw new RuntimeException("UTF-8 encoding was missed !!!!");
        }
    }

    private static String toParametersString(Map<String, String> map){
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

    private String generateURL(Map<String, String> command){
        Map<String, String> urlParameters = new LinkedHashMap<>(command);

//        commands.put("id", "f4dd2c32-ad2d-4f70-9b2d-753c37ff3c45");
        urlParameters.put("apiKey", API_KEY);
//        commands.put("hypervisor", "KVM");
        urlParameters.put("signature", CloudStack.calculateSignature(KEY, urlParameters));

        return BASE_URL + CloudStack.toParametersString(urlParameters);
    }

    public Hypervisor[] getKVMHypervisors(){
        Map<String, String> command = new LinkedHashMap<>();

//        command.put("command","listHypervisors");
        command.put("command", "listHosts");
        command.put("hypervisor","KVM");
//        commands.put("id", "f4dd2c32-ad2d-4f70-9b2d-753c37ff3c45");

        System.out.println(this.generateURL(command));
        return null;
    }
}
