package com.taskmaple.todo.application;

import com.taskmaple.todo.application.view.DeleteFolderResult;
import com.taskmaple.todo.application.view.FolderView;
import com.taskmaple.todo.domain.error.DomainExceptions;
import com.taskmaple.todo.domain.model.Folder;
import com.taskmaple.todo.domain.model.Task;
import com.taskmaple.todo.support.InMemoryRepositories;
import com.taskmaple.todo.support.InMemoryRepositories.RecordingAuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FolderServiceTest {

    private InMemoryRepositories repos;
    private RecordingAuditLog audit;
    private FolderService service;
    private Folder general;

    @BeforeEach
    void setUp() {
        repos = new InMemoryRepositories();
        audit = new RecordingAuditLog();
        service = new FolderService(repos.folderRepository, repos.taskRepository, audit);
        general = repos.seedGeneral();
    }

    @Test
    void listsFoldersWithLiveCounts() {
        Folder work = repos.seedFolder("Work");
        repos.seedTask(general.id(), "Buy milk", false);
        repos.seedTask(work.id(), "Ship", false);

        List<FolderView> views = service.listFolders();

        assertThat(views).extracting(FolderView::name).containsExactly("General", "Work");
        assertThat(views.get(0).taskCount()).isEqualTo(1);
        assertThat(views.get(1).taskCount()).isEqualTo(1);
        assertThat(views.get(0).isDefault()).isTrue();
    }

    @Test
    void createsFolderAndAudits() {
        FolderView created = service.createFolder("  Reading ");
        assertThat(created.name()).isEqualTo("Reading");
        assertThat(created.taskCount()).isZero();
        assertThat(audit.created).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateNameCaseInsensitive() {
        service.createFolder("Work");
        assertThatThrownBy(() -> service.createFolder("work"))
            .isInstanceOf(DomainExceptions.DuplicateFolderName.class);
    }

    @Test
    void rejectsEmptyName() {
        assertThatThrownBy(() -> service.createFolder("   "))
            .isInstanceOf(DomainExceptions.InvalidFolderName.class);
    }

    @Test
    void deleteReassignsTasksToGeneralAndRemovesFolder() {
        Folder work = repos.seedFolder("Work");
        Task t = repos.seedTask(work.id(), "Prepare deck", false);

        DeleteFolderResult result = service.deleteFolder(work.id());

        assertThat(result.reassignedCount()).isEqualTo(1);
        assertThat(result.generalFolderId()).isEqualTo(general.id());
        assertThat(repos.folders).doesNotContainKey(work.id());
        assertThat(repos.tasks.get(t.id()).folderId()).isEqualTo(general.id());
        assertThat(audit.deleted).isEqualTo(1);
        assertThat(audit.lastReassignedCount).isEqualTo(1);
    }

    @Test
    void cannotDeleteGeneral() {
        assertThatThrownBy(() -> service.deleteFolder(general.id()))
            .isInstanceOf(DomainExceptions.DefaultFolderProtected.class);
        assertThat(repos.folders).containsKey(general.id());
    }

    @Test
    void deleteUnknownFolderThrowsNotFound() {
        assertThatThrownBy(() -> service.deleteFolder(java.util.UUID.randomUUID()))
            .isInstanceOf(DomainExceptions.FolderNotFound.class);
    }
}
