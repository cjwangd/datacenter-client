package cn.sh.cares.dsp.utils;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.concurrent.atomic.AtomicLong;

public class DspJson {
      private  static   ScriptEngineManager manager = new ScriptEngineManager();
      private static   ScriptEngine engine = manager.getEngineByMimeType("text/javascript");

      private AtomicLong atomicLong = new AtomicLong(1);
      private String jsonvar;

    public DspJson() {
        try {
            jsonvar = "json" + atomicLong.getAndIncrement();
            engine.eval(jsonvar+"={}");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }



    public void put(String key,Object val) {
        ScriptObjectMirror scriptObjectMirror =   (ScriptObjectMirror) engine.get(jsonvar);
        scriptObjectMirror.put(key, val);
    }

    public void parse(String jsonbody) {

        try {
            engine.eval(jsonvar + "=" + jsonbody);
        } catch (ScriptException e) {
            e.printStackTrace();
        }

    }


    @Override
    public String toString() {
        try {
            return (String) engine.eval("JSON.stringify("+jsonvar+")");
        } catch (ScriptException e) {
            e.printStackTrace();
            return "";
        }
    }
}
