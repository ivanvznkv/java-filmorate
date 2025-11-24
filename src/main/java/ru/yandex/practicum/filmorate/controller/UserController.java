package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.info("Получен запрос на добавление пользователя: {}", user);

        if (user.getEmail() == null ||
                user.getEmail().isEmpty() ||
                !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: некорректный email. Пользователь: {}", user);
            throw new ValidationException("Имэйл не может быть пустым и должен содержать символ @!");
        }

        if (user.getLogin() == null ||
                user.getLogin().isBlank() ||
                user.getLogin().matches(".*\\s.*")) {
            log.warn("Ошибка валидации: некорректный логин. Пользователь: {}", user);
            throw new ValidationException("Логин не может быть пустым и содержать пробелы!");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя отсутствует, использован логин как имя: {}", user.getLogin());
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: некорректная дата рождения. Пользователь: {}", user);
            throw new ValidationException("Дата рождения не может быть в будущем!");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        log.info("Получен запрос на обновление пользователя: {}", updatedUser);

        if (updatedUser.getId() == null) {
            log.warn("Ошибка обновления: не указан id. Пользователь: {}", updatedUser);
            throw new ValidationException("Id должен быть указан");
        }

        User oldUser = users.get(updatedUser.getId());
        if (oldUser == null) {
            log.warn("Ошибка обновления: пользователь с id={} не найден", updatedUser.getId());
            throw new ValidationException("Пользователь с указанным Id не найден");
        }

        if (updatedUser.getEmail() != null) {
            if (updatedUser.getEmail().isBlank() || !updatedUser.getEmail().contains("@")) {
                log.warn("Ошибка валидации email при обновлении: {}", updatedUser);
                throw new ValidationException("Имэйл не может быть пустым и должен содержать символ @!");
            }
            oldUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getLogin() != null) {
            if (updatedUser.getLogin().isBlank() ||
                    updatedUser.getLogin().matches(".*\\s.*")) {
                log.warn("Ошибка валидации логина при обновлении: {}", updatedUser);
                throw new ValidationException("Логин не может быть пустым и содержать пробелы!");
            }
            oldUser.setLogin(updatedUser.getLogin());
        }

        if (updatedUser.getName() != null) {
            if (updatedUser.getName().isBlank()) {
                oldUser.setName(oldUser.getLogin());
                log.info("Имя пустое. Установлено значение логина: {}", oldUser.getLogin());
            } else {
                oldUser.setName(updatedUser.getName());
            }
        }

        if (updatedUser.getBirthday() != null) {
            if (updatedUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Ошибка валидации даты рождения при обновлении: {}", updatedUser);
                throw new ValidationException("Дата рождения не может быть в будущем!");
            }
            oldUser.setBirthday(updatedUser.getBirthday());
        }

        users.put(oldUser.getId(), oldUser);
        log.info("Пользователь успешно обновлён: {}", oldUser);
        return oldUser;
    }

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Запрошен список всех пользователей.");
        return users.values();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
