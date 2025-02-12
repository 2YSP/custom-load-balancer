package cn.sp.order.config;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2025/2/12
 */
@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnDiscoveryEnabled
@ConditionalOnBlockingDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureBefore({SimpleDiscoveryClientAutoConfiguration.class, CommonsClientAutoConfiguration.class})
@AutoConfigureAfter({NacosDiscoveryAutoConfiguration.class})
public class MyNacosDiscoveryClientConfiguration {


    @Bean
    public DiscoveryClient nacosDiscoveryClient(NacosServiceDiscovery nacosServiceDiscovery) {
        return new NacosDiscoveryClient(nacosServiceDiscovery);
    }

    @Bean
    @ConditionalOnProperty(
            value = {"spring.cloud.nacos.discovery.watch.enabled"},
            matchIfMissing = true
    )
    public NacosWatch nacosWatch(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties nacosDiscoveryProperties, ObjectProvider<ThreadPoolTaskScheduler> taskExecutorObjectProvider) {
        nacosDiscoveryProperties.getMetadata().put("request-tag", "ship");
        return new NacosWatch(nacosServiceManager, nacosDiscoveryProperties, taskExecutorObjectProvider);
    }
}
