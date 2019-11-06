package cn.sh.cares.dsp.client.clients;

import cn.sh.cares.dsp.client.AbstractDspClient;
import cn.sh.cares.dsp.client.DspClientProperty;
import cn.sh.cares.dsp.utils.DspJson;
import cn.sh.cares.dsp.utils.HttpUtil;

/**
 * 接口客户端
 * @author wangcj
 */
public class DataApiClient extends AbstractDspClient {

    public DataApiClient( DspClientProperty dspClientProperty) {
        super(dspClientProperty);
        if (dspClientProperty.getUrl().endsWith(URL_SEP_CHAR)) {
            DSP_CLIENT_URL = dspClientProperty.getUrl() + DSP_API_URL;
        } else {
            DSP_CLIENT_URL = dspClientProperty.getUrl() +URL_SEP_CHAR + DSP_API_URL;
        }
    }

    @Override
    protected void checkParam() {
        super.checkParam();
    }

    @Override
    public void login() {
        super.login();
        doLogin();
    }

    @Override
    public void start() {
        super.start();
    }

    /**
     * 获取接口数据
     *
     * @param jsonbody 请求参数
     * @return json格式字符串
     */
    public String getApiData(String jsonbody) {
        DspJson reqbody = new DspJson();
        reqbody.parse(jsonbody);
        reqbody.put("token", dspClientProperty.getToken());
        reqbody.put("serviceCode", dspClientProperty.getDatatypes());
        return HttpUtil.sendRequestJson(DSP_CLIENT_URL, reqbody.toString());
    }
}
