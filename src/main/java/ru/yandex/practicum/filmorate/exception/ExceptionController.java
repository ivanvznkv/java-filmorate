package ru.yandex.practicum.filmorate.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationException(ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());

        return new ValidationErrorResponse(
                List.of(new Violation("error", e.getMessage()))
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ValidationErrorResponse handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("{} с Id={} не найден", e.getEntityName(), e.getEntityId());
        return new ValidationErrorResponse(
                List.of(new Violation("id", e.getMessage()))
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<Violation> violations = e.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    log.warn("Ошибка валидации! Поле: {}; Сообщение: {}", error.getField(), error.getDefaultMessage());
                    return new Violation(error.getField(), error.getDefaultMessage());
                })
                .collect(Collectors.toList());

        return new ValidationErrorResponse(violations);
    }

    @Getter
    @AllArgsConstructor
    public static class Violation {
        private final String fieldName;
        private final String message;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ValidationErrorResponse {
        private final List<Violation> violations;
    }
}
