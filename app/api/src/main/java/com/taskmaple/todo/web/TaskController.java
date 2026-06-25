package com.taskmaple.todo.web;

import com.taskmaple.todo.application.TaskService;
import com.taskmaple.todo.application.view.TaskView;
import com.taskmaple.todo.web.dto.CreateTaskRequest;
import com.taskmaple.todo.web.dto.UpdateTaskRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Create, list, toggle, rename, and delete tasks within folders")
public class TaskController {

    private final TaskService tasks;

    public TaskController(TaskService tasks) {
        this.tasks = tasks;
    }

    @GetMapping
    @Operation(summary = "List tasks; omit folderId for the cross-folder (All folders) view")
    public List<TaskView> list(@RequestParam(name = "folderId", required = false) UUID folderId) {
        return tasks.listTasks(folderId);
    }

    @PostMapping
    @Operation(summary = "Create a task in a folder (title trimmed, 1–500 chars)")
    public ResponseEntity<TaskView> create(@RequestBody(required = false) CreateTaskRequest request) {
        UUID folderId = request == null ? null : request.folderId();
        String title = request == null ? null : request.title();
        return ResponseEntity.status(HttpStatus.CREATED).body(tasks.createTask(folderId, title));
    }

    @PatchMapping("/{id}")
    public TaskView update(@PathVariable UUID id, @RequestBody(required = false) UpdateTaskRequest request) {
        Boolean done = request == null ? null : request.done();
        String title = request == null ? null : request.title();
        return tasks.updateTask(id, done, title);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tasks.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
