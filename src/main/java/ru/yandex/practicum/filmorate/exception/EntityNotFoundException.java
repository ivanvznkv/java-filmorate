package ru.yandex.practicum.filmorate.exception;

public class EntityNotFoundException extends RuntimeException {
    private final String entityName;
    private final Long entityId;

    public EntityNotFoundException(String entityName, Long entityId) {
        super(entityName + " с Id=" + entityId + " не найден");
        this.entityName = entityName;
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getEntityId() {
        return entityId;
    }
}
