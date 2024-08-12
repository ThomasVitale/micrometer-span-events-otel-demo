package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.testcontainers.grafana.LgtmStackContainer;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @Scope("singleton")
    @ServiceConnection("otel/opentelemetry-collector-contrib")
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer("grafana/otel-lgtm:0.7.1")
                .withStartupTimeout(Duration.ofMinutes(2))
                .withReuse(true);
    }

}
