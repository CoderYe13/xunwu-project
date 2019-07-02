package com.henu.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * es配置类
 */
@Configuration
public class ElasticSearchConfig {
    @Bean
    public TransportClient esClient() throws UnknownHostException{

        Settings settings= Settings.builder()
                .put("cluster.name","elasticsearch")
                .put("client.transport.sniff",true)
                .build();
        InetSocketTransportAddress master=new InetSocketTransportAddress(
          InetAddress.getByName("192.168.0.114"),9300);
        TransportClient client=new PreBuiltTransportClient(settings)
                .addTransportAddresses(master);
        return client;
    }
}
