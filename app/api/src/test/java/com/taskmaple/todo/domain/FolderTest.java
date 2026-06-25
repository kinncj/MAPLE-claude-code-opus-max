package com.taskmaple.todo.domain;

import com.taskmaple.todo.domain.error.DomainExceptions;
import com.taskmaple.todo.domain.model.Folder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FolderTest {

    @Test
    void createTrimsName() {
        Folder f = Folder.create("  Work  ");
        assertThat(f.name()).isEqualTo("Work");
        assertThat(f.isDefault()).isFalse();
        assertThat(f.id()).isNotNull();
        assertThat(f.createdAt()).isNotNull();
    }

    @Test
    void createRejectsEmptyName() {
        assertThatThrownBy(() -> Folder.create("   "))
            .isInstanceOf(DomainExceptions.InvalidFolderName.class);
        assertThatThrownBy(() -> Folder.create(""))
            .isInstanceOf(DomainExceptions.InvalidFolderName.class);
        assertThatThrownBy(() -> Folder.create(null))
            .isInstanceOf(DomainExceptions.InvalidFolderName.class);
    }

    @Test
    void createRejectsTooLongName() {
        String tooLong = "x".repeat(51);
        assertThatThrownBy(() -> Folder.create(tooLong))
            .isInstanceOf(DomainExceptions.InvalidFolderName.class);
    }

    @Test
    void acceptsBoundaryLength() {
        assertThat(Folder.create("x".repeat(50)).name()).hasSize(50);
    }

    @Test
    void createDefaultIsGeneralAndProtected() {
        Folder general = Folder.createDefault();
        assertThat(general.name()).isEqualTo("General");
        assertThat(general.isDefault()).isTrue();
    }

    @Test
    void renameValidates() {
        Folder f = Folder.create("Work");
        f.rename("  Projects ");
        assertThat(f.name()).isEqualTo("Projects");
        assertThatThrownBy(() -> f.rename(""))
            .isInstanceOf(DomainExceptions.InvalidFolderName.class);
    }
}
