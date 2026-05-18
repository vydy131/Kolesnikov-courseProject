package com.investagg.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI investAggOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Investment Aggregator Platform API")
                        .version("1.0.0")
                        .description("REST API for aggregating brokerage accounts and portfolio analytics"));
    }
}
