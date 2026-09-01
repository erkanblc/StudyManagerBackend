package com.studymanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token'ı Authorization header'ına 'Bearer <token>' formatında ekleyin")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Education Platform API")
                .description("""
                        Education Platform — Kullanıcı, Rol ve İzin Yönetimi REST API'si.
                        
                        **Kimlik doğrulama:** JWT Bearer Token  
                        **Kullanım:** Sağ üstteki 'Authorize' butonuna tıklayıp `Bearer <token>` formatında token girin.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Education Platform Team")
                        .email("dev@educationplatform.com")
                );
    }
}
