package com.taskmaple.todo.web;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskApiIT extends ApiIntegrationBase {

    private UUID taskIdByTitle(UUID folderId, String title) throws Exception {
        String body = mvc.perform(get("/api/v1/tasks").param("folderId", folderId.toString()))
            .andReturn().getResponse().getContentAsString();
        for (JsonNode t : json.readTree(body)) {
            if (t.get("title").asText().equals(title)) {
                return UUID.fromString(t.get("id").asText());
            }
        }
        throw new AssertionError("task not found: " + title);
    }

    @Test
    void listsTasksScopedToFolder() throws Exception {
        UUID general = folderIdByName("General");
        String body = mvc.perform(get("/api/v1/tasks").param("folderId", general.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andReturn().getResponse().getContentAsString();
        for (JsonNode t : json.readTree(body)) {
            org.assertj.core.api.Assertions.assertThat(t.get("folderName").asText()).isEqualTo("General");
        }
    }

    @Test
    void listsAllTasksLabeledByFolder() throws Exception {
        String body = mvc.perform(get("/api/v1/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(6)))
            .andReturn().getResponse().getContentAsString();
        boolean found = false;
        for (JsonNode t : json.readTree(body)) {
            if (t.get("title").asText().equals("Prepare release deck")) {
                org.assertj.core.api.Assertions.assertThat(t.get("folderName").asText()).isEqualTo("Work");
                found = true;
            }
        }
        org.assertj.core.api.Assertions.assertThat(found).isTrue();
    }

    @Test
    void createsTaskInFolderAndIncrementsCount() throws Exception {
        UUID work = folderIdByName("Work");
        mvc.perform(post("/api/v1/tasks").contentType(APPLICATION_JSON)
                .content("{\"folderId\":\"" + work + "\",\"title\":\"Ship release\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title", is("Ship release")))
            .andExpect(jsonPath("$.folderName", is("Work")))
            .andExpect(jsonPath("$.done", is(false)));
        org.assertj.core.api.Assertions.assertThat(folderCount("Work")).isEqualTo(2);
    }

    @Test
    void togglesTaskDone() throws Exception {
        UUID general = folderIdByName("General");
        UUID buyMilk = taskIdByTitle(general, "Buy milk");
        mvc.perform(patch("/api/v1/tasks/" + buyMilk).contentType(APPLICATION_JSON).content("{\"done\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.done", is(true)));
    }

    @Test
    void deletesTaskAndDecrementsCount() throws Exception {
        UUID general = folderIdByName("General");
        UUID walkDog = taskIdByTitle(general, "Walk dog");
        mvc.perform(delete("/api/v1/tasks/" + walkDog)).andExpect(status().isNoContent());
        org.assertj.core.api.Assertions.assertThat(folderCount("General")).isEqualTo(2);
    }

    @Test
    void rejectsEmptyTitle() throws Exception {
        UUID general = folderIdByName("General");
        mvc.perform(post("/api/v1/tasks").contentType(APPLICATION_JSON)
                .content("{\"folderId\":\"" + general + "\",\"title\":\"  \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code", is("VALIDATION")));
    }

    @Test
    void rejectsUnknownFolder() throws Exception {
        mvc.perform(post("/api/v1/tasks").contentType(APPLICATION_JSON)
                .content("{\"folderId\":\"" + UUID.randomUUID() + "\",\"title\":\"x\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
    }

    @Test
    void rejectsMissingFolderId() throws Exception {
        mvc.perform(post("/api/v1/tasks").contentType(APPLICATION_JSON).content("{\"title\":\"x\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code", is("VALIDATION")));
    }
}
