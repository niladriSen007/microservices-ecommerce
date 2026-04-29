package com.niladri.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class ProductServiceApplication {

    public static void main(String[] args) {
        /* TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata")); */
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}
