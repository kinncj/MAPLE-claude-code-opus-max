package com.taskmaple.todo.application;

import com.taskmaple.todo.application.view.TaskView;
import com.taskmaple.todo.domain.error.DomainException;
import com.taskmaple.todo.domain.error.DomainExceptions;
import com.taskmaple.todo.domain.error.ErrorCode;
import com.taskmaple.todo.domain.model.Folder;
import com.taskmaple.todo.domain.model.Task;
import com.taskmaple.todo.support.InMemoryRepositories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskServiceTest {

    private InMemoryRepositories repos;
    private TaskService service;
    private Folder general;
    private Folder work;

    @BeforeEach
    void setUp() {
        repos = new InMemoryRepositories();
        service = new TaskService(repos.taskRepository, repos.folderRepository);
        general = repos.seedGeneral();
        work = repos.seedFolder("Work");
    }

    @Test
    void listScopedToFolder() {
        repos.seedTask(general.id(), "Buy milk", false);
        repos.seedTask(work.id(), "Ship", false);

        List<TaskView> generalTasks = service.listTasks(general.id());
        assertThat(generalTasks).extracting(TaskView::title).containsExactly("Buy milk");
        assertThat(generalTasks.get(0).folderName()).isEqualTo("General");
    }

    @Test
    void listAllLabelsByFolder() {
        repos.seedTask(general.id(), "Buy milk", false);
        repos.seedTask(work.id(), "Ship", false);

        List<TaskView> all = service.listTasks(null);
        assertThat(all).extracting(TaskView::folderName).contains("General", "Work");
        assertThat(all).hasSize(2);
    }

    @Test
    void listUnknownFolderThrowsNotFound() {
        assertThatThrownBy(() -> service.listTasks(UUID.randomUUID()))
            .isInstanceOf(DomainExceptions.FolderNotFound.class);
    }

    @Test
    void createTaskInFolder() {
        TaskView created = service.createTask(work.id(), "  Ship release ");
        assertThat(created.title()).isEqualTo("Ship release");
        assertThat(created.folderId()).isEqualTo(work.id());
        assertThat(created.folderName()).isEqualTo("Work");
        assertThat(created.done()).isFalse();
    }

    @Test
    void createTaskUnknownFolderThrowsNotFound() {
        assertThatThrownBy(() -> service.createTask(UUID.randomUUID(), "x"))
            .isInstanceOf(DomainExceptions.FolderNotFound.class);
    }

    @Test
    void createTaskNullFolderIsValidationError() {
        assertThatThrownBy(() -> service.createTask(null, "x"))
            .isInstanceOf(DomainException.class)
            .extracting(e -> ((DomainException) e).code())
            .isEqualTo(ErrorCode.VALIDATION);
    }

    @Test
    void updateTogglesDone() {
        Task t = repos.seedTask(general.id(), "Buy milk", false);
        TaskView updated = service.updateTask(t.id(), true, null);
        assertThat(updated.done()).isTrue();
    }

    @Test
    void updateRenames() {
        Task t = repos.seedTask(general.id(), "Buy milk", false);
        TaskView updated = service.updateTask(t.id(), null, "Buy oat milk");
        assertThat(updated.title()).isEqualTo("Buy oat milk");
    }

    @Test
    void updateUnknownTaskThrowsNotFound() {
        assertThatThrownBy(() -> service.updateTask(UUID.randomUUID(), true, null))
            .isInstanceOf(DomainExceptions.TaskNotFound.class);
    }

    @Test
    void deleteRemovesTask() {
        Task t = repos.seedTask(general.id(), "Buy milk", false);
        service.deleteTask(t.id());
        assertThat(repos.tasks).doesNotContainKey(t.id());
    }

    @Test
    void deleteUnknownTaskThrowsNotFound() {
        assertThatThrownBy(() -> service.deleteTask(UUID.randomUUID()))
            .isInstanceOf(DomainExceptions.TaskNotFound.class);
    }
}
