package com.taskmaple.todo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI metadata. Swagger UI is served at /swagger-ui.html, the spec at /v3/api-docs. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI todoOpenApi() {
        return new OpenAPI().info(new Info()
            .title("TODO Folders API")
            .version("1.0.0")
            .description("""
                Folder-scoped task management (story folders-001). Tasks belong to exactly one folder.
                Deleting a folder reassigns its tasks to the default "General" folder in a single
                transaction; General cannot be deleted.""")
            .license(new License().name("Internal").url("about:blank")));
    }
}
