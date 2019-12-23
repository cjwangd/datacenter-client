package cn.sh.cares.dsp.client;

import java.util.List;
/**
 * 客户端属性
 * @author wangcj
 */
public class DspClientProperty {
    /**
     * 系统代码
     **/
    private String sysCode = "";
    /**
     * 数据类型
     **/
    private String datatypes;

    private String token;

    /**
     * 用户名
     **/
    private String username;

    /**
     * 密码
     **/
    private String password;
    /**
     * 数据共享平台连接地址
     **/
    private String url;


    /**
     * 默认心跳间隔
     */
    private Long hearbeatInteval = 60000L;


    /**
     * 数据请求间隔
     */
    private Long datareqInteval = 5000L;

    private IMsgResolver msgResolver;

    private boolean logEnabled = false;

    private boolean subscribed = false;

    private List<Class> classesInput;


    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getDatatypes() {
        return datatypes;
    }

    public void setDatatypes(String datatypes) {
        this.datatypes = datatypes;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getHearbeatInteval() {
        return hearbeatInteval;
    }

    public void setHearbeatInteval(Long hearbeatInteval) {
        this.hearbeatInteval = hearbeatInteval;
    }

    public Long getDatareqInteval() {
        return datareqInteval;
    }

    public void setDatareqInteval(Long datareqInteval) {
        this.datareqInteval = datareqInteval;
    }

    public IMsgResolver getMsgResolver() {
        return msgResolver;
    }

    public void setMsgResolver(IMsgResolver msgResolver) {
        this.msgResolver = msgResolver;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }


    public List<Class> getClassesInput() {
        return classesInput;
    }

    public void setClassesInput(List<Class> classesInput) {
        this.classesInput = classesInput;
    }
}
