# 数据中心客户端

本工程是提供一个接入的参考代码，可以直接使用也可自己实现。基本逻辑是基于httppost消息和解析响应
***
## 1、引入依赖
```
      <dependency>
            <groupId>cn.sh.cares</groupId>
            <artifactId>datacenter-client</artifactId>
            <version>LATEST</version>
      </dependency>
```      
***      
## 2、样例配置

```

package com.cares.osbtest;

import cn.sh.cares.datacenterclient.client.DcsClient;
import cn.sh.cares.datacenterclient.client.IMsgResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class config {

    @Value("${datacenter.url}")
    private String url;

    @Value("${datacenter.syscode}")
    private String syscode;

    @Value("${datacenter.token}")
    private String token;

    @Value("#{'${datacenter.datatype}'.split(',')}")
    private List<String> datatypes;

    private AtomicLong atomicLong = new AtomicLong(1L);


    @Bean
    DcsClient dcsClient() {
        DcsClient dcsClient = new DcsClient();
        dcsClient.setUrl(url);
        dcsClient.setDatatypes(datatypes);
        dcsClient.setSysCode(syscode);
        dcsClient.setToken(token);
        dcsClient.setMsgResolver(new MyMsgResolver());
        return dcsClient;
    }

    /**
     * 自定义消息实现
     */
    public class MyMsgResolver implements IMsgResolver {

        /**
         * 解析异步消息
         * 消息格式参考接口文档
         * 必须提供实现
         * @param msg
         */
        @Override
        public void resolve(String msg) {

        }

        /**
         * 全局唯一消息序列
         * 必须提供实现
         * @return
         */
        @Override
        public String getUniqueSeq() {
            return atomicLong.getAndIncrement()+"";
        }
    }

}
```
***
## 3、消息解析需自己实现
  因为消息是异步的，所以解析消息类型和数据类型做相应处理

***
## 4、application.properties

\# 数据中心参数配置

datacenter.url=http://127.0.0.1/dcs/services

datacenter.syscode=NJWX

datacenter.token=token

datacenter.datatype=odsSmislk,odsSimsScs



   
