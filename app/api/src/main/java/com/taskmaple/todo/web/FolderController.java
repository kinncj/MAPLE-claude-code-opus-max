package com.taskmaple.todo.web;

import com.taskmaple.todo.application.FolderService;
import com.taskmaple.todo.application.view.DeleteFolderResult;
import com.taskmaple.todo.application.view.FolderView;
import com.taskmaple.todo.web.dto.CreateFolderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/folders")
@Tag(name = "Folders", description = "Create, list, and delete folders (delete reassigns tasks to General)")
public class FolderController {

    private final FolderService folders;

    public FolderController(FolderService folders) {
        this.folders = folders;
    }

    @GetMapping
    @Operation(summary = "List all folders with live task counts")
    public List<FolderView> list() {
        return folders.listFolders();
    }

    @PostMapping
    @Operation(summary = "Create a folder (name trimmed, 1–50 chars, case-insensitive unique)")
    public ResponseEntity<FolderView> create(@RequestBody(required = false) CreateFolderRequest request) {
        String name = request == null ? null : request.name();
        return ResponseEntity.status(HttpStatus.CREATED).body(folders.createFolder(name));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a folder; its tasks are reassigned to General. General cannot be deleted.")
    public DeleteFolderResult delete(@PathVariable UUID id) {
        return folders.deleteFolder(id);
    }
}
