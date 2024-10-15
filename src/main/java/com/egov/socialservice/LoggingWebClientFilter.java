package com.egov.socialservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class LoggingWebClientFilter implements ExchangeFilterFunction {

    private static final Logger log = LoggerFactory.getLogger(LoggingWebClientFilter.class);


    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        logRequestHeaders(request);
        return next.exchange(request);
    }

    private void logRequestHeaders(ClientRequest request) {
        request.headers().forEach((name, values) -> {
            log.info("Outgoing Request Header: "+name + ": " + values);
        });
    }
}
