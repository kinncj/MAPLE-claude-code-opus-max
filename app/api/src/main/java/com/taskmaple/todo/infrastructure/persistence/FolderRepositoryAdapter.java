package com.taskmaple.todo.infrastructure.persistence;

import com.taskmaple.todo.domain.error.DomainExceptions;
import com.taskmaple.todo.domain.model.Folder;
import com.taskmaple.todo.domain.repository.FolderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Adapts Spring Data JPA to the {@link FolderRepository} port. */
@Repository
public class FolderRepositoryAdapter implements FolderRepository {

    private final SpringDataFolderRepository folders;
    private final SpringDataTaskRepository tasks;

    public FolderRepositoryAdapter(SpringDataFolderRepository folders, SpringDataTaskRepository tasks) {
        this.folders = folders;
        this.tasks = tasks;
    }

    @Override
    public List<Folder> findAllOrdered() {
        return folders.findAllByOrderByIsDefaultDescCreatedAtAsc()
            .stream().map(FolderJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Folder> findById(UUID id) {
        return folders.findById(id).map(FolderJpaEntity::toDomain);
    }

    @Override
    public Optional<Folder> findDefault() {
        return folders.findFirstByIsDefaultTrue().map(FolderJpaEntity::toDomain);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return folders.existsByNameCi(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public Folder save(Folder folder) {
        try {
            return folders.save(FolderJpaEntity.fromDomain(folder)).toDomain();
        } catch (DataIntegrityViolationException e) {
            // Unique constraint on name_ci: a concurrent create won the race.
            throw new DomainExceptions.DuplicateFolderName(folder.name());
        }
    }

    @Override
    @Transactional
    public ReassignmentResult deleteAndReassignToDefault(UUID folderId) {
        // Resolve the default folder inside the transaction so the reassignment target cannot
        // change between selection and use.
        UUID generalId = folders.findFirstByIsDefaultTrue()
            .orElseThrow(() -> new IllegalStateException("Default folder is missing"))
            .getId();
        int reassigned = tasks.reassign(folderId, generalId);
        folders.deleteById(folderId);
        return new ReassignmentResult(reassigned, generalId);
    }
}
