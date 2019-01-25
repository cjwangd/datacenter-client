#数据中心客户端


##1、引入依赖

      <dependency>
            <groupId>cn.sh.cares</groupId>
            <artifactId>datacenter-client</artifactId>
            <version>1.0.RELEASE</version>
      </dependency>
      
      
##2、样例配置


package com.cares.osbtest;

import cn.sh.cares.datacenterclient.client.DcsClient;

import cn.sh.cares.datacenterclient.client.IMsgResolver;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import java.util.List;

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

    public class MyMsgResolver implements IMsgResolver {
        @Override
        public void resolve(String msg) {
           // 自实现解析逻辑，根据消息类型和数据类型
        }
    }

}


##3、消息解析需自己实现
  因为消息是异步的，所以解析消息类型和数据类型做相应处理


##4、application.properties

\# 数据中心参数配置

datacenter.url=http://127.0.0.1/dcs/services

datacenter.syscode=NJWX

datacenter.token=token

datacenter.datatype=odsSmislk,odsSimsScs



   
