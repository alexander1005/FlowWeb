package com.boraydata.flowregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

/**
 * TODO
 *
 * @date: 2021/4/14
 * @author: hatter
 **/

@ComponentScan({"com.boraydata.flowregistry.dao","com.boraydata.flowregistry"})
@SpringBootApplication
public class SchemaRegistryApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SchemaRegistryApplication.class)
                .properties("spring.config.location=classpath:/flowregistry.yml").run(args);
    }
}