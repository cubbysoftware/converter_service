package com.cubbysoftware.converter;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "File Conveter Service", description = "file-converter-service", version = "1.0"))
public class ConverterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConverterApplication.class, args);
    }
}
