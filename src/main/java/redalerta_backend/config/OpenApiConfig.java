package redalerta_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de OpenAPI 3.0 / Swagger UI para la documentacion interactiva de la API.
 * Configura el esquema de seguridad Bearer JWT para permitir la prueba de endpoints protegidos.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RedAlerta Backend API")
                        .version("1.0.0")
                        .description("API RESTful para la gestion de seguridad comunitaria, alertas tempranas y caracterizacion de usuarios.")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo RedAlerta")
                                .email("soporte@redalerta.uniquindio.edu.co"))
                        .license(new License()
                                .name("Uso Academico e Institucional")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT recibido en /auth/login para autorizar las solicitudes.")));
    }
}
