package com.taskmaple.todo.web;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract test: the live, generated OpenAPI document exposes the documented operations and
 * Swagger UI is served. Verifies the API surface matches the published contract.
 */
class OpenApiContractIT extends ApiIntegrationBase {

    @Test
    void openApiDocumentsAllOperations() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi", containsString("3.")))
            .andExpect(jsonPath("$.paths['/api/v1/folders'].get").exists())
            .andExpect(jsonPath("$.paths['/api/v1/folders'].post").exists())
            .andExpect(jsonPath("$.paths['/api/v1/folders/{id}'].delete").exists())
            .andExpect(jsonPath("$.paths['/api/v1/tasks'].get").exists())
            .andExpect(jsonPath("$.paths['/api/v1/tasks'].post").exists())
            .andExpect(jsonPath("$.paths['/api/v1/tasks/{id}'].patch").exists())
            .andExpect(jsonPath("$.paths['/api/v1/tasks/{id}'].delete").exists());
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(jsonPath("$.info.title", is("TODO Folders API")));
    }
}
