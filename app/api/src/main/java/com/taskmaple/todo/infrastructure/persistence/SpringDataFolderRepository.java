package com.taskmaple.todo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA repository for folders. Infra-internal; adapted to the domain port. */
public interface SpringDataFolderRepository extends JpaRepository<FolderJpaEntity, UUID> {

    /** Default ("General") first (isDefault desc), then oldest-first by creation. */
    List<FolderJpaEntity> findAllByOrderByIsDefaultDescCreatedAtAsc();

    Optional<FolderJpaEntity> findFirstByIsDefaultTrue();

    boolean existsByNameCi(String nameCi);
}
