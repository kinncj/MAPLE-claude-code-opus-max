package com.taskmaple.todo.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmaple.todo.infrastructure.support.SeedService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Base for API integration tests. Boots the full context against H2 and resets to the canonical
 * Background (General=3, Work=1, Groceries=2) before each test for determinism.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"todo.seed=false"})
abstract class ApiIntegrationBase {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    @Autowired
    protected SeedService seed;

    @BeforeEach
    void resetState() {
        seed.resetToBackground();
    }

    protected JsonNode folders() throws Exception {
        String body = mvc.perform(get("/api/v1/folders")).andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    protected UUID folderIdByName(String name) throws Exception {
        for (JsonNode f : folders()) {
            if (f.get("name").asText().equals(name)) {
                return UUID.fromString(f.get("id").asText());
            }
        }
        throw new AssertionError("folder not found: " + name);
    }

    protected long folderCount(String name) throws Exception {
        for (JsonNode f : folders()) {
            if (f.get("name").asText().equals(name)) {
                return f.get("taskCount").asLong();
            }
        }
        throw new AssertionError("folder not found: " + name);
    }
}
