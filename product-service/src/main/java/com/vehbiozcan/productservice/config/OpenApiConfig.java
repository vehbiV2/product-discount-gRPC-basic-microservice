package com.vehbiozcan.productservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(
               new Info()
                       .title("Görev 4: gRPC Entegrasyonu")
                       .version("0.0.1-SNAPSHOT")
                       .description("Türksat Aday Mühendislik Görevi 4. Product - Discount Basic gRPC Microservice")
        );
    }
}
