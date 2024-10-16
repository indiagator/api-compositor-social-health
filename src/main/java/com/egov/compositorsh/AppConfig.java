package com.egov.compositorsh;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
public class AppConfig {

    @Autowired
    private EurekaDiscoveryClient discoveryClient;



    @Bean
    public WebClient webClient_2(WebClient.Builder webClientBuilder)
    {
        /*
        return webClientBuilder
                .baseUrl("http://"+gateway_hostname+":"+gateway_portnumber+"/health-service/api/v1/gethealthstatus")
                .filter(new LoggingWebClientFilter())
                .build();
                */
        ServiceInstance instance = getServiceInstance("health-service");
        String hostname = instance.getHost();
        int port = instance.getPort();

        return webClientBuilder
                .baseUrl("http://"+hostname+":"+port+"/api/v1/get/btype")
                .filter(new LoggingWebClientFilter())
                .build();

    }

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder)
    {
        /*
        return webClientBuilder
                .baseUrl("http://"+gateway_hostname+":"+gateway_portnumber+"/health-service/api/v1/gethealthstatus")
                .filter(new LoggingWebClientFilter())
                .build();
                */
        ServiceInstance instance = getServiceInstance("social-service");
        String hostname = instance.getHost();
        int port = instance.getPort();

        return webClientBuilder
                    .baseUrl("http://"+hostname+":"+port+"/api/v1/get/social/events")
                .filter(new LoggingWebClientFilter())
                .build();
    }

    public ServiceInstance getServiceInstance(String serviceName)
    {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            throw new RuntimeException("No instances found for "+serviceName);
        }
        return instances.get(0);
    }

}
