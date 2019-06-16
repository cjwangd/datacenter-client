package cn.sh.cares.dsp.utils;

import java.io.*;
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


    private static Logger logger = Logger.getLogger(HttpUtil.class.getName());

    private HttpUtil() {

    }

    public static String sendRequestXml(String url,String bodyXml) {
        Map<String,String> map = new HashMap<>(1);
        map.put("Content-Type", "application/xml;charset=UTF-8");
        return  sendHttp(url, bodyXml, map);

    }

    public static String sendRequestJson(String url,String bodyJson) {
        Map<String,String> map = new HashMap<>(1);
        map.put("Content-Type", "application/json;charset=UTF-8");
        return  sendHttp(url, bodyJson, map);
    }


    private static String sendHttp(String url, String body, Map<String, String> header) {
        String result = "";
        HttpURLConnection connection = null;

        try(ByteArrayOutputStream bs = new ByteArrayOutputStream();) {

            URL httpUrl = new URL(url);

            connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent","DSP-CLIENT");
            connection.setRequestMethod("POST");

            for (Map.Entry<String,String> entry : header.entrySet()) {
                connection.setRequestProperty(entry.getKey(),entry.getValue());
            }



            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            try (OutputStream outputStream = connection.getOutputStream()){
                outputStream.write(body.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }


            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

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
