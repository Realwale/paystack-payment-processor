package com.example.paymentprocessor.config;



import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Paystack Payment Processor API")
                .version("1.0")
                .description("API documentation for Paystack payment integration")
                .contact(new Contact()
                    .name("Your Name")
                    .email("your.email@example.com"))
                .license(new License()
                    .name("API License")
                    .url("http://your-license-url.com")))
            .externalDocs(new ExternalDocumentation()
                .description("Additional Documentation")
                .url("https://paystack.com/docs/"));
    }
}