package cn.sh.cares.dsp;

import cn.sh.cares.dsp.utils.DateUtil;
import cn.sh.cares.dsp.utils.DspJson;
import cn.sh.cares.dsp.utils.HttpUtil;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DspTest {

    private  static ScriptEngineManager manager = new ScriptEngineManager();
    private static ScriptEngine engine = manager.getEngineByMimeType("text/javascript");

    @Test
    public void test(){
        System.out.println(DateUtil.formatDate(new Date()));
        System.out.println(DateUtil.formatDate(DateUtil.parseDate("2019-08-22 12:00:34")));
        System.out.println(HttpUtil.sendRequestJson("http://www.baidu.com",""));
    }

    @Test
    public void testScript() throws ScriptException {

        DspJson dspJson = new DspJson();
        dspJson.parse("{}");
        dspJson.put("abc", 123);
        System.out.println(dspJson.toString());
    }

}
