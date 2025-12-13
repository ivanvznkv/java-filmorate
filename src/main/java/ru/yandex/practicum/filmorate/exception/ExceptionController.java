package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("Ошибка валидации");
        log.warn("Ошибка валидации MethodArgumentNotValidException: {}", message);
        return new ErrorResponse("Ошибка валидации", message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("Ошибка валидации");
        log.warn("Ошибка валидации ConstraintViolationException: {}", message);
        return new ErrorResponse("Ошибка валидации", message);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("{} с Id={} не найден", e.getEntityName(), e.getEntityId());
        return new ErrorResponse(
                "Объект не найден",
                e.getEntityName() + " с Id=" + e.getEntityId() + " не найден"
        );
    }

    @ExceptionHandler(FriendOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleFriendOperationException(FriendOperationException e) {
        return new ErrorResponse("Ошибка работы с друзьями", e.getMessage());
    }


    @ExceptionHandler(LikeOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleOperationException(RuntimeException e) {
        log.warn("Ошибка операции: {}", e.getMessage());
        return new ErrorResponse("Ошибка операции", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllOtherExceptions(Exception e) {
        log.error("Непредвиденная ошибка: ", e);
        return new ErrorResponse("Внутренняя ошибка сервера", e.getMessage());
    }
}
