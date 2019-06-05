package cn.sh.cares.dsp.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {

    public static String sendRequestXml(String url,String bodyXml) throws Exception {
        Map<String,String> map = new HashMap<>(1);
        map.put("Content-Type", "application/xml;charset=UTF-8");
        return  sendHttp(url, bodyXml, map);

    }

    public static String sendRequestJson(String url,String bodyJson) throws Exception {
        Map<String,String> map = new HashMap<>(1);
        map.put("Content-Type", "application/json;charset=UTF-8");
        return  sendHttp(url, bodyJson, map);
    }


    private static String sendHttp(String url, String body, Map<String, String> header) throws Exception {
        InputStream in = null;
        String result = "";
        HttpURLConnection connection = null;

        try {

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
            connection.getOutputStream().write(body.getBytes("utf-8"));
            connection.getOutputStream().flush();

            if (connection.getResponseCode() == 200) {
                in = connection.getInputStream();

                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                int n = 0;
                byte[] datas = new byte[2048];

                while ((n = in.read(datas)) != -1) {
                    bs.write(datas, 0, n);
                }

                bs.flush();
                result = new String(bs.toByteArray(), "utf-8");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }

            try {
                connection.disconnect();
            } catch (Exception ex) {
                return null;
            }
        }

        return result;
    }
}
