package cn.sp.gateway;

import cn.sp.gateway.config.TagLoadBalancerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

@LoadBalancerClients(defaultConfiguration = {TagLoadBalancerConfig.class})
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
