package cn.sh.cares.dsp.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wangcj
 */

public class HttpUtil {

    private static String POST = "POST";
    private static String PUT = "PUT";


    private static Logger logger = Logger.getLogger(HttpUtil.class.getName());

    private HttpUtil() {

    }

    public static String sendRequestXml(String url, String bodyXml) {
        Map<String, String> map = new HashMap<>(1);
        map.put("Content-Type", "application/xml;charset=UTF-8");
        return sendHttp(url, bodyXml, map, "POST");
    }

    public static String sendRequestJson(String url, String bodyJson) {
        Map<String, String> map = new HashMap<>(1);
        map.put("Content-Type", "application/json;charset=UTF-8");
        return sendHttp(url, bodyJson, map, "POST");
    }


    public static String putXml(String url, String bodyJson) {
        Map<String, String> map = new HashMap<>(1);
        map.put("Content-Type", "application/xml;charset=UTF-8");
        return sendHttp(url, bodyJson, map, "PUT");
    }

    private static String sendHttp(String url, String body, Map<String, String> header, String method) {
        String result = "";
        HttpURLConnection connection = null;

        try (ByteArrayOutputStream bs = new ByteArrayOutputStream();) {

            URL httpUrl = new URL(url);

            connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "DSP-CLIENT");
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            for (Map.Entry<String, String> entry : header.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }

            boolean reqOk = (POST.equals(method) && connection.getResponseCode() == HttpURLConnection.HTTP_OK)

                    || (PUT.equals(method) && connection.getResponseCode() == HttpURLConnection.HTTP_CREATED);

            if (reqOk) {

                try (InputStream in = connection.getInputStream()) {
                    int n = 0;
                    byte[] datas = new byte[2048];

                    while ((n = in.read(datas)) != -1) {
                        bs.write(datas, 0, n);
                    }

                    bs.flush();
                }
                result = new String(bs.toByteArray(), StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "", ex);
        } finally {
            connection.disconnect();
        }

        return result;
    }
}
