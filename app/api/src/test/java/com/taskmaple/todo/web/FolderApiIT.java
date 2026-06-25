package com.taskmaple.todo.web;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class FolderApiIT extends ApiIntegrationBase {

    @Test
    void listsFoldersGeneralFirstWithCounts() throws Exception {
        mvc.perform(get("/api/v1/folders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name", is("General")))
            .andExpect(jsonPath("$[0].isDefault", is(true)))
            .andExpect(jsonPath("$[0].taskCount", is(3)));
        org.assertj.core.api.Assertions.assertThat(folderCount("Work")).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(folderCount("Groceries")).isEqualTo(2);
    }

    @Test
    void createsFolder() throws Exception {
        mvc.perform(post("/api/v1/folders").contentType(APPLICATION_JSON).content("{\"name\":\"Reading\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("Reading")))
            .andExpect(jsonPath("$.isDefault", is(false)))
            .andExpect(jsonPath("$.taskCount", is(0)));
    }

    @Test
    void rejectsDuplicateNameCaseInsensitive() throws Exception {
        mvc.perform(post("/api/v1/folders").contentType(APPLICATION_JSON).content("{\"name\":\"work\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code", is("DUPLICATE_NAME")));
    }

    @Test
    void rejectsEmptyName() throws Exception {
        mvc.perform(post("/api/v1/folders").contentType(APPLICATION_JSON).content("{\"name\":\"   \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code", is("VALIDATION")))
            .andExpect(jsonPath("$.error.field", is("name")));
    }

    @Test
    void deleteReassignsTasksToGeneral() throws Exception {
        UUID work = folderIdByName("Work");
        long generalBefore = folderCount("General");

        mvc.perform(delete("/api/v1/folders/" + work))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reassignedCount", is(1)))
            .andExpect(jsonPath("$.deletedFolderId", is(work.toString())));

        // Work is gone; its task moved to General (count +1).
        mvc.perform(get("/api/v1/folders")).andExpect(jsonPath("$[0].name", is("General")));
        java.util.List<String> names = new java.util.ArrayList<>();
        folders().forEach(f -> names.add(f.get("name").asText()));
        org.assertj.core.api.Assertions.assertThat(names).doesNotContain("Work");
        org.assertj.core.api.Assertions.assertThat(folderCount("General")).isEqualTo(generalBefore + 1);
    }

    @Test
    void cannotDeleteGeneral() throws Exception {
        UUID general = folderIdByName("General");
        mvc.perform(delete("/api/v1/folders/" + general))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code", is("CANNOT_DELETE_DEFAULT")));
    }

    @Test
    void deleteUnknownFolderReturns404() throws Exception {
        mvc.perform(delete("/api/v1/folders/" + UUID.randomUUID()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
    }
}
