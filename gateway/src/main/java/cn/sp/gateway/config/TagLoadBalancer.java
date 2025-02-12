package cn.sp.gateway.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.*;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2025/2/12
 */
public class TagLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final String TAG_HEADER = "request-tag";

    private static final Log log = LogFactory.getLog(TagLoadBalancer.class);
    final AtomicInteger position;
    final String serviceId;
    ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    public TagLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        this(serviceInstanceListSupplierProvider, serviceId, (new Random()).nextInt(1000));
    }

    public TagLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, int seedPosition) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger(seedPosition);
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = (ServiceInstanceListSupplier) this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map((serviceInstances) -> {
            return this.processInstanceResponse(supplier, serviceInstances, request);
        });
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier, List<ServiceInstance> serviceInstances, Request request) {
        Response<ServiceInstance> serviceInstanceResponse = this.getInstanceResponse(serviceInstances, request);
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance((ServiceInstance) serviceInstanceResponse.getServer());
        }

        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {
        if (instances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers available for service: " + this.serviceId);
            }
            return new EmptyResponse();
        }
        if (request instanceof DefaultRequest) {
            DefaultRequest<RequestDataContext> defaultRequest = (DefaultRequest) request;
            // 上下文获取请求头
            HttpHeaders headers = defaultRequest.getContext().getClientRequest().getHeaders();
            List<String> list = headers.get(TAG_HEADER);
            if (!CollectionUtils.isEmpty(list)) {
                String requestTag = list.get(0);
                for (ServiceInstance instance : instances) {
                    String str = instance.getMetadata().getOrDefault(TAG_HEADER, "");
                    if (requestTag.equals(str)) {
                        return new DefaultResponse(instance);
                    }
                }
                log.error(String.format("No servers available for service:%s,tag:%s ", this.serviceId, requestTag));
                return new EmptyResponse();
            }
        }

        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        return new DefaultResponse(instance);
    }
}
