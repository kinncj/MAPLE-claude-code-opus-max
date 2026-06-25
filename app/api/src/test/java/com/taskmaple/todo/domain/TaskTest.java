package com.taskmaple.todo.domain;

import com.taskmaple.todo.domain.error.DomainExceptions;
import com.taskmaple.todo.domain.model.Task;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    private final UUID folderId = UUID.randomUUID();

    @Test
    void createTrimsTitleAndDefaultsNotDone() {
        Task t = Task.create(folderId, "  Buy milk ");
        assertThat(t.title()).isEqualTo("Buy milk");
        assertThat(t.done()).isFalse();
        assertThat(t.folderId()).isEqualTo(folderId);
    }

    @Test
    void createRejectsEmptyTitle() {
        assertThatThrownBy(() -> Task.create(folderId, "   "))
            .isInstanceOf(DomainExceptions.InvalidTaskTitle.class);
    }

    @Test
    void createRejectsNullFolder() {
        assertThatThrownBy(() -> Task.create(null, "Buy milk"))
            .isInstanceOf(DomainExceptions.InvalidTaskTitle.class);
    }

    @Test
    void createRejectsTooLongTitle() {
        assertThatThrownBy(() -> Task.create(folderId, "x".repeat(501)))
            .isInstanceOf(DomainExceptions.InvalidTaskTitle.class);
    }

    @Test
    void markDoneToggles() {
        Task t = Task.create(folderId, "Walk dog");
        t.markDone(true);
        assertThat(t.done()).isTrue();
        t.markDone(false);
        assertThat(t.done()).isFalse();
    }

    @Test
    void reassignToChangesFolderAndRejectsNull() {
        Task t = Task.create(folderId, "Walk dog");
        UUID other = UUID.randomUUID();
        t.reassignTo(other);
        assertThat(t.folderId()).isEqualTo(other);
        assertThatThrownBy(() -> t.reassignTo(null))
            .isInstanceOf(DomainExceptions.InvalidTaskTitle.class);
    }
}
