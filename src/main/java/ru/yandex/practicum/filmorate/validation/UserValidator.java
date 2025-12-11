package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserValidator {
    private final UserStorage userStorage;

    public User getUserOrThrow(long id) {
        log.info("Запрошен пользователь с id={}", id);
        User user = userStorage.getById(id);
        if (user == null) {
            log.warn("Пользователь с id={} не найден", id);
            throw new EntityNotFoundException("Пользователь", id);
        }
        return user;
    }
}
