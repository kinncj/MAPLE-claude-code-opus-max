package com.taskmaple.todo.domain.error;

/** Concrete domain exceptions, grouped to keep the error vocabulary in one place. */
public final class DomainExceptions {

    private DomainExceptions() {
    }

    public static final class InvalidFolderName extends DomainException {
        public InvalidFolderName(String message) {
            super(ErrorCode.VALIDATION, message, "name");
        }
    }

    public static final class InvalidTaskTitle extends DomainException {
        public InvalidTaskTitle(String message) {
            super(ErrorCode.VALIDATION, message, "title");
        }
    }

    public static final class DuplicateFolderName extends DomainException {
        public DuplicateFolderName(String name) {
            super(ErrorCode.DUPLICATE_NAME, "A folder named \"" + name + "\" already exists.", "name");
        }
    }

    public static final class DefaultFolderProtected extends DomainException {
        public DefaultFolderProtected() {
            super(ErrorCode.CANNOT_DELETE_DEFAULT, "The default \"General\" folder cannot be deleted.", null);
        }
    }

    public static final class FolderNotFound extends DomainException {
        public FolderNotFound(Object id) {
            super(ErrorCode.NOT_FOUND, "Folder " + id + " was not found.", "folderId");
        }
    }

    public static final class TaskNotFound extends DomainException {
        public TaskNotFound(Object id) {
            super(ErrorCode.NOT_FOUND, "Task " + id + " was not found.", "id");
        }
    }
}
