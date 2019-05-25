package cn.sh.cares.dsp.client;

/**
 * 数据共享平台消息解析接口，由接入方自己实现
 */
public interface IMsgResolver {

    /**
     * 解析消息
     *
     * @param msg
     */
    void resolve(String msg);

    /**
     * 获取消息唯一序列
     * @return
     */
    String getUniqueSeq();



}
