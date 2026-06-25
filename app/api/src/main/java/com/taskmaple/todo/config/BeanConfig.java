package com.taskmaple.todo.config;

import com.taskmaple.todo.application.FolderService;
import com.taskmaple.todo.application.TaskService;
import com.taskmaple.todo.application.port.AuditLog;
import com.taskmaple.todo.domain.repository.FolderRepository;
import com.taskmaple.todo.domain.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free application services from their domain ports. Declaring the services
 * here (rather than annotating them with {@code @Service}) keeps the application layer import-clean.
 */
@Configuration
public class BeanConfig {

    @Bean
    public FolderService folderService(FolderRepository folders, TaskRepository tasks, AuditLog audit) {
        return new FolderService(folders, tasks, audit);
    }

    @Bean
    public TaskService taskService(TaskRepository tasks, FolderRepository folders) {
        return new TaskService(tasks, folders);
    }
}
