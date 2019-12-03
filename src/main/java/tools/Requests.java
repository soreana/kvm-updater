package tools;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Requests {
    private DocumentBuilder builder;

    public Requests() {
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public Document get(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();

            ByteArrayInputStream input = new ByteArrayInputStream(
                    Objects.requireNonNull(response.body())
                            .string().getBytes(StandardCharsets.UTF_8));

            return builder.parse(input);
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
