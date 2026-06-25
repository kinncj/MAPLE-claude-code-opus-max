package com.taskmaple.todo.infrastructure.observability;

import com.taskmaple.todo.application.port.AuditLog;
import com.taskmaple.todo.domain.model.Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Structured logging of folder lifecycle events, including reassignment counts. */
@Component
public class Slf4jAuditLog implements AuditLog {

    private static final Logger log = LoggerFactory.getLogger("todo.audit");

    @Override
    public void folderCreated(Folder folder) {
        log.info("event=folder.created folderId={} name=\"{}\" isDefault={}",
            folder.id(), folder.name(), folder.isDefault());
    }

    @Override
    public void folderDeleted(UUID folderId, String name, int reassignedCount, UUID generalFolderId) {
        log.info("event=folder.reassigned fromFolderId={} toFolderId={} reassignedCount={}",
            folderId, generalFolderId, reassignedCount);
        log.info("event=folder.deleted folderId={} name=\"{}\" reassignedCount={}",
            folderId, name, reassignedCount);
    }
}
