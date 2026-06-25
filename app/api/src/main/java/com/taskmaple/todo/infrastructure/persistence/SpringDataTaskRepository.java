package com.taskmaple.todo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/** Spring Data JPA repository for tasks. Infra-internal; adapted to the domain port. */
public interface SpringDataTaskRepository extends JpaRepository<TaskJpaEntity, UUID> {

    List<TaskJpaEntity> findByFolderIdOrderByCreatedAtAscIdAsc(UUID folderId);

    List<TaskJpaEntity> findAllByOrderByCreatedAtAscIdAsc();

    /** Bulk reassignment used by the transactional delete-and-reassign operation. */
    @Modifying
    @Query("update TaskJpaEntity t set t.folderId = :to where t.folderId = :from")
    int reassign(@Param("from") UUID from, @Param("to") UUID to);

    @Query("select t.folderId as folderId, count(t) as cnt from TaskJpaEntity t group by t.folderId")
    List<FolderCount> countGroupedByFolder();

    /** Projection for grouped counts. */
    interface FolderCount {
        UUID getFolderId();

        long getCnt();
    }
}
