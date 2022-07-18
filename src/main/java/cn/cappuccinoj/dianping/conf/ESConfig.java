package cn.cappuccinoj.dianping.conf;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * @Author cappuccino
 * @Date 2022-05-25 19:08
 */
@Configuration
public class ESConfig {

    /**
     * Elastic Search 集群之间使用 Transport 协议通信
     * 在以后的版本将废弃此操作使用 http restful 协议进行集群间通信
     * @return
     */
    @Bean
    public TransportClient getClient() {
        TransportClient transportClient = null;

        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", "dianping-app").build();
            transportClient = new PreBuiltTransportClient(settings);
            TransportAddress firstAddress = new TransportAddress(InetAddress.getByName("127.0.0.1"), 9300);
            TransportAddress secondAddress = new TransportAddress(InetAddress.getByName("127.0.0.1"), 9301);
            TransportAddress thirdAddress = new TransportAddress(InetAddress.getByName("127.0.0.1"), 9302);
            transportClient.addTransportAddresses(firstAddress, secondAddress, thirdAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return transportClient;
    }

}


