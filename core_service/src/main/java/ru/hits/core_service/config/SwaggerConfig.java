package ru.hits.core_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI coreServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Core Service API")
                        .description("Ядро интернет-банка. Управление счетами, операциями и кредитами.")
                        .version("1.0.0"));
    }
}
