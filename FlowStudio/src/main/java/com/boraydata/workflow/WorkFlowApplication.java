package com.boraydata.workflow;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.boraydata.workflow.config", "com.boraydata.workflow"})
public class WorkFlowApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(WorkFlowApplication.class)
                .properties("spring.config.location=classpath:/flowstudio.yml").run(args);
    }
}