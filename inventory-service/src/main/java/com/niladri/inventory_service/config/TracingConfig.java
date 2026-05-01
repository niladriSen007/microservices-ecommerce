package com.niladri.inventory_service.config;

//import feign.Capability;
//import feign.micrometer.MicrometerCapability;
//import io.micrometer.core.instrument.MeterRegistry;
//import org.springframework.context.annotation.Bean;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

//    @Bean
//    public Capability capability(final MeterRegistry registry) {
//        return new MicrometerCapability(registry);
//    }

//    @Bean
//    ApplicationRunner runner(Tracer tracer) {
//        return args -> {
//            System.out.println("Tracer bean: " + tracer);
//        };
//    }
}
