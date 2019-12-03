package tools;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.w3c.dom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class CloudStack {
    private final String apiKey;
    private final String baseURL;
    private final String key;
    private final Requests requests;

    public CloudStack(String baseURL, String key, String apiKey) {
        this.baseURL = baseURL;
        this.key = key;
        this.apiKey = apiKey;
        this.requests = new Requests();
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
        urlParameters.put("apiKey", apiKey);
//        commands.put("hypervisor", "KVM");
        urlParameters.put("signature", CloudStack.calculateSignature(key, urlParameters));

        return baseURL + CloudStack.toParametersString(urlParameters);
    }

    public Hypervisor[] getKVMHypervisors(){
        Map<String, String> command = new LinkedHashMap<>();

//        command.put("command","listHypervisors");
        command.put("command", "listHosts");
        command.put("hypervisor","KVM");
//        commands.put("id", "f4dd2c32-ad2d-4f70-9b2d-753c37ff3c45");

        String requestURL = generateURL(command);
        Element root = requests.get(requestURL).getDocumentElement();
        System.out.println(root.getElementsByTagName("host").item(0));
        return null;
    }
}
