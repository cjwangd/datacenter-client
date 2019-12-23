package cn.sh.cares.dsp.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wangcj
 */
public class FileUtils {

    public final static String SUBS_FILE_NAME = "/dspSubscription";

    public synchronized static void saveSubscribe(String dataTypes){
        File f = new File(SUBS_FILE_NAME);

        try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            fileOutputStream.write(dataTypes.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static String readSubscribe(){
        File f = new File(SUBS_FILE_NAME);
        if (!f.canRead()) {
            return null;
        }
        try (FileInputStream fileReader = new FileInputStream(f)){
            byte[] buf = new byte[4096];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (fileReader.read(buf)>0) {
                byteArrayOutputStream.write(buf);
            }
            return  new String(byteArrayOutputStream.toByteArray(),StandardCharsets.UTF_8);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
