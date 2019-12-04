package tools;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class CloudStack {
    private final String apiKey;
    private final String baseURL;
    private final String key;
    private final Requests requests;
    private final String privateKey;
    private final Hypervisor[] hypervisors;

    public CloudStack(String baseURL, String key, String apiKey, String privateKey) {
        this.baseURL = baseURL;
        this.key = key;
        this.apiKey = apiKey;
        this.privateKey = privateKey;
        this.requests = new Requests();
        this.hypervisors = initializeKVMHypervisors();
    }

    public static String calculateSignature(String key, Map<String, String> commands) {

        Map<String, String> sortedCommands = new TreeMap<>(commands);
        String parameters = toParametersString(sortedCommands).toLowerCase();
        byte[] keyBytes = key.getBytes();
        byte[] parametersBytes = parameters.getBytes();

        return new String(Base64.encodeBase64(HmacUtils.getHmacSha1(keyBytes).doFinal(parametersBytes))).trim();
    }

    private static String toURLFriendly(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            throw new RuntimeException("UTF-8 encoding was missed !!!!");
        }
    }

    private static String toParametersString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();

        for (String current : map.keySet()) {
            sb.append(current)
                    .append("=")
                    .append(toURLFriendly(map.get(current)))
                    .append("&");
        }

        sb.delete(sb.length() - 1, sb.length());

        return sb.toString();
    }

    private String generateURL(Map<String, String> command) {
        Map<String, String> urlParameters = new LinkedHashMap<>(command);

//        commands.put("id", "f4dd2c32-ad2d-4f70-9b2d-753c37ff3c45");
        urlParameters.put("apiKey", apiKey);
//        commands.put("hypervisor", "KVM");
        urlParameters.put("signature", CloudStack.calculateSignature(key, urlParameters));

        return baseURL + CloudStack.toParametersString(urlParameters);
    }

    private Hypervisor[] initializeKVMHypervisors() {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "listHosts");
        command.put("hypervisor", "KVM");

        String requestURL = generateURL(command);
        Element root = requests.get(requestURL).getDocumentElement();

        // todo check root for error

        NodeList hosts = root.getElementsByTagName("host");

        Hypervisor[] hypervisors = new Hypervisor[hosts.getLength()];
        String id,ip,name ,state,resourceState;

        for (int i = 0; i < hosts.getLength(); i++) {
            Node node = hosts.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element host = (Element) node;

                id = getTextContent(host, "id");
                ip = getTextContent(host, "ipaddress");
                name = getTextContent(host, "name");
                state = getTextContent(host, "state");
                resourceState = getTextContent(host, "resourcestate");

                try {
                    hypervisors[i] = new KVM(id, ip, name, state, resourceState);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

        }

        return hypervisors;
    }

    private static String getTextContent(Element e, String tagName) {
        return e.getElementsByTagName(tagName).item(0).getTextContent();
    }

    private class KVM implements Hypervisor {
        private final Shell shell;
        private final String id;
        private final InetAddress ip;
        private final String name;
        private String state;
        private String resourceState;

        private KVM(String id, String ip, String name, String state, String resourceState) throws UnknownHostException {
            this.id = id;
            this.ip = InetAddress.getByName(ip);
            this.name = name;
            this.state = state;
            this.resourceState = resourceState;

            this.shell = new Ssh(ip, 22, "root", privateKey);
        }

        @Override
        public String update() throws IOException {
            return new Shell.Plain(shell).exec("echo 'update'");
        }

        @Override
        public String reboot() throws IOException {
            return new Shell.Plain(shell).exec("echo 'reboot'");
        }
    }
}
